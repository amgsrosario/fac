package com.ar2lda.fac.financial;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static com.ar2lda.fac.financial.FinancialCore.*;

@SpringBootTest
class FinancialCoreIntegrationTests {
 @Autowired FinancialCore core;
 @Autowired JdbcTemplate db;
 @Autowired PlatformTransactionManager tx;
 Scope scope;
 static BigDecimal m(String v){return new BigDecimal(v);}
 String key(){return UUID.randomUUID().toString();}
 @BeforeEach void fixture(){
  db.update("insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing");
  db.update("insert into transporte(id,nome) values('F20','Financial fixture') on conflict do nothing");
  db.update("insert into empresa(id,nome,nif,morada,id_codpostal,localidade,id_pais,capital_social,matricula_registo_comercial,cae,descricao_cae,email) values(1,'Teste','999999990','Rua','1000-020','Lisboa','PT',0,'Registo','00000','Teste','op20@fac.test') on conflict do nothing");
  long client=db.queryForObject("insert into cliente(nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values('OP20','Rua','1000-020',?,'EUR','CON','F20','PT') returning id",Long.class,UUID.randomUUID().toString().substring(0,20));
  scope=new Scope(1,client,"EUR");
 }
 Position pos(Nature n,String amount){return core.create(scope,key(),n,key(),"Fixture",m(amount),"test");}
 Settlement req(List<Part> p,String in,String out,String discount){return new Settlement(p,m(in),m(out),m(discount));}
 long comp(Position d,Position c,String value){return core.settle(scope,key(),req(List.of(new Part(d.id(),m(value)),new Part(c.id(),m(value))),"0","0","0"),"test");}
 void balance(Position p,String v){assertThat(core.position(scope,p.id()).available()).isEqualByComparingTo(v);}
 @Test void aOpenZeroNet(){var d=pos(Nature.DEBT,"1000");var c=pos(Nature.CREDIT,"1000");balance(d,"1000");balance(c,"1000");assertThat(d.state()).isEqualTo("OPEN");assertThat(c.state()).isEqualTo("OPEN");assertThat(core.history(scope,d.id())).isEmpty();}
 @Test void bAndCPartialCreditAndIndependentReversal(){var d=pos(Nature.DEBT,"1000");var c=pos(Nature.CREDIT,"800");long x=comp(d,c,"300");balance(d,"700");balance(c,"500");assertThat(core.position(scope,c.id()).state()).isEqualTo("PARTIAL");comp(d,c,"200");balance(d,"500");balance(c,"300");core.reverse(scope,key(),x,"Independent reversal","test");balance(d,"800");balance(c,"600");assertThat(core.history(scope,d.id())).hasSize(3);assertThatThrownBy(()->core.reverse(scope,key(),x,"Again","test")).isInstanceOf(RuntimeException.class);}
 @Test void dMixedRealCash(){var d=pos(Nature.DEBT,"1000");var c=pos(Nature.CREDIT,"800");long op=core.settle(scope,key(),req(List.of(new Part(d.id(),m("1000")),new Part(c.id(),m("800"))),"200","0","0"),"test");balance(d,"0");balance(c,"0");assertThat(db.queryForObject("select amount from financial_component where operation_id=? and kind='CASH_IN'",BigDecimal.class,op)).isEqualByComparingTo("200");}
 @Test void eZeroCash(){var d=pos(Nature.DEBT,"1000");var c=pos(Nature.CREDIT,"1000");long op=comp(d,c,"1000");balance(d,"0");balance(c,"0");assertThat(db.queryForObject("select count(*) from financial_component where operation_id=?",Long.class,op)).isZero();}
 @Test void fDiscountSeparate(){var d=pos(Nature.DEBT,"1000");long op=core.settle(scope,key(),req(List.of(new Part(d.id(),m("1000"))),"980","0","20"),"test");balance(d,"0");assertThat(db.queryForObject("select amount from financial_component where operation_id=? and kind='CASH_IN'",BigDecimal.class,op)).isEqualByComparingTo("980");assertThat(db.queryForObject("select amount from financial_component where operation_id=? and kind='DISCOUNT'",BigDecimal.class,op)).isEqualByComparingTo("20");core.reverse(scope,key(),op,"Refund reversal","test");balance(d,"1000");}
 List<Object> concurrent(Callable<?> action)throws Exception{
  var pool=Executors.newFixedThreadPool(2);var start=new CyclicBarrier(2);
  try{var calls=new ArrayList<Future<Object>>();for(int i=0;i<2;i++)calls.add(pool.submit(()->{start.await(10,TimeUnit.SECONDS);try{return action.call();}catch(Exception e){return e;}}));var result=new ArrayList<Object>();for(var f:calls)result.add(f.get(20,TimeUnit.SECONDS));return result;}finally{pool.shutdownNow();}
 }
 @Test void gConcurrentNoOverconsumption()throws Exception{var d=pos(Nature.DEBT,"100");var request=req(List.of(new Part(d.id(),m("100"))),"100","0","0");var result=concurrent(()->core.settle(scope,key(),request,"test"));assertThat(result.stream().filter(Long.class::isInstance).count()).isEqualTo(1);balance(d,"0");}
 @Test void hConcurrentRetrySameKey()throws Exception{var d=pos(Nature.DEBT,"100");String k=key();var request=req(List.of(new Part(d.id(),m("100"))),"100","0","0");var result=concurrent(()->core.settle(scope,k,request,"test"));assertThat(result).allMatch(Long.class::isInstance);assertThat(result.get(0)).isEqualTo(result.get(1));assertThat(core.history(scope,d.id())).hasSize(1);assertThatThrownBy(()->core.settle(scope,k,req(List.of(new Part(d.id(),m("50"))),"50","0","0"),"test")).isInstanceOf(RuntimeException.class);}
 @Test void dependencyOnCreationAndReversalRetry(){var d=pos(Nature.DEBT,"100");long creation=db.queryForObject("select creation_id from financial_position where id=?",Long.class,d.id());long use=core.settle(scope,key(),req(List.of(new Part(d.id(),m("40"))),"40","0","0"),"test");assertThatThrownBy(()->core.reverse(scope,key(),creation,"Creation reversal","test")).isInstanceOf(RuntimeException.class);core.reverse(scope,key(),use,"Undo consumption","test");String k=key();long reverse=core.reverse(scope,k,creation,"Creation reversal","test");assertThat(core.reverse(scope,k,creation,"Creation reversal","test")).isEqualTo(reverse);assertThat(core.position(scope,d.id()).state()).isEqualTo("CANCELLED");assertThatThrownBy(()->new TransactionTemplate(tx).execute(status->db.update("update financial_position set cancelled=false,available=original where id=?",d.id()))).isInstanceOf(RuntimeException.class);}
 @Test void refundIdentifiable(){var c=pos(Nature.CREDIT,"500");long op=core.settle(scope,key(),req(List.of(new Part(c.id(),m("200"))),"0","200","0"),"test");balance(c,"300");assertThat(db.queryForObject("select amount from financial_component where operation_id=? and kind='CASH_OUT'",BigDecimal.class,op)).isEqualByComparingTo("200");}
 @Test void scopesInvalidValuesAndDuplicates(){var d=pos(Nature.DEBT,"100");assertThatThrownBy(()->core.position(new Scope(2,scope.cliente(),"EUR"),d.id())).isInstanceOf(RuntimeException.class);assertThatThrownBy(()->core.position(new Scope(1,scope.cliente()+1,"EUR"),d.id())).isInstanceOf(RuntimeException.class);assertThatThrownBy(()->core.position(new Scope(1,scope.cliente(),"USD"),d.id())).isInstanceOf(RuntimeException.class);assertThatThrownBy(()->pos(Nature.CREDIT,"-1")).isInstanceOf(RuntimeException.class);assertThatThrownBy(()->pos(Nature.CREDIT,"0.001")).isInstanceOf(RuntimeException.class);assertThatThrownBy(()->core.settle(scope,key(),req(List.of(new Part(d.id(),m("10")),new Part(d.id(),m("10"))),"20","0","0"),"test")).isInstanceOf(RuntimeException.class);balance(d,"100");}
 @Test void rollbackAfterWrites(){var d=pos(Nature.DEBT,"100");String k=key();assertThatThrownBy(()->new TransactionTemplate(tx).execute(status->{core.settle(scope,k,req(List.of(new Part(d.id(),m("100"))),"100","0","0"),"test");throw new IllegalStateException("Simulated failure after writes");})).isInstanceOf(IllegalStateException.class);balance(d,"100");assertThat(core.history(scope,d.id())).isEmpty();assertThat(db.queryForObject("select count(*) from financial_operation where request_key=?",Long.class,k)).isZero();}
 @Test void persistenceRejectsProjectionAndHistoryTampering(){var d=pos(Nature.DEBT,"100");assertThatThrownBy(()->new TransactionTemplate(tx).execute(status->db.update("update financial_position set available=50 where id=?",d.id()))).isInstanceOf(RuntimeException.class);balance(d,"100");assertThatThrownBy(()->db.update("update financial_position set nature='CREDIT' where id=?",d.id())).isInstanceOf(RuntimeException.class);long op=core.settle(scope,key(),req(List.of(new Part(d.id(),m("10"))),"10","0","0"),"test");assertThatThrownBy(()->db.update("delete from financial_operation where id=?",op)).isInstanceOf(RuntimeException.class);}
 @Test void consolidatedOperationsRejectAppendAndFakeReversals(){
  var d=pos(Nature.DEBT,"100");long op=core.settle(scope,key(),req(List.of(new Part(d.id(),m("10"))),"10","0","0"),"test");
  assertThatThrownBy(()->db.update("insert into financial_component(operation_id,kind,amount) values(?,'CASH_OUT',1)",op)).isInstanceOf(RuntimeException.class);
  assertThatThrownBy(()->new TransactionTemplate(tx).execute(status->db.update("insert into financial_operation(empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason,reverse_of) values(1,?,'EUR','REVERSE',?,'fake','test','invalid',?)",scope.cliente(),key(),op))).isInstanceOf(RuntimeException.class);
  assertThatThrownBy(()->new TransactionTemplate(tx).execute(status->db.update("update financial_position set cancelled=true,available=0 where id=?",d.id()))).isInstanceOf(RuntimeException.class);
  balance(d,"90");
 }
 @Test void concurrentReversalsRestoreOnlyOnce() throws Exception {
  var d=pos(Nature.DEBT,"100");long op=core.settle(scope,key(),req(List.of(new Part(d.id(),m("10"))),"10","0","0"),"test");
  var results=concurrent(()->core.reverse(scope,key(),op,"Concurrent reversal","test"));
  assertThat(results.stream().filter(Long.class::isInstance).count()).isEqualTo(1);balance(d,"100");
 }

 @Test void writesRejectCrossScopeAndCreationDuplicate(){
  var d=pos(Nature.DEBT,"100");var request=req(List.of(new Part(d.id(),m("100"))),"100","0","0");
  assertThatThrownBy(()->core.settle(new Scope(2,scope.cliente(),"EUR"),key(),request,"test")).isInstanceOf(RuntimeException.class);
  assertThatThrownBy(()->core.settle(new Scope(1,scope.cliente()+1,"EUR"),key(),request,"test")).isInstanceOf(RuntimeException.class);
  assertThatThrownBy(()->core.settle(new Scope(1,scope.cliente(),"USD"),key(),request,"test")).isInstanceOf(RuntimeException.class);
  balance(d,"100");
  String origin=key(),k=key();var created=core.create(scope,k,Nature.CREDIT,origin,"Reference",m("20"),"test");
  assertThat(core.create(scope,k,Nature.CREDIT,origin,"Reference",m("20"),"test").id()).isEqualTo(created.id());
  assertThatThrownBy(()->core.create(scope,key(),Nature.CREDIT,origin,"Reference",m("20"),"test")).isInstanceOf(RuntimeException.class);
 }

}
