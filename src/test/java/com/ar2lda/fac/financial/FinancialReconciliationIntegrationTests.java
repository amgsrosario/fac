package com.ar2lda.fac.financial;

import com.ar2lda.fac.controller.dto.*;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.UtilizadorRepository;
import com.ar2lda.fac.service.DocumentoFinanceiroService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static com.ar2lda.fac.financial.FinancialReconciliation.*;

@SpringBootTest
class FinancialReconciliationIntegrationTests {
    @Autowired FinancialReconciliation diagnosis;
    @Autowired FinancialCore core;
    @Autowired FinancialQueries queries;
    @Autowired FinancialReceiptIssuer issuer;
    @Autowired FinancialReceiptReversal reverser;
    @Autowired FinancialReceiptProjections projections;
    @Autowired DocumentoFinanceiroService legacy;
    @Autowired JdbcTemplate db;
    @Autowired UtilizadorRepository users;
    @Autowired PlatformTransactionManager tx;
    long client; String serie; long invoiceSequence;
    record Debt(long document,long pending,long position) {}
    static BigDecimal m(String value) { return new BigDecimal(value); }
    String key() { return UUID.randomUUID().toString(); }
    FinancialCore.Scope scope() { return new FinancialCore.Scope(1,client,"EUR"); }
    @BeforeEach void fixture() {
        db.update("insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('B1T','Teste') on conflict do nothing");
        db.update("insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais) values('B1A','Teste','Rua','1000-020','Lisboa','PT') on conflict do nothing");
        db.update("insert into mpagamento(id,nome) values('B1P','Teste') on conflict do nothing");
        db.update("insert into empresa(id,nome,nif,morada,id_codpostal,localidade,id_pais,capital_social,matricula_registo_comercial,cae,descricao_cae,email) values(1,'Teste','500000000','Rua','1000-020','Lisboa','PT',0,'Registo','00000','Teste','b1@fac.test') on conflict do nothing");
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('BFT','FT Teste',2,1,1,'FT'),('BRC','RC Teste',3,1,2,'RC') on conflict do nothing");
        if(users.findById("B1_EMISSOR").isEmpty()) users.save(new Utilizador("B1_EMISSOR","Teste","b1@fac.test","$2a$10$123456789012345678901uZVXKMcIlbsK8PL5kN1l4I2zxIlW85ee",false));
        client=db.queryForObject("select nextval('cliente_id_seq')",Long.class);
        db.update("insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values(?,'Teste','Rua','1000-020',?,'EUR','CON','B1T','PT')",client,Long.toString(900000000+client));
        serie=key().substring(0,8);
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('BRC',?,'Teste',0,'B1RC','2026-01-01'),('BFT',?,'Teste',0,'B1FT','2026-01-01')",serie,serie);
    }
    Debt debt(String amount) { return debt(amount,true); }
    Debt debt(String amount,boolean originate) {
        long number=++invoiceSequence;
        long doc=db.queryForObject("""
            insert into documento_comercial(id_tipo_documento,serie,numero_documento,estado,data_emissao,id_cliente,
              id_armazem_carga,id_moeda,id_riva,id_ppagamento,id_transporte,cliente_nome,cliente_nif,cliente_morada,
              cliente_cod_postal,cliente_pais,data_carga,carga_nome,carga_morada,carga_cod_postal,carga_localidade,carga_pais,
              data_vencimento,valor_total,numero_documento_completo)
            values('BFT',?,?,'EMITIDO','2026-01-01',?,'B1A','EUR','CON','P30','B1T','Teste','999999990','Rua',
              '1000-020','PT','2026-01-01','Teste','Rua','1000-020','Lisboa','PT','2026-01-31',?,?) returning id
            """,Long.class,serie,number,client,m(amount),"BFT "+serie+"/"+number);
        long pending=db.queryForObject("""
            insert into pendente(id_documento_comercial,id_cliente,id_tipo_documento,numero_documento,serie_documento,
            valor_documento,valor_pendente,data_documento,data_vencimento,id_moeda)
            values(?,?,'BFT',?,?,?,?,'2026-01-01','2026-01-31','EUR') returning id
            """,Long.class,doc,client,number,serie,m(amount),m(amount));
        if(!originate) return new Debt(doc,pending,0);
        var p=core.create(scope(),"FT_ORIGIN:"+doc,FinancialCore.Nature.DEBT,"DOCUMENTO_COMERCIAL:"+doc,
                "BFT "+serie+"/"+number,m(amount),"B1_EMISSOR");
        db.update("insert into financial_document_origin(document_id,position_id) values(?,?)",doc,p.id());
        return new Debt(doc,pending,p.id());
    }
    DocumentoFinanceiroCreateDto request(LocalDate date,List<LinhaDocumentoFinanceiroCreateDto> lines) {
        return new DocumentoFinanceiroCreateDto("BRC",serie,date,client,"EUR","B1P",null,"B1_EMISSOR",null,lines);
    }
    LinhaDocumentoFinanceiroCreateDto line(Debt d,String amount) { return new LinhaDocumentoFinanceiroCreateDto(d.pending(),m(amount),m("0"),m("0")); }
    DocumentoFinanceiroCreateDto request(Debt d,String amount) { return request(LocalDate.of(2026,2,1),List.of(line(d,amount))); }

