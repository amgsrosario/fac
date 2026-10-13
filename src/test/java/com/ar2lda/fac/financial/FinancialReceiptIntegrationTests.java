package com.ar2lda.fac.financial;

import com.ar2lda.fac.controller.dto.*;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.UtilizadorRepository;
import com.ar2lda.fac.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Real committed PostgreSQL operations; synthetic persisted FT fixtures, no mocks of JDBC/ledger. */
@SpringBootTest
class FinancialReceiptIntegrationTests {
    @Autowired FinancialReceiptIssuer issuer;
    @Autowired FinancialReceiptProjections projections;
    @Autowired FinancialQueries queries;
    @Autowired DocumentoFinanceiroService legacy;
    @Autowired JdbcTemplate db;
    @Autowired org.springframework.transaction.PlatformTransactionManager tx;
    @Autowired UtilizadorRepository users;
    @SpyBean FinancialCore core;
    @SpyBean AtcudService atcud;
    @SpyBean FiscalQrService qr;
    @SpyBean AuditoriaService audit;
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
    long count(String table) { return db.queryForObject("select count(*) from "+table,Long.class); }
    long numerator() { return db.queryForObject("select numerador from serie where id_tipo_documento='BRC' and serie=?",Long.class,serie); }
    @Test void partialMultipleSuccessiveReceiptsAndDocumentaryEffect() {
        var a=debt("100");var b=debt("50");
        var receipt=issuer.issue(key(),request(LocalDate.of(2026,2,1),List.of(line(b,"20"),line(a,"30"))));
        assertThat(receipt.valorPagamentoLiquido()).isEqualByComparingTo("50");
        assertThat(receipt.atcud()).isEqualTo("B1RC-1");
        assertThat(receipt.linhas()).extracting(LinhaDocumentoFinanceiroDto::pendenteId).containsExactly(b.pending(),a.pending());
        assertThat(queries.position(scope(),a.position()).residual()).isEqualByComparingTo("70");
        assertThat(queries.position(scope(),b.position()).residual()).isEqualByComparingTo("30");
        long op=db.queryForObject("select operation_id from financial_receipt where document_id=?",Long.class,receipt.id());
        assertThat(db.queryForObject("select count(*) from financial_receipt_line l join linha_documento_financeiro d on d.id=l.line_id where d.id_documento_financeiro=?",Long.class,receipt.id())).isEqualTo(2);
        assertThat(db.queryForObject("select created_at from financial_operation where id=?",java.sql.Timestamp.class,op)).isNotNull();
        assertThat(queries.operation(scope(),op).financialEffectDate()).contains(receipt.dataEmissao());
        assertThat(queries.operation(scope(),op).financialDocument().orElseThrow().documentId()).isEqualTo(receipt.id());
        issuer.issue(key(),request(a,"70"));
        assertThat(queries.position(scope(),a.position()).residual()).isZero();
        assertThat(db.queryForObject("select valor_pendente from pendente where id=?",BigDecimal.class,a.pending())).isEqualByComparingTo("100");
        var page=projections.debts(scope(),FinancialQueries.Selection.ALL,0,500);
        assertThat(page.items().stream().map(PendenteDto::valorPendente).reduce(BigDecimal.ZERO,BigDecimal::add)).isEqualByComparingTo(projections.totals(scope()).openDebt());
        assertThat(projections.debts(scope(),FinancialQueries.Selection.OPEN,0,1).total()).isEqualTo(1);
        assertThat(projections.inspectCutover().operationalActivationPermitted()).isFalse();
        // Linked receipt cancellation cannot run the legacy snapshot-restoration path.
        assertThatThrownBy(()->db.update("update documento_financeiro set anulado=true where id=?",receipt.id())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->core.reverse(scope(),key(),op,"Unapproved reversal","B1_EMISSOR")).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->db.update("update linha_documento_financeiro set valor_pagamento_bruto=1 where id=?",receipt.linhas().getFirst().id())).isInstanceOf(RuntimeException.class);
    }
    @Test void requestReplayDifferentPayloadContextAndUniqueBindings() {
        var d=debt("100");String k=key();var first=issuer.issue(k,request(d,"10"));
        assertThat(issuer.issue(k,request(d,"10.000000")).id()).isEqualTo(first.id());
        assertThat(numerator()).isEqualTo(1);
        assertThatThrownBy(()->issuer.issue(k,request(d,"11"))).isInstanceOf(RuntimeException.class);
        var dto=request(d,"10");
        var other=new DocumentoFinanceiroCreateDto(dto.tipoDocumentoId(),dto.serie(),dto.dataEmissao(),client+10000,dto.moedaId(),dto.mPagamentoId(),null,dto.emissorId(),null,dto.linhas());
        assertThatThrownBy(()->issuer.issue(k,other)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->db.update("insert into financial_receipt select document_id,operation_id,empresa_id,cliente_id,moeda_id,'other',request_payload from financial_receipt where document_id=?",first.id())).isInstanceOf(RuntimeException.class);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("90");
    }
    List<Object> concurrent(Callable<?> one,Callable<?> two) throws Exception {
        var pool=Executors.newFixedThreadPool(2);var barrier=new CyclicBarrier(2);
        try {var futures=new ArrayList<Future<Object>>();for(var action:List.of(one,two)) futures.add(pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);try{return action.call();}catch(Exception e){return e;}}));
            var result=new ArrayList<Object>();for(var f:futures) result.add(f.get(30,TimeUnit.SECONDS));return result;
        } finally {pool.shutdownNow();}
    }
    @Test void concurrentSameRequestCreatesOneReceipt() throws Exception {
        var d=debt("100");String k=key();var dto=request(d,"40");var results=concurrent(()->issuer.issue(k,dto),()->issuer.issue(k,dto));
        assertThat(results).allMatch(DocumentoFinanceiroDto.class::isInstance);
        assertThat(((DocumentoFinanceiroDto)results.getFirst()).id()).isEqualTo(((DocumentoFinanceiroDto)results.get(1)).id());
        assertThat(numerator()).isEqualTo(1);assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("60");
    }
    @Test void concurrentDifferentSeriesCannotOverconsume() throws Exception {
        var d=debt("100");var e=debt("100");String another=key().substring(0,8);
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at) values('BRC',?,'Teste',0,'B1RC')",another);
        var dto=request(LocalDate.of(2026,2,1),List.of(line(d,"70"),line(e,"70")));
        var second=new DocumentoFinanceiroCreateDto("BRC",another,dto.dataEmissao(),client,"EUR","B1P",null,"B1_EMISSOR",null,List.of(line(e,"70"),line(d,"70")));
        var results=concurrent(()->issuer.issue(key(),dto),()->issuer.issue(key(),second));
        assertThat(results.stream().filter(DocumentoFinanceiroDto.class::isInstance).count()).isEqualTo(1);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("30");
        assertThat(queries.position(scope(),e.position()).residual()).isEqualByComparingTo("30");
    }
    @Test void excessCentsDiscountDuplicatesAndIsolationAreRejectedWithoutNumber() {
        var d=debt("10");
        for(var amount:List.of("10.01","0.001")) assertThatThrownBy(()->issuer.issue(key(),request(d,amount))).isInstanceOf(RuntimeException.class);
        for(var discounted:List.of(new LinhaDocumentoFinanceiroCreateDto(d.pending(),m("1"),m("1"),m("0")),new LinhaDocumentoFinanceiroCreateDto(d.pending(),m("1"),m("0"),m("0.01"))))
            assertThatThrownBy(()->issuer.issue(key(),request(LocalDate.of(2026,2,1),List.of(discounted)))).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->issuer.issue(key(),request(LocalDate.of(2026,2,1),List.of(line(d,"1"),line(d,"1"))))).isInstanceOf(RuntimeException.class);
        var dto=request(d,"1");var wrong=new DocumentoFinanceiroCreateDto("BRC",serie,dto.dataEmissao(),client+100000,"EUR","B1P",null,"B1_EMISSOR",null,dto.linhas());
        assertThatThrownBy(()->issuer.issue(key(),wrong)).isInstanceOf(RuntimeException.class);
        assertThat(projections.debts(new FinancialCore.Scope(1,client+100000,"EUR"),FinancialQueries.Selection.ALL,0,500).items()).isEmpty();
        assertThat(numerator()).isZero();assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("10");
    }
    @Test void protectedSeriesChronologyRollsBackReservation() {
        var d=debt("10");issuer.issue(key(),request(LocalDate.of(2026,3,1),List.of(line(d,"1"))));
        assertThatThrownBy(()->issuer.issue(key(),request(d,"1"))).isInstanceOf(RuntimeException.class);
        assertThat(numerator()).isEqualTo(1);assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("9");
    }
    @Test void failureAfterNumberRestoresNumeratorAndLeavesNoReceipt() {
        var d=debt("10");long docs=count("documento_financeiro"),ops=count("financial_operation");
        doThrow(new IllegalStateException("after number")).when(atcud).gerar(anyString(),anyLong());
        assertThatThrownBy(()->issuer.issue(key(),request(d,"1"))).isInstanceOf(RuntimeException.class);
        assertThat(numerator()).isZero();assertThat(count("documento_financeiro")).isEqualTo(docs);assertThat(count("financial_operation")).isEqualTo(ops);
    }
    @Test void failureAfterActualSettlementRollsBackDocumentLedgerAndRetry() {
        var d=debt("10");String k=key();long docs=count("documento_financeiro"),ops=count("financial_operation"),allocs=count("financial_allocation");
        doAnswer(invocation->{invocation.callRealMethod();throw new IllegalStateException("after real SETTLE");}).when(core).settle(any(),anyString(),any(),anyString());
        assertThatThrownBy(()->issuer.issue(k,request(d,"1"))).isInstanceOf(RuntimeException.class);
        assertThat(numerator()).isZero();assertThat(count("documento_financeiro")).isEqualTo(docs);assertThat(count("financial_operation")).isEqualTo(ops);assertThat(count("financial_allocation")).isEqualTo(allocs);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("10");
        doCallRealMethod().when(core).settle(any(),anyString(),any(),anyString());
        assertThat(issuer.issue(k,request(d,"1")).numeroDocumento()).isEqualTo(1);
    }
    @Test void failureDuringQrRollsBackLinesAndBalances() {
        var d=debt("10");long links=count("financial_receipt"),lines=count("linha_documento_financeiro");
        doThrow(new IllegalStateException("QR failure")).when(qr).buildDocumentoFinanceiro(any());
        assertThatThrownBy(()->issuer.issue(key(),request(d,"1"))).isInstanceOf(RuntimeException.class);
        assertThat(count("financial_receipt")).isEqualTo(links);assertThat(count("linha_documento_financeiro")).isEqualTo(lines);
        assertThat(numerator()).isZero();assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("10");
    }
    @Test void unreconciledLegacyReceiptIsBlockedAndLegacyCancellationStillWorks() {
        var d=debt("100");var old=legacy.create(request(d,"40"));
        assertThat(db.queryForObject("select valor_pendente from pendente where id=?",BigDecimal.class,d.pending())).isEqualByComparingTo("60");
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("100");
        assertThatThrownBy(()->issuer.issue(key(),request(d,"10"))).isInstanceOf(RuntimeException.class).hasMessageContaining("legacy");
        assertThat(projections.inspectCutover().activeLegacyReceipts()).isGreaterThan(0);
        legacy.anular(old.id());
        assertThat(db.queryForObject("select valor_pendente from pendente where id=?",BigDecimal.class,d.pending())).isEqualByComparingTo("100");
        assertThat(issuer.issue(key(),request(d,"10")).valorPagamentoLiquido()).isEqualByComparingTo("10");
    }
    @Test void missingOriginIsNeverImportedByReceiptReadOrWrite() {
        var d=debt("10",false);long positions=count("financial_position");
        assertThatThrownBy(()->issuer.issue(key(),request(d,"1"))).isInstanceOf(RuntimeException.class).hasMessageContaining("no FinancialCore origin");
        assertThat(count("financial_position")).isEqualTo(positions);
        assertThat(projections.inspectCutover().missingOrigins()).isGreaterThan(0);
        assertThat(projections.debts(scope(),FinancialQueries.Selection.ALL,0,500).items()).isEmpty();
    }
    @Test void existingOtherCustomerAndActorCannotReuseRequestContext() {
        var d=debt("10");String k=key();issuer.issue(k,request(d,"1"));
        users.save(new Utilizador("B1_OTHER","Outro","b1-other@fac.test","$2a$10$123456789012345678901uZVXKMcIlbsK8PL5kN1l4I2zxIlW85ee",false));
        var req=request(d,"1");var actor=new DocumentoFinanceiroCreateDto("BRC",serie,req.dataEmissao(),client,"EUR","B1P",null,"B1_OTHER",null,req.linhas());
        assertThatThrownBy(()->issuer.issue(k,actor)).isInstanceOf(RuntimeException.class).hasMessageContaining("context");
        long other=db.queryForObject("insert into cliente(nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values('Outro','Rua','1000-020',?,'EUR','CON','B1T','PT') returning id",Long.class,key().substring(0,20));
        var wrong=new DocumentoFinanceiroCreateDto("BRC",serie,req.dataEmissao(),other,"EUR","B1P",null,"B1_EMISSOR",null,req.linhas());
        assertThatThrownBy(()->issuer.issue(key(),wrong)).isInstanceOf(RuntimeException.class).hasMessageContaining("scope");
        assertThat(numerator()).isEqualTo(1);
    }
    @Test void failureAfterBindingsAndAuditRollsBackEverything() {
        var d=debt("10");long trace=count("financial_receipt_line"),links=count("financial_receipt"),events=count("auditoria_evento");
        doThrow(new IllegalStateException("audit failure")).when(audit).registarComo(any(),anyString(),anyLong(),any(),any(),anyString(),anyString(),anyString());
        assertThatThrownBy(()->issuer.issue(key(),request(d,"1"))).isInstanceOf(RuntimeException.class);
        assertThat(count("financial_receipt_line")).isEqualTo(trace);assertThat(count("financial_receipt")).isEqualTo(links);
        assertThat(count("auditoria_evento")).isEqualTo(events);assertThat(numerator()).isZero();
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("10");
    }

    @Test void immediateConstraintsCannotPermitLateUnlinkedLineInSameTransaction() {
        var d=debt("10");long receipts=count("financial_receipt");
        assertThatThrownBy(()->new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
            var receipt=issuer.issue(key(),request(d,"1"));
            db.execute("set constraints all immediate");
            db.update("""
                insert into linha_documento_financeiro(id_documento_financeiro,numero_linha,id_pendente,
                  valor_pendente_antes,valor_pagamento_bruto,valor_desconto_financeiro,valor_pagamento_liquido,
                  data_documento,data_vencimento,id_tipo_documento,numero_documento,serie_documento,valor_documento,
                  desconto_percentual,novo_valor_pendente,id_moeda)
                select id_documento_financeiro,numero_linha+1,id_pendente,valor_pendente_antes,valor_pagamento_bruto,
                  valor_desconto_financeiro,valor_pagamento_liquido,data_documento,data_vencimento,id_tipo_documento,
                  numero_documento,serie_documento,valor_documento,desconto_percentual,novo_valor_pendente,id_moeda
                from linha_documento_financeiro where id=?
                """,receipt.linhas().getFirst().id());
            return null;
        })).isInstanceOf(RuntimeException.class);
        assertThat(count("financial_receipt")).isEqualTo(receipts);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("10");
    }
    @Test void requestObservationsAreImmutableButPrintMarkerRemainsMutable() {
        var d=debt("10");var receipt=issuer.issue(key(),request(d,"1"));
        assertThatThrownBy(()->db.update("update documento_financeiro set observacoes='tampered' where id=?",receipt.id())).isInstanceOf(RuntimeException.class);
        db.update("update documento_financeiro set impresso=true where id=?",receipt.id());
        assertThat(db.queryForObject("select impresso from documento_financeiro where id=?",Boolean.class,receipt.id())).isTrue();
    }

    @Test void financialDebtLinkDoesNotAuthoriseOtherDocumentTypes() {
        var d=debt("10");
        db.update("update documento_comercial set tipo_documento_codigo_fiscal='ND' where id=?",d.document());
        assertThatThrownBy(()->issuer.issue(key(),request(d,"1"))).isInstanceOf(RuntimeException.class).hasMessageContaining("Only FT");
        assertThat(numerator()).isZero();assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("10");
    }


    void verifyConstraintTiming(int mode) {
        if(mode>0) db.execute("set constraints all immediate");
        if(mode==2) db.execute("set constraints all deferred");
    }
    void assertSealedRollback(Debt d,int mode,java.util.function.LongConsumer append) {
        String[] tables={"documento_financeiro","linha_documento_financeiro","financial_operation",
            "financial_allocation","financial_component","financial_receipt","financial_receipt_line","auditoria_evento"};
        long[] before=Arrays.stream(tables).mapToLong(this::count).toArray();
        long number=numerator();var residual=queries.position(scope(),d.position()).residual();
        assertThatThrownBy(()->new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
            var receipt=issuer.issue(key(),request(d,"10"));
            long op=db.queryForObject("select operation_id from financial_receipt where document_id=?",Long.class,receipt.id());
            verifyConstraintTiming(mode);append.accept(op);return null;
        })).isInstanceOf(RuntimeException.class).hasRootCauseMessage("ERROR: Cannot append to a receipt-bound financial operation\n  Where: PL/pgSQL function financial_receipt_entry_guard() line 4 at RAISE");
        for(int i=0;i<tables.length;i++) assertThat(count(tables[i])).as(tables[i]).isEqualTo(before[i]);
        assertThat(numerator()).isEqualTo(number);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo(residual);
    }
    @Test void consumedDeferredChecksCannotPermitForbiddenBalancedPair() {
        var d=debt("20");
        assertSealedRollback(d,2,op->db.update("insert into financial_component(operation_id,kind,amount) values(?,'CASH_OUT',1),(?,'DISCOUNT',1)",op,op));
    }
    @Test void cashOutAfterReceiptBindingIsAlwaysRejected() {
        var d=debt("20");
        for(int mode=0;mode<3;mode++) assertSealedRollback(d,mode,op->db.update("insert into financial_component(operation_id,kind,amount) values(?,'CASH_OUT',1)",op));
    }
    @Test void discountAfterReceiptBindingIsAlwaysRejected() {
        var d=debt("20");
        for(int mode=0;mode<3;mode++) assertSealedRollback(d,mode,op->db.update("insert into financial_component(operation_id,kind,amount) values(?,'DISCOUNT',1)",op));
    }
    @Test void incompatibleAllocationAfterReceiptBindingIsAlwaysRejected() {
        var d=debt("20");var other=debt("20");
        for(int mode=0;mode<3;mode++) assertSealedRollback(d,mode,op->db.update("""
            insert into financial_allocation(operation_id,position_id,empresa_id,cliente_id,moeda_id,amount,effect)
            values(?,?,1,?,'EUR',1,'CONSUME')
            """,op,other.position(),client));
        assertThat(queries.position(scope(),other.position()).residual()).isEqualByComparingTo("20");
    }
    @Test void validReceiptCommitsAfterImmediateAndDeferredChecks() {
        var d=debt("20");
        long op=new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
            var receipt=issuer.issue(key(),request(d,"10"));verifyConstraintTiming(2);
            return db.queryForObject("select operation_id from financial_receipt where document_id=?",Long.class,receipt.id());
        });
        assertThat(db.queryForList("select kind from financial_component where operation_id=?",String.class,op)).containsExactly("CASH_IN");
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("10");
    }
    @Test void genericSettlementStillPermitsBalancedComponentsWithinCreatingTransaction() {
        var d=debt("20");
        long op=new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
            long id=core.settle(scope(),key(),new FinancialCore.Settlement(List.of(new FinancialCore.Part(d.position(),m("10"))),m("10"),m("0"),m("0")),"B1_EMISSOR");
            verifyConstraintTiming(2);
            db.update("insert into financial_component(operation_id,kind,amount) values(?,'CASH_OUT',1),(?,'DISCOUNT',1)",id,id);
            return id;
        });
        assertThat(db.queryForList("select kind from financial_component where operation_id=? order by kind",String.class,op)).containsExactly("CASH_IN","CASH_OUT","DISCOUNT");
        assertThat(db.queryForObject("select count(*) from financial_receipt where operation_id=?",Long.class,op)).isZero();
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("10");
    }
    @Test void forbiddenComponentsBeforeReceiptBindingStillFailDeferredValidation() {
        var d=debt("20");long receipts=count("financial_receipt"),number=numerator();
        doAnswer(invocation->{
            long op=(Long)invocation.callRealMethod();
            db.update("insert into financial_component(operation_id,kind,amount) values(?,'CASH_OUT',1),(?,'DISCOUNT',1)",op,op);
            return op;
        }).when(core).settle(any(),anyString(),any(),anyString());
        assertThatThrownBy(()->issuer.issue(key(),request(d,"10"))).isInstanceOf(RuntimeException.class).hasStackTraceContaining("Incomplete financial receipt");
        assertThat(count("financial_receipt")).isEqualTo(receipts);assertThat(numerator()).isEqualTo(number);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("20");
    }

}
