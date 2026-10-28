package com.ar2lda.fac.fiscal.catalog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.postgresql.PGConnection;
import java.io.StringReader;
import java.net.URI;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;

/** Isolated databases and synthetic tuples only; never a production fiscal definition. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GovernedFtPersistenceTests {
 String admin,url,user,password,name,legacy;JdbcTemplate db;
 AtomicInteger sequence=new AtomicInteger(100);ObjectMapper json=new ObjectMapper();
 @BeforeAll void database()throws Exception {
  admin=System.getenv("FAC_TEST_DATASOURCE_URL");assertThat(admin).isNotBlank();var uri=URI.create(admin.substring(5));assertThat(uri.getHost()).isIn("127.0.0.1","localhost");assertThat(uri.getPath()).endsWith("_test");
  user=System.getenv("FAC_TEST_DATASOURCE_USERNAME");password=System.getenv("FAC_TEST_DATASOURCE_PASSWORD");name="governed_"+UUID.randomUUID().toString().replace("-","")+"_test";
  try(var c=DriverManager.getConnection(admin,user,password);var st=c.createStatement()){st.execute("CREATE DATABASE "+name);}
  url="jdbc:postgresql://"+uri.getRawAuthority()+"/"+name;db=new JdbcTemplate(new DriverManagerDataSource(url,user,password));
  Flyway.configure().dataSource(url,user,password).target("27").load().migrate();
  db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico) values('OLD','Prior version FT','FT',2,1,1),('NUL','Unclassified',null,2,1,1)");
  legacy=row("OLD");var f=Flyway.configure().dataSource(url,user,password).load();assertThat(f.migrate().migrationsExecuted).isOne();f.validate();assertThat(f.info().current().getVersion().getVersion()).isEqualTo("28");assertThat(f.migrate().migrationsExecuted).isZero();assertThat(row("OLD")).isEqualTo(legacy);assertThat(db.queryForObject("select count(*) from fiscal_catalog_revision",Long.class)).isZero();
 }
 @AfterAll void destroy()throws Exception {if(name!=null)try(var c=DriverManager.getConnection(admin,user,password);var st=c.createStatement()){st.execute("drop database "+name+" with(force)");}}
 String id(){return String.format("%03d",sequence.incrementAndGet());}
 long publish(){long rev=sequence.incrementAndGet();db.update("insert into fiscal_catalog_revision values('FT',?,?::jsonb)",rev,GovernedFtControllerTests.definition(rev));return rev;}
 String insert(long rev){String id=id();db.update(insertSql(id,rev));return id;}
 String insertSql(String id,long rev){return "insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico,liquidacao_imediata,fiscal_catalog_code,fiscal_catalog_revision) values('"+id+"','Synthetic','FT',98,9,2,false,'FT',"+rev+")";}
 String row(String id){return db.queryForObject("select to_jsonb(t)::text from tipodocumento t where id=?",String.class,id);}
 @ParameterizedTest @ValueSource(strings={"FT","ft"," Ft"}) void directInsertCannotOmitBindingOrBypassByCase(String code){
  assertThatThrownBy(()->db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico) values(?,'Test',?,98,9,2)",id(),code)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
 }
 @ParameterizedTest @ValueSource(strings={"codigo_fiscal='NC'","area_gestao=99","entidade=1","sinal_contabilistico=1","liquidacao_imediata=true","id='REN'"})
 void sqlAndOldWriterShapeCannotChangeBoundMateriality(String assignment){var id=insert(publish());String before=row(id);assertThatThrownBy(()->db.execute("update tipodocumento set "+assignment+" where id='"+id+"'")).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);assertThat(row(id)).isEqualTo(before);}
 @Test void compatibleUpdateAndTechnicalEmissionMarkerKeepSameRevision(){
  long rev=publish();String id=insert(rev);db.update("update tipodocumento set area_gestao=97,entidade=8,sinal_contabilistico=1 where id=?",id);
  db.update("update tipodocumento set configuracao_bloqueada=true where id=?",id);
  assertThatThrownBy(()->db.update("update tipodocumento set area_gestao=98,entidade=9,sinal_contabilistico=2 where id=?",id)).hasMessageContaining("bloqueada");
  assertThat(db.queryForObject("select fiscal_catalog_revision from tipodocumento where id=?",Long.class,id)).isEqualTo(rev);
 }
 @Test void legacyDescriptionAndV25TechnicalUpdateDoNotReclassify(){
  db.update("update tipodocumento set descricao='Description only',codigo_fiscal=codigo_fiscal,area_gestao=area_gestao where id='OLD'");
  db.update("update tipodocumento set configuracao_bloqueada=true where id='OLD'");
  assertThat(db.queryForObject("select codigo_fiscal from tipodocumento where id='OLD'",String.class)).isEqualTo("FT");
  assertThat(db.queryForObject("select fiscal_catalog_code from tipodocumento where id='OLD'",String.class)).isNull();
 }
 @ParameterizedTest @ValueSource(strings={"RC","RL","GUI"}) void nullFiscalIdentityOutsidePerimeterIsPreserved(String id){
  db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico) values(?,'Out of perimeter',?,1,2)",id,id.equals("GUI")?1:3);
  db.update("update tipodocumento set codigo_fiscal='NC' where id=?",id);db.update("update tipodocumento set codigo_fiscal=null where id=?",id);
  assertThatThrownBy(()->db.update("update tipodocumento set codigo_fiscal='FT',liquidacao_imediata=false where id=?",id)).hasMessageContaining("requires a governed binding");
 }
 @Test void bindingCannotBeBackfilledClearedOrRetargeted(){long r=publish();String id=insert(r);long other=publish();
  for(String change:List.of("fiscal_catalog_code=null,fiscal_catalog_revision=null","fiscal_catalog_revision="+other))assertThatThrownBy(()->db.execute("update tipodocumento set "+change+" where id='"+id+"'")).hasMessageContaining("binding is immutable");
  assertThatThrownBy(()->db.update("update tipodocumento set codigo_fiscal='FT',fiscal_catalog_code='FT',fiscal_catalog_revision=? where id='NUL'",r)).hasMessageContaining("binding is immutable");
 }
 @Test void wholeTupleIsRequiredNotCartesianAttributes(){long r=publish();String id=id();assertThatThrownBy(()->db.execute(insertSql(id,r).replace("98,9,2","98,8,2"))).hasMessageContaining("incompatible");}
 @Test void upsertInsertSelectAndMultirowRespectPersistentGuards(){
  long r=publish();String id=insert(r);
  assertThatThrownBy(()->db.execute("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico) values('"+id+"','Changed','FT',98,9,2) on conflict(id) do update set descricao=excluded.descricao")).hasMessageContaining("requires a governed binding");
  assertThatThrownBy(()->db.execute(insertSql(id,r)+" on conflict(id) do update set area_gestao=99")).hasMessageContaining("incompatible");
  assertThatThrownBy(()->db.execute("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico) select 'SEL',descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico from tipodocumento where id='"+id+"'")).hasMessageContaining("requires a governed binding");
  assertThatThrownBy(()->db.execute("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico) values('M01','Allowed','NC',3,1,1),('M02','Refused','FT',98,9,2)")).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
  assertThat(db.queryForObject("select count(*) from tipodocumento where id in('M01','M02')",Long.class)).isZero();
 }
 @Test void copyCannotPartiallyCommitInvalidRows()throws Exception {
  try(var c=DriverManager.getConnection(url,user,password)){
   assertThatThrownBy(()->c.unwrap(PGConnection.class).getCopyAPI().copyIn("COPY tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico,liquidacao_imediata) FROM STDIN WITH(FORMAT csv)",new StringReader("C01,Allowed,NC,3,1,1,false\nC02,Refused,FT,98,9,2,false\n"))).isInstanceOf(SQLException.class);
  }
  assertThat(db.queryForObject("select count(*) from tipodocumento where id in('C01','C02')",Long.class)).isZero();
 }
 @Test void rejectedTransactionRollsBackAllWritesAndPublication()throws Exception {
  long rev=sequence.incrementAndGet();String id=id();try(var c=DriverManager.getConnection(url,user,password)){c.setAutoCommit(false);try(var st=c.createStatement()){
   st.execute("insert into fiscal_catalog_revision values('FT',"+rev+",'"+GovernedFtControllerTests.definition(rev)+"')");
   st.execute(insertSql(id,rev));assertThatThrownBy(()->st.execute("update tipodocumento set sinal_contabilistico=1 where id='"+id+"'")).isInstanceOf(SQLException.class);
  }c.rollback();}
  assertThat(db.queryForObject("select count(*) from fiscal_catalog_revision where revision=?",Long.class,rev)).isZero();assertThat(db.queryForObject("select count(*) from tipodocumento where id=?",Long.class,id)).isZero();
 }
 static Stream<String> invalidDefinitions(){String d=GovernedFtControllerTests.definition(1);return Stream.of("{}","null",d.replace("\"schemaVersion\":1","\"schemaVersion\":2"),d.replace("\"areaGestao\":98","\"areaGestao\":\"98\""),d.replace("\"areaGestao\":98","\"areaGestao\":98.1"),d.replace("\"liquidacaoImediata\":false","\"liquidacaoImediata\":\"false\""),d.replace("\"entidade\":9","\"entidade\":null"),d.replace("\"semantics\":{","\"semantics\":{\"unexpected\":1,"));}
 @ParameterizedTest @MethodSource("invalidDefinitions") void javaAndSqlRefuseSameMalformedSemantics(String d)throws Exception {
  assertThatThrownBy(()->FtDeferredSemantics.configurations(json.readTree(d))).isInstanceOf(IllegalStateException.class);
  assertThat(db.queryForObject("select fiscal_ft_definition_valid(?::jsonb)",Boolean.class,d)).isFalse();
  assertThatThrownBy(()->db.update("insert into fiscal_catalog_revision values('FT',?,?::jsonb)",sequence.incrementAndGet(),d)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
 }
 @ParameterizedTest @ValueSource(ints={Connection.TRANSACTION_READ_COMMITTED,Connection.TRANSACTION_REPEATABLE_READ})
 void concurrentPublicationNeverExposesUncommittedDefinition(int isolation)throws Exception {
  long rev=sequence.incrementAndGet();String id=id();try(var a=DriverManager.getConnection(url,user,password);var b=DriverManager.getConnection(url,user,password)){
   a.setAutoCommit(false);b.setTransactionIsolation(isolation);b.setAutoCommit(false);
   try(var sa=a.createStatement();var sb=b.createStatement()){
    sa.execute("insert into fiscal_catalog_revision values('FT',"+rev+",'"+GovernedFtControllerTests.definition(rev)+"')");
    assertThatThrownBy(()->sb.execute(insertSql(id,rev))).isInstanceOf(SQLException.class);b.rollback();a.commit();
   }
  }
  db.execute(insertSql(id,rev));assertThat(db.queryForObject("select fiscal_catalog_revision from tipodocumento where id=?",Long.class,id)).isEqualTo(rev);
 }
 @ParameterizedTest @ValueSource(ints={Connection.TRANSACTION_READ_COMMITTED,Connection.TRANSACTION_REPEATABLE_READ})
 void concurrentSameIdentityHasOnlyOneWinner(int isolation)throws Exception {
  long rev=publish();String id=id();var started=new CountDownLatch(1);ExecutorService pool=Executors.newSingleThreadExecutor();
  try(var first=DriverManager.getConnection(url,user,password)){
   first.setAutoCommit(false);first.createStatement().execute(insertSql(id,rev));
   var f=pool.submit(()->{try(var c=DriverManager.getConnection(url,user,password)){c.setTransactionIsolation(isolation);c.setAutoCommit(false);try(var st=c.createStatement()){st.setQueryTimeout(10);started.countDown();st.execute(insertSql(id,rev));c.commit();return "unexpected";}catch(SQLException e){c.rollback();return e.getSQLState();}}});
   assertThat(started.await(5,TimeUnit.SECONDS)).isTrue();first.commit();assertThat(f.get(15,TimeUnit.SECONDS)).isIn("23505","40001");
  }finally{pool.shutdownNow();}
  assertThat(db.queryForObject("select count(*) from tipodocumento where id=?",Long.class,id)).isOne();
 }
 @ParameterizedTest @ValueSource(strings={"update fiscal_catalog_revision set definition=definition","delete from fiscal_catalog_revision","truncate fiscal_catalog_revision cascade"})
 void revisionImmutabilityIsNotWeakened(String sql){publish();assertThatThrownBy(()->db.execute(sql)).hasMessageContaining("revisions are immutable");}
}
