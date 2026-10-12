package com.ar2lda.fac.financial;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.ar2lda.fac.financial.FinancialCore.*;
import static com.ar2lda.fac.financial.FinancialQueries.*;

@SpringBootTest
class FinancialQueriesIntegrationTests {
    @Autowired FinancialCore core;
    @Autowired FinancialQueries queries;
    @Autowired JdbcTemplate db;
    @Autowired PlatformTransactionManager tx;
    Scope scope;
    String key() { return UUID.randomUUID().toString(); }
    static BigDecimal m(String amount) { return new BigDecimal(amount); }
    @BeforeEach void fixture() {
        db.update("insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('F20','Financial fixture') on conflict do nothing");
        db.update("insert into empresa(id,nome,nif,morada,id_codpostal,localidade,id_pais,capital_social,matricula_registo_comercial,cae,descricao_cae,email) values(1,'Teste','999999990','Rua','1000-020','Lisboa','PT',0,'Registo','00000','Teste','op22@fac.test') on conflict do nothing");
        long client=db.queryForObject("insert into cliente(nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values('OP22','Rua','1000-020',?,'EUR','CON','F20','PT') returning id",Long.class,key().substring(0,20));
        scope=new Scope(1,client,"EUR");
    }
    Position pos(Nature nature,String amount) { return core.create(scope,key(),nature,key(),"Fixture",m(amount),"test"); }
    long compensate(Position debt,Position credit,String amount) {
        return core.settle(scope,key(),new Settlement(List.of(new Part(debt.id(),m(amount)),new Part(credit.id(),m(amount))),m("0"),m("0"),m("0")),"test");
    }
    @Test void distinctNaturesZeroNetStillOpenAndEmptyScopes() {
        var d=pos(Nature.DEBT,"1000.01"); var c=pos(Nature.CREDIT,"1000.01");
        var b=queries.balance(scope);
        assertThat(b.openDebt()).isEqualByComparingTo("1000.01");
        assertThat(b.availableCredit()).isEqualByComparingTo("1000.01");
        assertThat(b.netDebt()).isZero(); assertThat(b.openPositions()).isEqualTo(2); assertThat(b.closedPositions()).isZero();
        assertThat(queries.positions(scope,Selection.OPEN,Nature.DEBT,0,10).items()).extracting(PositionView::id).containsExactly(d.id());
        assertThat(queries.positions(scope,Selection.OPEN,Nature.CREDIT,0,10).items()).extracting(PositionView::id).containsExactly(c.id());
        assertThat(queries.position(scope,d.id()).state()).isEqualTo(State.OPEN);
        assertThat(queries.balance(new Scope(1,scope.cliente()+100000,"EUR")).openDebt()).isZero();
    }
    @Test void partialMultipleAllocationsExplicitCompensationAndAuditReversal() {
        var d=pos(Nature.DEBT,"1000"); var c=pos(Nature.CREDIT,"800");
        long first=compensate(d,c,"300"); compensate(d,c,"200");
        assertThat(queries.balance(scope).netDebt()).isEqualByComparingTo("200");
        assertThat(queries.position(scope,d.id()).residual()).isEqualByComparingTo("500");
        assertThat(queries.position(scope,c.id()).residual()).isEqualByComparingTo("300");
        long reverse=core.reverse(scope,key(),first,"Undo first independently","test");
        assertThat(queries.balance(scope).openDebt()).isEqualByComparingTo("800");
        assertThat(queries.balance(scope).availableCredit()).isEqualByComparingTo("600");
        assertThat(queries.balance(scope).netDebt()).isEqualByComparingTo("200");
        assertThat(queries.position(scope,c.id()).state()).isEqualTo(State.PARTIAL);
        var history=queries.allocations(scope,d.id(),0,500);
        assertThat(history.total()).isEqualTo(3);
        assertThat(history.items()).extracting(Allocation::effect).containsExactly(Effect.CONSUME,Effect.CONSUME,Effect.RESTORE);
        assertThat(history.items().get(2).reverseOfAllocationId()).isEqualTo(history.items().getFirst().id());
        assertThat(history.items().get(2).reverseOfOperationId()).isEqualTo(first);
        assertThat(queries.operation(scope,first).reversedByOperationId()).isEqualTo(reverse);
        assertThat(queries.operation(scope,reverse).reverseOfOperationId()).isEqualTo(first);
        assertThat(queries.operation(scope,first).components()).isEmpty();
        assertThat(queries.allocations(scope,d.id(),3,10).items()).isEmpty();
        assertThat(queries.allocations(scope,d.id(),3,10).total()).isEqualTo(3);
    }
    @Test void exhaustedAndCancelledRemainIdentifiableAndCreditNetIsSigned() {
        var d=pos(Nature.DEBT,"10"); var c=pos(Nature.CREDIT,"20"); compensate(d,c,"10");
        assertThat(queries.position(scope,d.id()).state()).isEqualTo(State.EXHAUSTED);
        assertThat(queries.balance(scope).netDebt()).isEqualByComparingTo("-10");
        var unused=pos(Nature.DEBT,"5"); long creation=queries.position(scope,unused.id()).creationOperationId();
        long reverse=core.reverse(scope,key(),creation,"Cancel creation","test");
        assertThat(queries.position(scope,unused.id()).state()).isEqualTo(State.CANCELLED);
        assertThat(queries.operation(scope,creation).reversedByOperationId()).isEqualTo(reverse);
        assertThat(queries.positions(scope,Selection.CLOSED,null,0,10).items()).extracting(PositionView::id).containsExactly(d.id(),unused.id());
        assertThat(queries.balance(scope).closedPositions()).isEqualTo(2);
    }
    @Test void moneyComponentsAreSeparateImmutableIdentities() {
        var d=pos(Nature.DEBT,"100");
        long op=core.settle(scope,key(),new Settlement(List.of(new Part(d.id(),m("100"))),m("98.01"),m("0"),m("1.99")),"test");
        var view=queries.operation(scope,op);
        assertThat(view.kind()).isEqualTo(OperationKind.SETTLE);
        assertThat(view.financialDocumentLink()).isEqualTo(DocumentLinkStatus.NOT_RECORDED);
        assertThat(view.creationDocument()).isEmpty();
        assertThat(view.financialEffectDate()).isEmpty();
        assertThat(view.components()).extracting(Component::kind).containsExactly(ComponentKind.CASH_IN,ComponentKind.DISCOUNT);
        assertThat(view.components().getFirst().amount()).isEqualByComparingTo("98.01");
        long reverse=core.reverse(scope,key(),op,"Undo settlement","test");
        var restored=queries.operation(scope,reverse).components();
        assertThat(restored).extracting(Component::kind).containsExactly(ComponentKind.CASH_OUT,ComponentKind.DISCOUNT);
        assertThat(restored).allMatch(Component::reversal);
        assertThat(restored).extracting(Component::reverseOfComponentId).containsExactlyElementsOf(view.components().stream().map(Component::id).toList());
    }
    @Test void deterministic501PositionsWithTimestampTiesAndGlobalCounts() {
        var ids=new TransactionTemplate(tx).execute(status -> {
            var result=new ArrayList<Long>(); for(int i=0;i<501;i++) result.add(pos(Nature.DEBT,"1.01").id()); return result;
        });
        for(int repeat=0;repeat<2;repeat++) {
            var first=queries.positions(scope,Selection.ALL,null,0,500);
            var last=queries.positions(scope,Selection.ALL,null,500,500);
            assertThat(first.total()).isEqualTo(501); assertThat(last.total()).isEqualTo(501);
            var actual=new ArrayList<Long>(first.items().stream().map(PositionView::id).toList());
            actual.addAll(last.items().stream().map(PositionView::id).toList());
            assertThat(actual).containsExactlyElementsOf(ids).doesNotHaveDuplicates();
            assertThat(first.items()).extracting(PositionView::recordedAt).containsOnly(first.items().getFirst().recordedAt());
            var beyond=queries.positions(scope,Selection.ALL,null,501,500);
            assertThat(beyond.items()).isEmpty(); assertThat(beyond.total()).isEqualTo(501);
            assertThat(queries.balance(scope).openDebt()).isEqualByComparingTo("506.01");
        }
    }
    @Test void scopeIsolationForEveryIdentityAndList() {
        var p=pos(Nature.DEBT,"10"); long op=queries.position(scope,p.id()).creationOperationId();
        for(var other:List.of(new Scope(2,scope.cliente(),"EUR"),new Scope(1,scope.cliente()+100000,"EUR"),new Scope(1,scope.cliente(),"USD"))) {
            assertThat(queries.positions(other,Selection.ALL,null,0,10).total()).isZero();
            assertThat(queries.balance(other).openPositions()).isZero();
            assertThatThrownBy(()->queries.position(other,p.id())).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(()->queries.operation(other,op)).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(()->queries.allocations(other,p.id(),0,10)).isInstanceOf(RuntimeException.class);
        }
        assertThat(queries.position(scope,p.id()).commercialOrigin()).isEmpty();
        assertThat(queries.byCommercialDocument(scope,Long.MAX_VALUE)).isEmpty();
    }
    @Test void invalidInputsAndFinancialDateNeverFallbackToRecordingTimestamp() {
        assertThatThrownBy(()->queries.positions(scope,Selection.ALL,null,-1,10)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->queries.positions(scope,Selection.ALL,null,0,501)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->queries.positions(scope,null,null,0,10)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->queries.balance(null)).isInstanceOf(RuntimeException.class);
        pos(Nature.DEBT,"10");
        assertThatThrownBy(()->queries.balanceAt(scope,LocalDate.now())).isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("TUULI-FIN-TIME-001");
    }
}
