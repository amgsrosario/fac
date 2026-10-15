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
    @Autowired FinancialReceiptReversal reverser;
    @Autowired FinancialReceiptProjections projections;
    @Autowired FinancialQueries queries;
    @Autowired DocumentoFinanceiroService legacy;
    @Autowired DocumentoComercialService commercial;
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
        })).isInstanceOf(RuntimeException.class).rootCause().hasMessageContaining("Cannot append to a receipt-bound financial operation").hasMessageContaining("financial_receipt_entry_guard()");
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

    @Test void coreThenLegacyIsRejectedBeforeNumberAndReplaySurvives() {
        var d=debt("100");String k=key();var receipt=issuer.issue(k,request(d,"30"));
        long docs=count("documento_financeiro");
        assertThatThrownBy(()->legacy.create(request(d,"10"))).isInstanceOf(RuntimeException.class).hasMessageContaining("active FinancialCore");
        assertThat(numerator()).isEqualTo(1);assertThat(count("documento_financeiro")).isEqualTo(docs);
        assertThat(issuer.issue(k,request(d,"30")).id()).isEqualTo(receipt.id());
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("70");
    }
    @Test void legacyThenCoreIsRejectedBeforeNumberWithoutInferringOwnershipFromOrigin() {
        var d=debt("100");legacy.create(request(d,"30"));long docs=count("documento_financeiro");
        assertThatThrownBy(()->issuer.issue(key(),request(d,"10"))).isInstanceOf(RuntimeException.class).hasMessageContaining("legacy");
        assertThat(numerator()).isEqualTo(1);assertThat(count("documento_financeiro")).isEqualTo(docs);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("100");
    }
    @Test void crossCircuitConcurrentReceiptsCommitExactlyOne() throws Exception {
        var d=debt("100");String another=key().substring(0,8);
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at) values('BRC',?,'Teste',0,'B1RC')",another);
        var dto=request(d,"30");var old=new DocumentoFinanceiroCreateDto("BRC",another,dto.dataEmissao(),client,"EUR","B1P",null,"B1_EMISSOR",null,dto.linhas());
        var results=concurrent(()->issuer.issue(key(),dto),()->legacy.create(old));
        assertThat(results.stream().filter(DocumentoFinanceiroDto.class::isInstance).count()).isEqualTo(1);
        assertThat(results.stream().filter(RuntimeException.class::isInstance).count()).isEqualTo(1);
        assertThat(db.queryForObject("select sum(numerador) from serie where id_tipo_documento='BRC' and serie in (?,?)",Long.class,serie,another)).isEqualTo(1);
    }
    @Test void oppositeDebtOrdersAcrossCircuitsCannotCommitBoth() throws Exception {
        var d=debt("100");var e=debt("100");String another=key().substring(0,8);
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at) values('BRC',?,'Teste',0,'B1RC')",another);
        var dto=request(LocalDate.of(2026,2,1),List.of(line(d,"20"),line(e,"30")));
        var old=new DocumentoFinanceiroCreateDto("BRC",another,dto.dataEmissao(),client,"EUR","B1P",null,"B1_EMISSOR",null,List.of(line(e,"30"),line(d,"20")));
        var results=concurrent(()->issuer.issue(key(),dto),()->legacy.create(old));
        assertThat(results.stream().filter(DocumentoFinanceiroDto.class::isInstance).count()).isEqualTo(1);
        assertThat(results.stream().filter(RuntimeException.class::isInstance).count()).isEqualTo(1);
        assertThat(results.toString()).doesNotContain("deadlock detected");
    }
    void insertRawLegacyLine(long parent,long sourceLine) {
        db.update("""
            insert into linha_documento_financeiro(id_documento_financeiro,numero_linha,id_pendente,
              valor_pendente_antes,valor_pagamento_bruto,valor_desconto_financeiro,valor_pagamento_liquido,
              data_documento,data_vencimento,id_tipo_documento,numero_documento,serie_documento,valor_documento,
              desconto_percentual,novo_valor_pendente,id_moeda)
            select ?,2,id_pendente,valor_pendente_antes,valor_pagamento_bruto,valor_desconto_financeiro,
              valor_pagamento_liquido,data_documento,data_vencimento,id_tipo_documento,numero_documento,
              serie_documento,valor_documento,desconto_percentual,novo_valor_pendente,id_moeda
            from linha_documento_financeiro where id=?
            """,parent,sourceLine);
    }
    @Test void rawLegacyInsertionCannotBypassEitherConstraintMode() {
        var d=debt("100");var e=debt("100");var old=legacy.create(request(e,"10"));var receipt=issuer.issue(key(),request(d,"10"));
        long lines=count("linha_documento_financeiro");
        for(int mode=0;mode<3;mode++) {
            final int timing=mode;
            assertThatThrownBy(()->new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
                verifyConstraintTiming(timing);insertRawLegacyLine(old.id(),receipt.linhas().getFirst().id());return null;
            })).isInstanceOf(RuntimeException.class).hasStackTraceContaining("Incompatible liquidation circuits");
            assertThat(count("linha_documento_financeiro")).isEqualTo(lines);
        }
    }
    @Test void rawCoreConsumptionAfterLegacyRollsBackAllLedgerWrites() {
        var d=debt("100");legacy.create(request(d,"10"));
        long ops=count("financial_operation"),allocs=count("financial_allocation"),components=count("financial_component");
        assertThatThrownBy(()->core.settle(scope(),key(),new FinancialCore.Settlement(List.of(new FinancialCore.Part(d.position(),m("10"))),m("10"),m("0"),m("0")),"B1_EMISSOR"))
            .isInstanceOf(RuntimeException.class).hasStackTraceContaining("Incompatible liquidation circuits");
        assertThat(count("financial_operation")).isEqualTo(ops);assertThat(count("financial_allocation")).isEqualTo(allocs);assertThat(count("financial_component")).isEqualTo(components);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("100");
    }
    @Test void rawLegacyLineReassignmentIsRejected() {
        var d=debt("100");var e=debt("100");var old=legacy.create(request(e,"10"));issuer.issue(key(),request(d,"10"));
        assertThatThrownBy(()->db.update("update linha_documento_financeiro set id_pendente=? where id=?",d.pending(),old.linhas().getFirst().id()))
            .isInstanceOf(RuntimeException.class).hasStackTraceContaining("Incompatible liquidation circuits");
        assertThat(db.queryForObject("select id_pendente from linha_documento_financeiro where id=?",Long.class,old.linhas().getFirst().id())).isEqualTo(e.pending());
    }

    @Test void directSqlCoreConsumptionCannotBypassDeferredOrImmediateChecks() {
        var d=debt("100");legacy.create(request(d,"10"));
        long ops=count("financial_operation"),allocs=count("financial_allocation"),components=count("financial_component");
        for(int mode=0;mode<3;mode++) {
            final int timing=mode;
            assertThatThrownBy(()->new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
                if(timing==2) verifyConstraintTiming(2);
                long op=db.queryForObject("""
                    insert into financial_operation(empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason)
                    values(1,?,'EUR','SETTLE',?,'raw','B1_EMISSOR','raw') returning id
                    """,Long.class,client,key());
                db.update("insert into financial_allocation(operation_id,position_id,empresa_id,cliente_id,moeda_id,amount,effect) values(?,?,1,?,'EUR',10,'CONSUME')",op,d.position(),client);
                db.update("update financial_position set available=available-10 where id=?",d.position());
                db.update("insert into financial_component(operation_id,kind,amount) values(?,'CASH_IN',10)",op);
                if(timing==1) db.execute("set constraints all immediate");
                return null;
            })).isInstanceOf(RuntimeException.class).hasStackTraceContaining("Incompatible liquidation circuits");
            assertThat(count("financial_operation")).isEqualTo(ops);assertThat(count("financial_allocation")).isEqualTo(allocs);assertThat(count("financial_component")).isEqualTo(components);
            assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("100");
        }
    }
    @Test void staleRepeatableReadCannotIssueCoreReceiptAfterLegacyCommit() throws Exception {
        var d=debt("100");var ready=new CountDownLatch(1);var legacyDone=new CountDownLatch(1);
        var pool=Executors.newSingleThreadExecutor();
        try {
            var future=pool.submit(()->{
                var transaction=new org.springframework.transaction.support.TransactionTemplate(tx);
                transaction.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
                return transaction.execute(status->{
                    db.queryForObject("select count(*) from pendente",Long.class);ready.countDown();
                    try {assertThat(legacyDone.await(15,TimeUnit.SECONDS)).isTrue();}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException(ex);}
                    return issuer.issue(key(),request(d,"10"));
                });
            });
            assertThat(ready.await(15,TimeUnit.SECONDS)).isTrue();
            legacy.create(request(d,"10"));legacyDone.countDown();
            assertThatThrownBy(()->future.get(20,TimeUnit.SECONDS)).hasStackTraceContaining("could not serialize access due to concurrent update");
            assertThat(numerator()).isEqualTo(1);assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("100");
        } finally {legacyDone.countDown();pool.shutdownNow();}
    }
    @Test void lateOriginCannotHideExistingMixedConsumption() {
        var d=debt("100",false);legacy.create(request(d,"10"));
        var p=core.create(scope(),key(),FinancialCore.Nature.DEBT,"DOCUMENTO_COMERCIAL:"+d.document(),"FT",m("100"),"B1_EMISSOR");
        core.settle(scope(),key(),new FinancialCore.Settlement(List.of(new FinancialCore.Part(p.id(),m("10"))),m("10"),m("0"),m("0")),"B1_EMISSOR");
        assertThatThrownBy(()->db.update("insert into financial_document_origin(document_id,position_id) values(?,?)",d.document(),p.id()))
            .isInstanceOf(RuntimeException.class).hasStackTraceContaining("Incompatible liquidation circuits");
        assertThat(db.queryForObject("select count(*) from financial_document_origin where document_id=?",Long.class,d.document())).isZero();
    }

    @Test void directLegacyReactivationCannotBypassCircuitExclusion() {
        var d=debt("100");var old=legacy.create(request(d,"10"));legacy.anular(old.id());
        issuer.issue(key(),request(d,"10"));
        assertThatThrownBy(()->db.update("update documento_financeiro set anulado=false where id=?",old.id()))
            .isInstanceOf(RuntimeException.class).hasStackTraceContaining("Incompatible liquidation circuits");
        assertThat(db.queryForObject("select anulado from documento_financeiro where id=?",Boolean.class,old.id())).isTrue();
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("90");
    }

    @Test void documentaryOriginWithoutSharedIdentityIsRejected() {
        var d=debt("100",false);db.update("delete from pendente where id=?",d.pending());
        var p=core.create(scope(),key(),FinancialCore.Nature.DEBT,"DOCUMENTO_COMERCIAL:"+d.document(),"FT",m("100"),"B1_EMISSOR");
        assertThatThrownBy(()->db.update("insert into financial_document_origin(document_id,position_id) values(?,?)",d.document(),p.id()))
            .isInstanceOf(RuntimeException.class).hasStackTraceContaining("Shared documentary debt identity is required");
    }
    @Test void mappedConsumptionWithoutSharedIdentityIsRejected() {
        var d=debt("100");db.update("delete from pendente where id=?",d.pending());long operations=count("financial_operation");
        assertThatThrownBy(()->core.settle(scope(),key(),new FinancialCore.Settlement(List.of(new FinancialCore.Part(d.position(),m("10"))),m("10"),m("0"),m("0")),"B1_EMISSOR"))
            .isInstanceOf(RuntimeException.class).hasStackTraceContaining("Shared documentary debt identity is required");
        assertThat(count("financial_operation")).isEqualTo(operations);assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("100");
    }

    @Test void staleRecreatedIdentityMustObserveMappedPosition() throws Exception {
        var d=debt("100");var ready=new CountDownLatch(1);var changed=new CountDownLatch(1);
        var pool=Executors.newSingleThreadExecutor();
        try {
            var future=pool.submit(()->{
                var transaction=new org.springframework.transaction.support.TransactionTemplate(tx);
                transaction.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
                return transaction.execute(status->{
                    db.queryForObject("select count(*) from pendente",Long.class);ready.countDown();
                    try {assertThat(changed.await(15,TimeUnit.SECONDS)).isTrue();}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException(ex);}
                    return db.update("""
                        insert into pendente(id_documento_comercial,id_cliente,id_tipo_documento,numero_documento,
                          serie_documento,valor_documento,valor_pendente,data_documento,data_vencimento,id_moeda)
                        select id_documento_comercial,id_cliente,id_tipo_documento,numero_documento,serie_documento,
                          valor_documento,valor_pendente,data_documento,data_vencimento,id_moeda from pendente where id=?
                        """,d.pending());
                });
            });
            assertThat(ready.await(15,TimeUnit.SECONDS)).isTrue();
            core.settle(scope(),key(),new FinancialCore.Settlement(List.of(new FinancialCore.Part(d.position(),m("10"))),m("10"),m("0"),m("0")),"B1_EMISSOR");
            db.update("delete from pendente where id=?",d.pending());changed.countDown();
            assertThatThrownBy(()->future.get(20,TimeUnit.SECONDS)).hasStackTraceContaining("could not serialize access due to concurrent update");
            assertThat(db.queryForObject("select count(*) from pendente where id_documento_comercial=?",Long.class,d.document())).isZero();
            assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("90");
        } finally {changed.countDown();pool.shutdownNow();}
    }

    void raceWrite(boolean allocation,Debt d,long position) {
        if(!allocation) {
            db.update("insert into financial_document_origin(document_id,position_id) values(?,?)",d.document(),position);
            return;
        }
        long op=db.queryForObject("""
            insert into financial_operation(empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason)
            values(1,?,'EUR','SETTLE',?,'race','B1_EMISSOR','race') returning id
            """,Long.class,client,key());
        db.update("insert into financial_allocation(operation_id,position_id,empresa_id,cliente_id,moeda_id,amount,effect) values(?,?,1,?,'EUR',10,'CONSUME')",op,position,client);
        db.update("update financial_position set available=available-10 where id=?",position);
        db.update("insert into financial_component(operation_id,kind,amount) values(?,'CASH_IN',10)",op);
    }
    void awaitPhase(CountDownLatch latch) {
        try {assertThat(latch.await(15,TimeUnit.SECONDS)).isTrue();}
        catch(InterruptedException e) {Thread.currentThread().interrupt();throw new IllegalStateException(e);}
    }
    List<Object> raceOriginAgainstAllocation(int isolation,boolean allocationFirst,boolean returnDeferred,Debt d,long position) throws Exception {
        var pool=Executors.newFixedThreadPool(2);var firstReady=new CountDownLatch(1);
        var secondStarted=new CountDownLatch(1);var secondReady=new CountDownLatch(1);var commit=new CountDownLatch(1);
        String secondName="b2-origin-race-"+key();
        java.util.function.BiFunction<Boolean,Boolean,Object> writer=(first,allocation)->{
            try {
                var transaction=new org.springframework.transaction.support.TransactionTemplate(tx);transaction.setIsolationLevel(isolation);
                return transaction.execute(status->{
                    if(!first) db.queryForObject("select set_config('application_name',?,true)",String.class,secondName);
                    db.queryForObject("select count(*) from financial_document_origin",Long.class); // Establish RR snapshot before the other commit.
                    if(!first) secondStarted.countDown();
                    raceWrite(allocation,d,position);
                    db.execute("set constraints all immediate");
                    if(returnDeferred) db.execute("set constraints all deferred");
                    (first?firstReady:secondReady).countDown();awaitPhase(commit);
                    return Boolean.TRUE;
                });
            } catch(RuntimeException ex) {return ex;}
            finally {if(first) firstReady.countDown();else secondStarted.countDown();}
        };
        try {
            var first=pool.submit(()->writer.apply(true,allocationFirst));awaitPhase(firstReady);
            var second=pool.submit(()->writer.apply(false,!allocationFirst));awaitPhase(secondStarted);
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);boolean waiting=false;
            while(secondReady.getCount()!=0 && !second.isDone() && System.nanoTime()<deadline) {
                waiting=Boolean.TRUE.equals(db.queryForObject("select exists(select 1 from pg_stat_activity where application_name=? and wait_event_type='Lock')",Boolean.class,secondName));
                if(waiting) break;Thread.sleep(10);
            }
            assertThat(waiting || secondReady.getCount()==0 || second.isDone()).as("second writer must either finish checks or wait on database lock").isTrue();
            commit.countDown();return List.of(first.get(15,TimeUnit.SECONDS),second.get(15,TimeUnit.SECONDS));
        } finally {commit.countDown();pool.shutdownNow();}
    }
    boolean mixedDebt(long pending) {
        return Boolean.TRUE.equals(db.queryForObject("""
            select exists(select 1 from pendente p join financial_document_origin o on o.document_id=p.id_documento_comercial
            join financial_allocation a on a.position_id=o.position_id where p.id=? and a.effect='CONSUME'
            and not exists(select 1 from financial_allocation r where r.reverse_of=a.id)
            and exists(select 1 from linha_documento_financeiro l join documento_financeiro f on f.id=l.id_documento_financeiro
            left join financial_receipt receipt on receipt.document_id=f.id where l.id_pendente=p.id and not f.anulado and receipt.document_id is null))
            """,Boolean.class,pending));
    }

    static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> originAllocationRaces() {
        var cases=new ArrayList<org.junit.jupiter.params.provider.Arguments>();
        for(int isolation:List.of(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED,org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ))
            for(boolean first:List.of(true,false)) for(boolean deferred:List.of(true,false))
                cases.add(org.junit.jupiter.params.provider.Arguments.of(isolation,first,deferred));
        return cases.stream();
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("originAllocationRaces")
    void concurrentOriginAndSqlAllocationCannotCommitMixedDebt(int isolation,boolean allocationFirst,boolean deferred) throws Exception {
        var d=debt("100",false);legacy.create(request(d,"30"));
        var p=core.create(scope(),key(),FinancialCore.Nature.DEBT,"DOCUMENTO_COMERCIAL:"+d.document(),"FT",m("100"),"B1_EMISSOR");
        var result=raceOriginAgainstAllocation(isolation,allocationFirst,deferred,d,p.id());
        assertThat(result.getFirst()).isEqualTo(Boolean.TRUE);
        assertThat(result.get(1)).isInstanceOf(RuntimeException.class);
        var exception=(RuntimeException)result.get(1);
        assertThat(exception).hasStackTraceContaining(isolation==org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ
                ? "could not serialize access due to concurrent update" : "Incompatible liquidation circuits");
        assertThat(mixedDebt(d.pending())).isFalse();
        assertThat(queries.position(scope(),p.id()).residual()).isEqualByComparingTo(allocationFirst?"90":"100");
        assertThat(db.queryForObject("select count(*) from financial_document_origin where position_id=?",Long.class,p.id())).isEqualTo(allocationFirst?0L:1L);
        assertThat(numerator()).isEqualTo(1);
    }
    Object verifyPendingBeforeResource(Debt d,Callable<?> action,String table,long resource) throws Exception {
        var pool=Executors.newSingleThreadExecutor();var started=new CountDownLatch(1);
        var future=new java.util.concurrent.atomic.AtomicReference<Future<?>>();String name="b2-lock-order-"+key();
        try {
            new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
                db.queryForList("select id from pendente where id=? for update",d.pending());
                future.set(pool.submit(()->new org.springframework.transaction.support.TransactionTemplate(tx).execute(worker->{
                    db.queryForObject("select set_config('application_name',?,true)",String.class,name);started.countDown();
                    try {return action.call();}catch(RuntimeException e){throw e;}catch(Exception e){throw new IllegalStateException(e);}
                })));
                awaitPhase(started);boolean waiting=false;long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
                while(!waiting && System.nanoTime()<deadline) {
                    waiting=Boolean.TRUE.equals(db.queryForObject("select exists(select 1 from pg_stat_activity where application_name=? and wait_event_type='Lock')",Boolean.class,name));
                    if(!waiting) try {Thread.sleep(10);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}
                }
                assertThat(waiting).isTrue();
                // If the worker acquired this resource before Pendente, NOWAIT detects the inversion.
                db.queryForList("select id from "+table+" where id=? for update nowait",resource);
                return null;
            });
            return future.get().get(15,TimeUnit.SECONDS);
        } finally {pool.shutdownNow();}
    }
    @Test void genericReverseLocksPendingBeforePositionAndPreservesHistory() throws Exception {
        var d=debt("100");long op=core.settle(scope(),key(),new FinancialCore.Settlement(List.of(new FinancialCore.Part(d.position(),m("10"))),m("10"),m("0"),m("0")),"B1_EMISSOR");
        long reversal=(Long)verifyPendingBeforeResource(d,()->core.reverse(scope(),key(),op,"Generic reversal","B1_EMISSOR"),"financial_position",d.position());
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("100");
        assertThat(db.queryForObject("select reverse_of from financial_operation where id=?",Long.class,reversal)).isEqualTo(op);
    }
    @Test void invoiceCancellationLocksPendingBeforeDocumentAndPosition() throws Exception {
        var d=debt("100");db.update("update documento_comercial set id_emissor='B1_EMISSOR' where id=?",d.document());
        verifyPendingBeforeResource(d,()->commercial.anular(d.document(),new AnularDocumentoRequest("Lock order regression")),"documento_comercial",d.document());
        assertThat(db.queryForObject("select estado from documento_comercial where id=?",String.class,d.document())).isEqualTo("ANULADO");
        assertThat(queries.position(scope(),d.position()).residual()).isZero();
        assertThat(db.queryForObject("select cancelled from financial_position where id=?",Boolean.class,d.position())).isTrue();
    }


    DocumentoFinanceiroCreateDto valueRequest(java.time.LocalDate value, List<LinhaDocumentoFinanceiroCreateDto> lines) {
        var original=request(LocalDate.of(2026,2,1),lines);
        return new DocumentoFinanceiroCreateDto(original.tipoDocumentoId(),original.serie(),original.dataEmissao(),
            original.clienteId(),original.moedaId(),original.mPagamentoId(),original.dataHoraOperacao(),
            original.emissorId(),original.observacoes(),original.linhas(),value);
    }
    @Test void valueDateDefaultsAndDoesNotChangeCoreEffectOrLegacyBalance() {
        var d=debt("100"); var receipt=legacy.create(request(d,"30"));
        assertThat(receipt.dataValor()).isEqualTo(receipt.dataEmissao());
        assertThat(core.position(scope(),d.position()).available()).isEqualByComparingTo("100");
        assertThat(db.queryForObject("select valor_pendente from pendente where id=?",BigDecimal.class,d.pending())).isEqualByComparingTo("70");
    }
    @Test void earlierValueDatePersistsAndDueDateIsIndependent() {
        var d=debt("100");var receipt=legacy.create(valueRequest(LocalDate.of(2026,1,10),List.of(line(d,"30"))));
        assertThat(receipt.dataValor()).isEqualTo(LocalDate.of(2026,1,10));
        assertThat(legacy.getById(receipt.id()).dataValor()).isEqualTo(receipt.dataValor());
    }
    @Test void multipleDebtsUseLatestOriginAndRejectBeforeNumber() {
        var a=debt("100");var b=debt("100");
        db.update("update documento_comercial set data_emissao='2026-01-15' where id=?",b.document());
        assertThatThrownBy(()->legacy.create(valueRequest(LocalDate.of(2026,1,14),List.of(line(a,"10"),line(b,"10"))))).isInstanceOf(RuntimeException.class);
        assertThat(numerator()).isZero();
        assertThat(legacy.create(valueRequest(LocalDate.of(2026,1,15),List.of(line(a,"10"),line(b,"10")))).linhas()).hasSize(2);
    }
    @Test void valueDateBeforeDebtOrAfterReceiptIsRejectedWithoutNumber() {
        var d=debt("100");
        for(var date:List.of(LocalDate.of(2025,12,31),LocalDate.of(2026,2,2)))
            assertThatThrownBy(()->legacy.create(valueRequest(date,List.of(line(d,"10"))))).isInstanceOf(RuntimeException.class);
        assertThat(numerator()).isZero();
    }
    @Test void creditorDebtIsRejectedWithoutNumber() {
        var d=debt("100",false);
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('VCN','Credor',2,1,2,'NC') on conflict do nothing");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador) values('VCN',?,'Teste',0)",serie);
        db.update("update documento_comercial set id_tipo_documento='VCN' where id=?",d.document());
        assertThatThrownBy(()->legacy.create(request(d,"10"))).isInstanceOf(RuntimeException.class);
        assertThat(numerator()).isZero();
    }
    @Test void nonFtDebitUsesSameValueDateRule() {
        var d=debt("100",false);
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('VDN','Debito',2,1,1,'ND') on conflict do nothing");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador) values('VDN',?,'Teste',0)",serie);
        db.update("update documento_comercial set id_tipo_documento='VDN' where id=?",d.document());
        assertThat(legacy.create(valueRequest(LocalDate.of(2026,1,10),List.of(line(d,"10")))).dataValor()).isEqualTo(LocalDate.of(2026,1,10));
    }
    @Test void emittedValueDateCannotBeEditedBySqlIncludingNull() {
        var d=debt("100");var r=legacy.create(request(d,"10"));
        assertThatThrownBy(()->db.update("update documento_financeiro set data_valor=null where id=?",r.id())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->db.update("update documento_financeiro set data_valor='2026-01-15' where id=?",r.id())).isInstanceOf(RuntimeException.class);
        assertThat(legacy.getById(r.id()).dataValor()).isEqualTo(r.dataEmissao());
    }
    @Test void internalValueDateReplayAndOperationalEffectRemainSeparate() {
        var d=debt("100");var k=key();var req=valueRequest(LocalDate.of(2026,1,10),List.of(line(d,"20")));
        var r=issuer.issue(k,req);assertThat(issuer.issue(k,req).id()).isEqualTo(r.id());
        long op=db.queryForObject("select operation_id from financial_receipt where document_id=?",Long.class,r.id());
        assertThat(queries.operation(scope(),op).financialEffectDate()).contains(r.dataValor());
        assertThatThrownBy(()->issuer.issue(k,valueRequest(LocalDate.of(2026,1,11),List.of(line(d,"20"))))).isInstanceOf(RuntimeException.class);
        assertThat(projections.inspectCutover().operationalActivationPermitted()).isFalse();
    }

    @Test void sqlLineWriterCannotBypassValueDateInEitherConstraintMode() {
        var a=debt("100",false);var b=debt("100",false);
        db.update("update documento_comercial set data_emissao='2026-01-20' where id=?",b.document());
        var r=legacy.create(valueRequest(LocalDate.of(2026,1,10),List.of(line(a,"10"))));
        for(boolean immediate:List.of(false,true)) {
            var transaction=new org.springframework.transaction.support.TransactionTemplate(tx);
            assertThatThrownBy(()->transaction.execute(status->{
                if(immediate) db.execute("SET CONSTRAINTS ALL IMMEDIATE");
                db.update("update linha_documento_financeiro set id_pendente=? where id_documento_financeiro=?",b.pending(),r.id());
                db.execute("SET CONSTRAINTS ALL IMMEDIATE");db.execute("SET CONSTRAINTS ALL DEFERRED");
                return null;
            })).isInstanceOf(RuntimeException.class);
        }
        assertThat(db.queryForObject("select id_pendente from linha_documento_financeiro where id_documento_financeiro=?",Long.class,r.id())).isEqualTo(a.pending());
    }
    @Test void sqlHeaderWriterRequiresValueDateAndUpperLimit() {
        var d=debt("100",false);var r=legacy.create(request(d,"10"));
        for(String value:List.of("NULL","'2026-02-02'::date"))
            assertThatThrownBy(()->db.update("""
                insert into documento_financeiro(id_cliente,id_tipo_documento,serie,numero_documento,data_emissao,id_moeda,
                  id_mpagamento,id_emissor,valor_pagamento_bruto,valor_pagamento_liquido,data_valor)
                select id_cliente,id_tipo_documento,serie,numero_documento+100,data_emissao,id_moeda,
                  id_mpagamento,id_emissor,valor_pagamento_bruto,valor_pagamento_liquido,
                """+value+" from documento_financeiro where id=?",r.id())).isInstanceOf(RuntimeException.class);
    }
    @Test void receiptPreventsChangingOriginChronologyOrNature() {
        var d=debt("100",false);legacy.create(request(d,"10"));
        assertThatThrownBy(()->db.update("update documento_comercial set data_emissao='2026-03-01' where id=?",d.document())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->db.update("update tipodocumento set sinal_contabilistico=2 where id='BFT'")).isInstanceOf(RuntimeException.class);
    }

    @Test void cancelledReceiptRemainsPrintableAfterSourceInvoiceCancellation() {
        var d=debt("100",false);var receipt=legacy.create(request(d,"10"));
        legacy.anular(receipt.id());
        db.update("update documento_comercial set anulado=true,estado='ANULADO' where id=?",d.document());
        db.update("update documento_financeiro set impresso=true where id=?",receipt.id());
        assertThat(db.queryForObject("select impresso from documento_financeiro where id=?",Boolean.class,receipt.id())).isTrue();
    }

    @Test void nonReceiptPreservesLegacyCreditorSettlementAndSqlUpdates() {
        var d=debt("100",false);
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('VNF','Financeiro nao RC',3,1,1,'XX') on conflict do nothing");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('VNF',?,'Teste',0,'VNFAT','2026-01-01')",serie);
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('VCR','Credor',2,1,2,'NC') on conflict do nothing");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador) values('VCR',?,'Teste',0)",serie);
        db.update("update documento_comercial set id_tipo_documento='VCR' where id=?",d.document());
        var req=valueRequest(LocalDate.of(2025,1,1),List.of(line(d,"10")));
        var nonRc=new DocumentoFinanceiroCreateDto("VNF",serie,req.dataEmissao(),client,"EUR","B1P",null,"B1_EMISSOR",null,req.linhas(),req.dataValor());
        var r=legacy.create(nonRc);
        assertThat(r.dataValor()).isNull();
        assertThat(db.queryForObject("select valor_pendente from pendente where id=?",BigDecimal.class,d.pending())).isEqualByComparingTo("90");
        db.update("update documento_financeiro set data_valor='2027-01-01' where id=?",r.id());
        db.update("update documento_financeiro set data_valor='2025-01-01',data_emissao='2025-02-01' where id=?",r.id());
        db.update("update linha_documento_financeiro set data_documento='2025-01-01' where id_documento_financeiro=?",r.id());
        db.update("update documento_comercial set data_emissao='2027-01-01' where id=?",d.document());
        assertThat(db.queryForObject("select data_valor::text from documento_financeiro where id=?",String.class,r.id())).isEqualTo("2025-01-01");
    }
    @Test void valuedReceiptPreventsFiscalReclassificationAndRetainsDateGuards() {
        var d=debt("100",false);var r=legacy.create(request(d,"10"));
        assertThatThrownBy(()->db.update("update tipodocumento set codigo_fiscal='XX' where id='BRC'"))
            .hasMessageContaining("Receipt fiscal classification is immutable");
        assertThat(db.queryForObject("select codigo_fiscal from tipodocumento where id='BRC'",String.class)).isEqualTo("RC");
        assertThatThrownBy(()->db.update("update documento_financeiro set data_valor=null where id=?",r.id())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->db.update("update documento_comercial set data_emissao='2026-03-01' where id=?",d.document())).isInstanceOf(RuntimeException.class);
        assertThat(legacy.getById(r.id()).dataValor()).isEqualTo(r.dataEmissao());
    }

    @Test void valuedNonReceiptCannotBecomeReceiptButUnrelatedReclassificationRemainsAllowed() {
        var d=debt("100",false);
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('N1F','Nao RC',3,1,2,'XX') on conflict do nothing");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('N1F',?,'Teste',0,'N1FAT','2026-01-01')",serie);
        var req=request(d,"10");
        var r=legacy.create(new DocumentoFinanceiroCreateDto("N1F",serie,req.dataEmissao(),client,"EUR","B1P",null,"B1_EMISSOR",null,req.linhas()));
        assertThat(r.dataValor()).isNull();
        // Non-RC SQL writes of this field remain legal: this is not an impossible RC fixture.
        db.update("update documento_financeiro set data_valor='2026-01-15' where id=?",r.id());
        assertThatThrownBy(()->db.update("update tipodocumento set codigo_fiscal='RC' where id='N1F'"))
            .hasMessageContaining("Receipt fiscal classification is immutable");
        db.update("update tipodocumento set codigo_fiscal='YY' where id='N1F'");
        assertThat(db.queryForObject("select codigo_fiscal from tipodocumento where id='N1F'",String.class)).isEqualTo("YY");
    }

    @Test void fiscalReclassificationWithoutValuedDocumentsRemainsAllowed() {
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('N1U','Sem documentos',3,1,2,'XX') on conflict do nothing");
        db.update("update tipodocumento set codigo_fiscal='RC' where id='N1U'");
        db.update("update tipodocumento set codigo_fiscal='XX' where id='N1U'");
        assertThat(db.queryForObject("select codigo_fiscal from tipodocumento where id='N1U'",String.class)).isEqualTo("XX");
    }

    long receiptOperation(long document) {
        return db.queryForObject("select operation_id from financial_receipt where document_id=?",Long.class,document);
    }
    FinancialReceiptReversal.Result cancelUnreceived(String k,long document) {
        return reverser.cancelUnreceived(scope(),k,document,"Incorrect receipt","Verified: payment did not occur","B1_EMISSOR");
    }
    @Test void f2EffectUsesValueDateAndTechnicalOperationRemainsSeparate() {
        var d=debt("100");var value=LocalDate.of(2026,1,10);
        var r=issuer.issue(key(),valueRequest(value,List.of(line(d,"30"))));
        var op=queries.operation(scope(),receiptOperation(r.id()));
        assertThat(op.financialEffectDate()).contains(value);
        assertThat(op.operationAt()).contains(db.queryForObject("select momento_emissao from documento_financeiro where id=?",java.sql.Timestamp.class,r.id()).toInstant());
        assertThat(op.financialDocument().orElseThrow().issueDate()).isEqualTo(LocalDate.of(2026,2,1));
        assertThat(op.financialDocument().orElseThrow().valueDate()).isEqualTo(value);
        assertThat(op.recordedAt().atOffset(java.time.ZoneOffset.UTC).toLocalDate()).isAfter(value);
        assertThat(queries.balanceAt(scope(),LocalDate.of(2025,12,31)).openDebt()).isZero();
        assertThat(queries.balanceAt(scope(),value.minusDays(1)).openDebt()).isEqualByComparingTo("100");
        assertThat(queries.balanceAt(scope(),value).openDebt()).isEqualByComparingTo("70");
        assertThat(queries.balanceAt(scope(),r.dataEmissao()).openDebt()).isEqualByComparingTo("70");
        assertThat(db.queryForObject("select valor_pendente from pendente where id=?",BigDecimal.class,d.pending())).isEqualByComparingTo("100");
        assertThat(projections.inspectCutover().operationalActivationPermitted()).isFalse();
    }
    @Test void t1ReversalRestoresOnlyOriginalAllocationsAndHasOriginalValueDate() {
        var a=debt("100");var b=debt("50");var date=LocalDate.of(2026,1,10);
        var r=issuer.issue(key(),valueRequest(date,List.of(line(a,"30"),line(b,"20"))));
        issuer.issue(key(),valueRequest(date.plusDays(1),List.of(line(a,"10"))));
        var result=cancelUnreceived(key(),r.id());
        var reverse=queries.operation(scope(),result.reversalOperationId());
        assertThat(reverse.financialEffectDate()).contains(date);
        assertThat(reverse.operationAt()).contains(db.queryForObject("select occurred_at from financial_receipt_reversal where document_id=?",java.sql.Timestamp.class,r.id()).toInstant());
        assertThat(reverse.financialDocument().orElseThrow().documentId()).isEqualTo(r.id());
        assertThat(reverse.recordedAt().atOffset(java.time.ZoneOffset.UTC).toLocalDate()).isAfter(date);
        assertThat(reverse.reverseOfOperationId()).isEqualTo(receiptOperation(r.id()));
        assertThat(reverse.components()).singleElement().satisfies(c->{
            assertThat(c.kind()).isEqualTo(FinancialQueries.ComponentKind.CASH_OUT);
            assertThat(c.reversal()).isTrue();assertThat(c.reverseOfComponentId()).isNotNull();
            assertThat(c.amount()).isEqualByComparingTo("50");
        });
        assertThat(queries.position(scope(),a.position()).residual()).isEqualByComparingTo("90");
        assertThat(queries.position(scope(),b.position()).residual()).isEqualByComparingTo("50");
        assertThat(queries.balanceAt(scope(),date).openDebt()).isEqualByComparingTo("150");
        assertThat(queries.balanceAt(scope(),date.plusDays(1)).openDebt()).isEqualByComparingTo("140");
        assertThat(db.queryForObject("select anulado from documento_financeiro where id=?",Boolean.class,r.id())).isTrue();
        assertThat(db.queryForObject("select count(*) from financial_allocation where operation_id=? and effect='RESTORE' and reverse_of is not null",Long.class,result.reversalOperationId())).isEqualTo(2);
        assertThatThrownBy(()->legacy.anular(r.id())).isInstanceOf(RuntimeException.class);
    }
    @Test void cancellationReplayAndDifferentRequestCannotDuplicateReversal() throws Exception {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));String k=key();
        var outcomes=concurrent(()->cancelUnreceived(k,r.id()),()->cancelUnreceived(k,r.id()));
        assertThat(outcomes).allMatch(FinancialReceiptReversal.Result.class::isInstance);
        assertThat(outcomes.get(0)).isEqualTo(outcomes.get(1));
        assertThat(cancelUnreceived(k,r.id())).isEqualTo(outcomes.get(0));
        assertThatThrownBy(()->cancelUnreceived(key(),r.id())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->reverser.cancelUnreceived(scope(),k,r.id(),"Changed","Different evidence","B1_EMISSOR")).isInstanceOf(RuntimeException.class);
        assertThat(db.queryForObject("select count(*) from financial_operation where reverse_of=?",Long.class,receiptOperation(r.id()))).isEqualTo(1);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("100");
    }
    @Test void competingCancellationKeysAllowExactlyOneAct() throws Exception {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        var outcomes=concurrent(()->cancelUnreceived(key(),r.id()),()->cancelUnreceived(key(),r.id()));
        assertThat(outcomes.stream().filter(FinancialReceiptReversal.Result.class::isInstance).count()).isEqualTo(1);
        assertThat(db.queryForObject("select count(*) from financial_receipt_reversal where document_id=?",Long.class,r.id())).isEqualTo(1);
    }
    @Test void dv09AtomicReplacementCorrectsAmountWithoutDoubleCashOrSettlement() {
        var d=debt("1000");var date=LocalDate.of(2026,1,10);
        var r=issuer.issue(key(),valueRequest(date,List.of(line(d,"30"))));String k=key();
        var replacement=valueRequest(date,List.of(line(d,"300")));
        var result=reverser.replace(scope(),k,r.id(),"Correct amount 30 to 300","Actual payment 300 verified","B1_EMISSOR",replacement);
        assertThat(reverser.replace(scope(),k,r.id(),"Correct amount 30 to 300","Actual payment 300 verified","B1_EMISSOR",replacement)).isEqualTo(result);
        assertThat(numerator()).isEqualTo(2);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("700");
        assertThat(queries.balanceAt(scope(),date).openDebt()).isEqualByComparingTo("700");
        assertThat(db.queryForObject("select sum(case c.kind when 'CASH_IN' then c.amount else -c.amount end) from financial_component c join financial_operation op on op.id=c.operation_id where op.cliente_id=?",BigDecimal.class,client)).isEqualByComparingTo("300");
        assertThat(db.queryForObject("select data_valor from documento_financeiro where id=?",LocalDate.class,result.replacementDocumentId())).isEqualTo(date);
    }
    @Test void failedReplacementRollsBackActReversalAndNumbering() {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        assertThatThrownBy(()->reverser.replace(scope(),key(),r.id(),"Correction","Verified amount","B1_EMISSOR",request(d,"101"))).isInstanceOf(RuntimeException.class);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("70");
        assertThat(numerator()).isEqualTo(1);
        assertThat(db.queryForObject("select anulado from documento_financeiro where id=?",Boolean.class,r.id())).isFalse();
        assertThat(db.queryForObject("select count(*) from financial_operation where reverse_of=?",Long.class,receiptOperation(r.id()))).isZero();
        assertThat(db.queryForObject("select count(*) from financial_receipt_reversal where document_id=?",Long.class,r.id())).isZero();
    }
    @Test void replacementLaterValueDateChangesOnlyRectifiedHistory() {
        var d=debt("100");var date=LocalDate.of(2026,1,10);
        var r=issuer.issue(key(),valueRequest(date,List.of(line(d,"30"))));
        reverser.replace(scope(),key(),r.id(),"Date correction","Payment verified on 12 January","B1_EMISSOR",valueRequest(date.plusDays(2),List.of(line(d,"30"))));
        assertThat(queries.balanceAt(scope(),date).openDebt()).isEqualByComparingTo("100");
        assertThat(queries.balanceAt(scope(),date.plusDays(2)).openDebt()).isEqualByComparingTo("70");
    }
    @Test void directSqlCannotCommitOrphanReversalInEitherConstraintMode() {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        for(boolean immediate:List.of(false,true)) {
            var transaction=new org.springframework.transaction.support.TransactionTemplate(tx);
            assertThatThrownBy(()->transaction.execute(status->{
                long op=db.queryForObject("insert into financial_operation(empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason,reverse_of) values(1,?,'EUR','REVERSE',?,'SQL','B1_EMISSOR','SQL',?) returning id",Long.class,client,key(),receiptOperation(r.id()));
                db.update("insert into financial_allocation(operation_id,position_id,empresa_id,cliente_id,moeda_id,amount,effect,reverse_of) select ?,position_id,empresa_id,cliente_id,moeda_id,amount,'RESTORE',id from financial_allocation where operation_id=?",op,receiptOperation(r.id()));
                db.update("update financial_position set available=available+30 where id=?",d.position());
                db.update("insert into financial_component(operation_id,kind,amount,reversed,reverse_of) select ?,'CASH_OUT',amount,true,id from financial_component where operation_id=?",op,receiptOperation(r.id()));
                if(immediate) { db.execute("SET CONSTRAINTS ALL IMMEDIATE");db.execute("SET CONSTRAINTS ALL DEFERRED"); }
                return null;
            })).isInstanceOf(RuntimeException.class);
        }
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("70");
        assertThat(db.queryForObject("select count(*) from financial_operation where reverse_of=?",Long.class,receiptOperation(r.id()))).isZero();
    }
    @Test void reversalActAndReferencesCannotBeForgedOrEditedBySql() {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        assertThatThrownBy(()->db.update("insert into financial_receipt_reversal(document_id,operation_id,money_treatment,request_payload,evidence,actor) values(?,?,'NOT_RECEIVED','fake','fake','B1_EMISSOR')",r.id(),receiptOperation(r.id()))).isInstanceOf(RuntimeException.class);
        var result=cancelUnreceived(key(),r.id());
        assertThatThrownBy(()->db.update("update financial_receipt_reversal set evidence='changed' where document_id=?",r.id())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->db.update("delete from financial_receipt_reversal where document_id=?",r.id())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->db.update("update documento_financeiro set anulado=false where id=?",r.id())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->db.update("update documento_financeiro set data_valor='2026-01-15' where id=?",r.id())).isInstanceOf(RuntimeException.class);
        assertThat(queries.operation(scope(),result.reversalOperationId()).financialEffectDate()).contains(r.dataValor());
    }
    @Test void cancellationRejectsMissingMoneyEvidenceAndWrongScope() {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        assertThatThrownBy(()->reverser.cancelUnreceived(scope(),key(),r.id(),"Reason"," ","B1_EMISSOR")).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->reverser.cancelUnreceived(new FinancialCore.Scope(1,client+10000,"EUR"),key(),r.id(),"Reason","Evidence","B1_EMISSOR")).isInstanceOf(RuntimeException.class);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("70");
    }

    @Test void reversalCannotBeExtendedAfterEarlyConstraintChecksAndRollbackIsIntegral() {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        assertThatThrownBy(()->new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
            var result=cancelUnreceived(key(),r.id());
            db.execute("SET CONSTRAINTS ALL IMMEDIATE");db.execute("SET CONSTRAINTS ALL DEFERRED");
            db.update("insert into financial_component(operation_id,kind,amount) values(?,'DISCOUNT',1)",result.reversalOperationId());
            return null;
        })).rootCause().hasMessageContaining("Cannot append to a receipt-bound financial operation");
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("70");
        assertThat(db.queryForObject("select anulado from documento_financeiro where id=?",Boolean.class,r.id())).isFalse();
    }
    @Test void concurrentCancellationAndIssuePreserveIndependentConsumption() throws Exception {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        var outcomes=concurrent(()->cancelUnreceived(key(),r.id()),()->issuer.issue(key(),request(d,"20")));
        assertThat(outcomes).noneMatch(Exception.class::isInstance);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("80");
        assertThat(queries.balanceAt(scope(),r.dataValor()).openDebt()).isEqualByComparingTo("80");
    }
    @Test void concurrentReplacementsWithOppositeLineOrderUseSameLockOrder() throws Exception {
        var a=debt("100");var b=debt("100");
        var first=issuer.issue(key(),request(LocalDate.of(2026,2,1),List.of(line(a,"10"),line(b,"10"))));
        var second=issuer.issue(key(),request(LocalDate.of(2026,2,1),List.of(line(b,"10"),line(a,"10"))));
        var outcomes=concurrent(
            ()->reverser.replace(scope(),key(),first.id(),"Correction","Verified 40","B1_EMISSOR",request(LocalDate.of(2026,2,1),List.of(line(a,"20"),line(b,"20")))),
            ()->reverser.replace(scope(),key(),second.id(),"Correction","Verified 40","B1_EMISSOR",request(LocalDate.of(2026,2,1),List.of(line(b,"20"),line(a,"20")))));
        assertThat(outcomes).allMatch(FinancialReceiptReversal.Result.class::isInstance);
        assertThat(queries.balance(scope()).openDebt()).isEqualByComparingTo("120");
        assertThat(numerator()).isEqualTo(4);
    }

    @Test void replacementOmittedValueDatePreservesOriginalAndReplaysExplicitEquivalent() {
        var d=debt("100");var date=LocalDate.of(2026,1,10);var k=key();
        var r=issuer.issue(key(),valueRequest(date,List.of(line(d,"30"))));
        var result=reverser.replace(scope(),k,r.id(),"Correction","Payment verified","B1_EMISSOR",request(d,"30"));
        assertThat(db.queryForObject("select data_valor from documento_financeiro where id=?",LocalDate.class,result.replacementDocumentId())).isEqualTo(date);
        assertThat(reverser.replace(scope(),k,r.id(),"Correction","Payment verified","B1_EMISSOR",valueRequest(date,List.of(line(d,"30"))))).isEqualTo(result);
        assertThat(numerator()).isEqualTo(2);
    }
    @Test void replacementCannotBeCancelledBeforeItsAtomicTransactionCommits() {
        var d=debt("100");var r=issuer.issue(key(),request(d,"30"));
        assertThatThrownBy(()->new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
            var result=reverser.replace(scope(),key(),r.id(),"Correction","Payment verified","B1_EMISSOR",request(d,"30"));
            db.execute("SET CONSTRAINTS ALL IMMEDIATE");db.execute("SET CONSTRAINTS ALL DEFERRED");
            cancelUnreceived(key(),result.replacementDocumentId());return null;
        })).rootCause().hasMessageContaining("Atomic replacement must leave an active substitute");
        assertThat(numerator()).isEqualTo(1);
        assertThat(queries.position(scope(),d.position()).residual()).isEqualByComparingTo("70");
    }

    @Test void historyRefusesCancelledInvoiceWithReversedCreation() {
        var d=debt("100");var date=LocalDate.of(2026,2,1);
        assertThat(queries.balanceAt(scope(),date).openDebt()).isEqualByComparingTo("100");
        long creation=queries.position(scope(),d.position()).creationOperationId();
        new org.springframework.transaction.support.TransactionTemplate(tx).execute(status->{
            core.reverse(scope(),key(),creation,"Existing FT cancellation","B1_EMISSOR");
            db.update("update documento_comercial set anulado=true,estado='ANULADO' where id=?",d.document());
            return null;
        });
        assertThat(queries.position(scope(),d.position()).state()).isEqualTo(FinancialQueries.State.CANCELLED);
        assertThatThrownBy(()->queries.balanceAt(scope(),date))
            .isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("TUULI-FIN-TIME-001");
    }
    @Test void historyRefusesGenericSettlementEvenWhenInvoiceOriginHasKnownDate() {
        var d=debt("100");var date=LocalDate.of(2026,2,1);
        assertThat(queries.balanceAt(scope(),date).openDebt()).isEqualByComparingTo("100");
        long op=core.settle(scope(),key(),new FinancialCore.Settlement(List.of(new FinancialCore.Part(d.position(),m("10"))),m("10"),m("0"),m("0")),"B1_EMISSOR");
        assertThat(queries.position(scope(),d.position()).financialEffectDate()).contains(LocalDate.of(2026,1,1));
        assertThat(queries.operation(scope(),op).financialEffectDate()).isEmpty();
        assertThatThrownBy(()->queries.balanceAt(scope(),date))
            .isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("TUULI-FIN-TIME-001");
    }

}