    Entry only() { return diagnosis.inspect(scope(),0,500).page().items().getFirst(); }
    @Test void unpaidInvoiceAndGlobalComparableTotals() {
        var d=debt("100");var report=diagnosis.inspect(scope(),0,1);var row=report.page().items().getFirst();
        assertThat(row.documentId()).isEqualTo(d.document());assertThat(row.clientId()).isEqualTo(client);
        assertThat(row.pendingId()).isEqualTo(d.pending());assertThat(row.positionId()).isEqualTo(d.position());
        assertThat(row.comparable()).isTrue();assertThat(row.findings()).isEmpty();assertThat(row.difference()).isZero();
        assertThat(report.summary().comparableLegacy()).isEqualByComparingTo("100");
        assertThat(report.summary().fullComparisonCoverage()).isTrue();assertThat(report.operationalActivationPermitted()).isFalse();
    }
    @Test void partialAndFullLegacyPaymentExplainDifferenceWithoutCallingItAccountingError() {
        var d=debt("100");var receipt=legacy.create(request(d,"30"));var row=only();
        assertThat(row.legacyResidual()).isEqualByComparingTo("70");assertThat(row.coreResidual()).isEqualByComparingTo("100");
        assertThat(row.difference()).isEqualByComparingTo("-30");
        assertThat(row.findings()).contains(Finding.ACTIVE_LEGACY_DOCUMENT,Finding.DIFFERENCE_OBSERVED);
        assertThat(row.explanation()).contains("não escrevem no núcleo").contains("sem presumir erro");
        var refs=diagnosis.receipts(scope(),d.document(),0,1);
        assertThat(refs.total()).isEqualTo(1);assertThat(refs.items().getFirst().documentId()).isEqualTo(receipt.id());
        assertThat(refs.items().getFirst().coreLinked()).isFalse();
        legacy.create(request(d,"70"));
        assertThat(only().legacyResidual()).isZero();assertThat(only().difference()).isEqualByComparingTo("-100");
        assertThat(diagnosis.receipts(scope(),d.document(),1,1).items()).hasSize(1);
        assertThat(diagnosis.receipts(scope(),d.document(),2,1).total()).isEqualTo(2);
    }
    @Test void backdatedCoreReceiptHasEvidenceAndDoesNotChangeLegacyAuthority() {
        var d=debt("100");var date=LocalDate.of(2026,1,10);var base=request(d,"30");
        var r=issuer.issue(key(),new DocumentoFinanceiroCreateDto(base.tipoDocumentoId(),base.serie(),base.dataEmissao(),client,"EUR","B1P",null,"B1_EMISSOR",null,base.linhas(),date));
        var row=only();assertThat(row.legacyResidual()).isEqualByComparingTo("100");
        assertThat(row.coreResidual()).isEqualByComparingTo("70");assertThat(row.difference()).isEqualByComparingTo("30");
        assertThat(row.findings()).contains(Finding.CORE_RECEIPT,Finding.DIFFERENCE_OBSERVED).doesNotContain(Finding.ACTIVE_LEGACY_DOCUMENT);
        var trace=diagnosis.receipts(scope(),d.document(),0,500).items().getFirst();
        assertThat(trace.documentId()).isEqualTo(r.id());assertThat(trace.valueDate()).isEqualTo(date);
        assertThat(trace.issueDate()).isEqualTo(base.dataEmissao());assertThat(trace.operationId()).isNotNull();assertThat(trace.allocationId()).isNotNull();
        assertThat(row.consumed()).isEqualByComparingTo("30");assertThat(row.restored()).isZero();
        assertThat(projections.inspectCutover().operationalActivationPermitted()).isFalse();
    }
    @Test void dv09AndMultipleInvoicesDoNotMultiplyRowsOrTotals() {
        var a=debt("1000");var b=debt("100");
        var first=issuer.issue(key(),request(LocalDate.of(2026,2,1),List.of(line(a,"30"),line(b,"10"))));
        reverser.replace(scope(),key(),first.id(),"Correction","Verified payments","B1_EMISSOR",request(LocalDate.of(2026,2,1),List.of(line(a,"300"),line(b,"20"))));
        var report=diagnosis.inspect(scope(),0,500);
        assertThat(report.page().items()).hasSize(2);assertThat(report.summary().documents()).isEqualTo(2);
        assertThat(report.summary().comparableLegacy()).isEqualByComparingTo("1100");
        assertThat(report.summary().comparableCore()).isEqualByComparingTo("780");
        assertThat(report.summary().comparableDifference()).isEqualByComparingTo("320");
        var row=report.page().items().getFirst();assertThat(row.consumed()).isEqualByComparingTo("330");assertThat(row.restored()).isEqualByComparingTo("30");
        assertThat(diagnosis.receipts(scope(),a.document(),0,500).items()).hasSize(2);
        assertThat(queries.allocations(scope(),a.position(),0,500).items()).anyMatch(x->x.effect()==FinancialQueries.Effect.RESTORE);
    }
    @Test void cancelledCreationAndUndatedEffectAreHistoricalLimitsNotNewTemporalRules() {
        var a=debt("100");long creation=queries.position(scope(),a.position()).creationOperationId();
        new TransactionTemplate(tx).execute(status->{
            core.reverse(scope(),key(),creation,"FT cancellation","B1_EMISSOR");
            db.update("update documento_comercial set anulado=true,estado='ANULADO' where id=?",a.document());
            db.update("update pendente set valor_pendente=0 where id=?",a.pending());return null;
        });
        assertThat(only().findings()).contains(Finding.REVERSED_CREATION);assertThat(only().historicalLimit()).isTrue();
        var b=debt("100");core.settle(scope(),key(),new FinancialCore.Settlement(List.of(new FinancialCore.Part(b.position(),m("10"))),m("10"),m("0"),m("0")),"B1_EMISSOR");
        var rows=diagnosis.inspect(scope(),0,500).page().items();
        assertThat(rows.get(1).findings()).contains(Finding.UNDATED_CORE_EFFECT,Finding.DIFFERENCE_OBSERVED);
        assertThatThrownBy(()->queries.balanceAt(scope(),LocalDate.of(2026,2,1))).isInstanceOf(UnsupportedOperationException.class);
    }
    @Test void missingOriginsAndPendingNeverBecomeZeroBalances() {
        var missingOrigin=debt("100",false);var missingPending=debt("40");
        db.update("delete from pendente where id=?",missingPending.pending());
        var report=diagnosis.inspect(scope(),0,500);var rows=report.page().items();
        assertThat(rows.get(0).findings()).contains(Finding.MISSING_ORIGIN);assertThat(rows.get(0).coreResidual()).isNull();
        assertThat(rows.get(1).findings()).contains(Finding.MISSING_PENDING);assertThat(rows.get(1).legacyResidual()).isNull();
        assertThat(rows).allMatch(r->!r.comparable() && r.difference()==null);
        assertThat(report.summary().comparable()).isZero();assertThat(report.summary().comparableDifference()).isNull();
        assertThat(report.summary().fullComparisonCoverage()).isFalse();assertThat(missingOrigin.document()).isPositive();
    }
    @Test void unlinkedCorePositionsAreExplicitCoverageGaps() {
        core.create(scope(),key(),FinancialCore.Nature.DEBT,key(),"Generic",m("50"),"B1_EMISSOR");
        var report=diagnosis.inspect(scope(),0,500);
        assertThat(report.page().total()).isZero();assertThat(report.summary().unmappedCorePositions()).isEqualTo(1);
        assertThat(report.summary().comparableCore()).isNull();assertThat(report.summary().fullComparisonCoverage()).isFalse();
    }
    @Test void exactPrecisionAndUnknownCauseRemainExplicit() {
        var d=debt("100");db.update("update pendente set valor_pendente=99.999999 where id=?",d.pending());
        var row=only();assertThat(row.comparable()).isTrue();assertThat(row.difference()).isEqualByComparingTo("-0.000001");
        assertThat(row.explanation()).contains("causa não está demonstrada");
        db.update("update pendente set valor_documento=101 where id=?",d.pending());
        assertThat(only().findings()).contains(Finding.IDENTITY_MISMATCH);assertThat(only().difference()).isNull();
    }
    @Test void unsupportedTypesAndCurrenciesAreNotComparable() {
        var d=debt("100",false);
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('CND','ND',2,1,1,'ND') on conflict do nothing");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador) values('CND',?,'Teste',0)",serie);
        db.update("update documento_comercial set id_tipo_documento='CND' where id=?",d.document());
        assertThat(only().findings()).contains(Finding.UNSUPPORTED_DOCUMENT_TYPE);assertThat(only().difference()).isNull();
        db.update("insert into moeda(id,nome,simbolo,ndecimais) values('USD','Dollar','$',2) on conflict do nothing");
        db.update("update documento_comercial set id_moeda='USD' where id=?",d.document());
        var report=diagnosis.inspect(new FinancialCore.Scope(1,client,"USD"),0,10);
        assertThat(report.page().items().getFirst().findings()).contains(Finding.UNSUPPORTED_CURRENCY);
        assertThat(report.summary().comparable()).isZero();
    }
    @Test void stable501RowsGlobalSummaryAndBeyondLastPage() {
        var ids=new TransactionTemplate(tx).execute(status->{var result=new ArrayList<Long>();for(int i=0;i<501;i++) result.add(debt("1").document());return result;});
        for(int repeat=0;repeat<2;repeat++) {
            var first=diagnosis.inspect(scope(),0,500);var last=diagnosis.inspect(scope(),500,500);
            var actual=new ArrayList<Long>(first.page().items().stream().map(Entry::documentId).toList());
            actual.addAll(last.page().items().stream().map(Entry::documentId).toList());
            assertThat(actual).containsExactlyElementsOf(ids).doesNotHaveDuplicates();
            assertThat(first.summary().comparableCore()).isEqualByComparingTo("501");
            assertThat(last.summary()).isEqualTo(first.summary());
            var beyond=diagnosis.inspect(scope(),501,500);assertThat(beyond.page().items()).isEmpty();assertThat(beyond.summary()).isEqualTo(first.summary());
        }
    }
    @Test void clientCompanyAndEvidenceAreIsolatedAndInputsValidated() {
        var d=debt("100");legacy.create(request(d,"10"));
        var other=new FinancialCore.Scope(1,client+100000,"EUR");
        assertThat(diagnosis.inspect(other,0,10).page().total()).isZero();
        assertThat(diagnosis.receipts(other,d.document(),0,10).total()).isZero();
        for(var s:Arrays.asList(null,new FinancialCore.Scope(2,client,"EUR"),new FinancialCore.Scope(1,0,"EUR"),new FinancialCore.Scope(1,client,"eur"))) {
            assertThatThrownBy(()->diagnosis.inspect(s,0,10)).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(()->diagnosis.receipts(s,d.document(),0,10)).isInstanceOf(RuntimeException.class);
        }
        assertThatThrownBy(()->diagnosis.inspect(scope(),-1,10)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->diagnosis.inspect(scope(),0,501)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->diagnosis.receipts(scope(),0,0,10)).isInstanceOf(RuntimeException.class);
    }
    static class CountedJdbc extends JdbcTemplate {
        int reads;
        CountedJdbc(javax.sql.DataSource ds) { super(ds); }
        @Override public <T> T query(String sql,ResultSetExtractor<T> result,Object... args) {
            reads++;return super.query(sql,result,args);
        }
    }
    @Test void singleStatementPerPageAndDatabaseReadOnlyTransaction() {
        var d=debt("100");legacy.create(request(d,"10"));
        debt("50");
        var counted=new CountedJdbc(db.getDataSource());var service=new FinancialReconciliation(counted);
        var tables=List.of("documento_comercial","pendente","documento_financeiro","linha_documento_financeiro","financial_operation","financial_position","financial_allocation","financial_component","financial_receipt","financial_receipt_line","financial_receipt_reversal","serie","auditoria_evento");
        var before=tables.stream().map(t->db.queryForObject("select md5(coalesce(jsonb_agg(to_jsonb(x) order by to_jsonb(x)::text)::text,'')) from "+t+" x",String.class)).toList();
        var transaction=new TransactionTemplate(tx);transaction.setReadOnly(true);
        transaction.execute(status->{db.execute("SET TRANSACTION READ ONLY");service.inspect(scope(),0,10);service.receipts(scope(),d.document(),0,10);return null;});
        assertThat(counted.reads).isEqualTo(2);
        var after=tables.stream().map(t->db.queryForObject("select md5(coalesce(jsonb_agg(to_jsonb(x) order by to_jsonb(x)::text)::text,'')) from "+t+" x",String.class)).toList();
        assertThat(after).isEqualTo(before);
    }
    @Test void concurrentWriterCannotSplitSummaryFromPageSnapshot() throws Exception {
        var d=debt("100");var pool=Executors.newSingleThreadExecutor();
        try {
            var writer=pool.submit(()->{for(int i=0;i<30;i++) db.update("update pendente set valor_pendente=? where id=?",i%2==0?42:43,d.pending());});
            for(int i=0;i<20;i++) {
                var r=diagnosis.inspect(scope(),0,10);
                assertThat(r.summary().comparableLegacy()).isEqualByComparingTo(r.page().items().getFirst().legacyResidual());
                assertThat(r.summary().comparableDifference()).isEqualByComparingTo(r.page().items().getFirst().difference());
            }
            writer.get(20,TimeUnit.SECONDS);
        } finally {pool.shutdownNow();}
    }
    @Test void foreignReceiptIdentityIsNotExposedOrCountedAsComparable() {
        var own=debt("100");var owner=scope();
        fixture();var foreign=debt("100",false);var receipt=legacy.create(request(foreign,"10"));
        // Reachable SQL metadata inconsistency; do not disable any constraint/trigger.
        db.update("update linha_documento_financeiro set id_pendente=? where id_documento_financeiro=?",own.pending(),receipt.id());
        var row=diagnosis.inspect(owner,0,10).page().items().getFirst();
        assertThat(row.findings()).contains(Finding.IDENTITY_MISMATCH);assertThat(row.comparable()).isFalse();
        assertThat(row.difference()).isNull();assertThat(row.activeLegacyLines()).isZero();
        assertThat(diagnosis.receipts(owner,own.document(),0,10).total()).isZero();
    }
    @Test void restoredCoreReceiptAndLaterLegacyPaymentKeepBothContexts() {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        reverser.cancelUnreceived(scope(),key(),r.id(),"Payment did not occur","Verified by caller","B1_EMISSOR");
        legacy.create(request(d,"30"));var row=only();
        assertThat(row.findings()).contains(Finding.CORE_RECEIPT,Finding.ACTIVE_LEGACY_DOCUMENT,Finding.DIFFERENCE_OBSERVED);
        assertThat(row.explanation()).contains("legados activos e vínculos RC do núcleo");
        assertThat(row.consumed()).isEqualByComparingTo("30");assertThat(row.restored()).isEqualByComparingTo("30");
        assertThat(row.legacyResidual()).isEqualByComparingTo("70");assertThat(row.coreResidual()).isEqualByComparingTo("100");
    }

}
