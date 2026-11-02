package com.ar2lda.fac.fiscal.signingtrial;

import jakarta.persistence.*;
import h207fixture.TrialWitness;
import org.flywaydb.core.Flyway;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.support.TransactionTemplate;
import java.net.URI;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrialPersistenceTests {
 String adminUrl,url,name,user,password; JdbcTemplate db; DriverManagerDataSource ds;
 EntityManagerFactory emf; JpaTransactionManager tm; TrialProofEngine engine;
 EphemeralTrialSigner signer; UUID scope; String series;
 @BeforeAll void database() throws Exception {
  adminUrl=Objects.requireNonNull(System.getenv("FAC_TEST_DATASOURCE_URL"),"Explicit disposable database required");
  URI u=URI.create(adminUrl.substring(5));assertThat(u.getHost()).isEqualTo("127.0.0.1");assertThat(u.getPath()).endsWith("_test");
  user=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_USERNAME","postgres"); password=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_PASSWORD","");
  name="h207_"+UUID.randomUUID().toString().replace("-","")+"_test";
  new JdbcTemplate(new DriverManagerDataSource(adminUrl,user,password)).execute("create database "+name);
  url="jdbc:postgresql://"+u.getRawAuthority()+"/"+name;ds=new DriverManagerDataSource(url,user,password);db=new JdbcTemplate(ds);
  var baseline=Flyway.configure().dataSource(ds).locations("classpath:db/migration").target("28").load();baseline.migrate();baseline.validate();
  assertThat(baseline.info().current().getVersion().getVersion()).isEqualTo("28");
  assertThat(db.queryForObject("select count(*) from documento_comercial",Long.class)).isZero();
  assertThat(db.queryForObject("select count(*) from fiscal_catalog_revision",Long.class)).isZero();
  var trial=Flyway.configure().dataSource(ds).locations("classpath:db/signature-trial").defaultSchema("public").table("h207_trial_history").baselineOnMigrate(true).baselineVersion("0").load();
  assertThat(trial.migrate().migrationsExecuted).isOne();trial.validate();assertThat(trial.migrate().migrationsExecuted).isZero();
  db.execute("create table signature_trial.jpa_witness(id uuid primary key,name text)");
  var cfg=new Configuration().addAnnotatedClass(TrialWitness.class);cfg.getProperties().put("hibernate.connection.datasource",ds);
  cfg.setProperty("hibernate.hbm2ddl.auto","none");cfg.setProperty("hibernate.connection.handling_mode","DELAYED_ACQUISITION_AND_HOLD");emf=cfg.buildSessionFactory();tm=new JpaTransactionManager(emf);tm.setJpaDialect(new org.springframework.orm.jpa.vendor.HibernateJpaDialect());tm.setDataSource(ds);
 }
 @AfterAll void destroy(){if(emf!=null)emf.close();if(name!=null)new JdbcTemplate(new DriverManagerDataSource(adminUrl,user,password)).execute("drop database "+name+" with(force)");}
 @BeforeEach void setup(){signer=EphemeralTrialSigner.createForExperiment();engine=new TrialProofEngine(db,tm,signer);scope=engine.newScope();series="H207T"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();engine.newSeries(scope,series);}
 TrialCanonical snap(UUID id,LocalDate date,LocalDateTime at){return new TrialCanonical(id,"FT","EUR",false,date,at,new BigDecimal("123.40"));}
 TrialCanonical snap(){return snap(UUID.randomUUID(),LocalDate.of(2026,10,30),LocalDateTime.of(2026,10,30,12,0));}
 long count(){return db.queryForObject("select count(*) from signature_trial.proof where scope=?",Long.class,scope);}
 @Test void chainReplayReadProjectionAndNoOperationalWrites(){
  var first=engine.append(scope,series,snap());var request=snap();var second=engine.append(scope,series,request);
  assertThat(first.predecessor()).isEmpty();assertThat(second.predecessor()).isEqualTo(first.signature());
  assertThat(engine.append(scope,series,request)).isEqualTo(second);assertThat(count()).isEqualTo(2);
  assertThat(engine.find(scope,second.document()).orElseThrow().demonstrationQ()).startsWith("NONFISCAL:");
  assertThat(engine.find(UUID.randomUUID(),second.document())).isEmpty();
  for(String table:List.of("documento_comercial","documento_financeiro","pendente","financial_operation","fiscal_catalog_revision"))
   assertThat(db.queryForObject("select count(*) from "+table,Long.class)).as(table).isZero();
 }
 @Test void rejectsChangedReplayAndUnregisteredSeries(){
  var r=snap();engine.append(scope,series,r);
  assertThatThrownBy(()->engine.append(scope,series,new TrialCanonical(r.document(),"FT","EUR",false,r.invoiceDate(),r.systemEntryDate(),BigDecimal.ONE))).hasMessageContaining("conflict");
  assertThatThrownBy(()->engine.append(scope,"H207TUNKNOWN",snap())).isInstanceOf(RuntimeException.class);assertThat(count()).isOne();
 }
 @Test void seriesIsExplicitlyNewAndFiscalTypeCannotBeReclassified(){
  assertThatThrownBy(()->engine.newSeries(scope,series)).isInstanceOf(RuntimeException.class);
  assertThatThrownBy(()->engine.newSeries(scope,"DEMO26")).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->db.update("update signature_trial.series set fiscal_type='FR' where scope=?",scope)).hasMessageContaining("immutable");
 }
 @Test void multiyearChainKeepsPreviousExerciseHash(){
  var first=engine.append(scope,series,snap(UUID.randomUUID(),LocalDate.of(2026,12,31),LocalDateTime.of(2026,12,31,23,59,59)));
  var second=engine.append(scope,series,snap(UUID.randomUUID(),LocalDate.of(2027,1,1),LocalDateTime.of(2027,1,1,0,0)));
  assertThat(second.invoiceNumber()).endsWith("/2");assertThat(second.predecessor()).isEqualTo(first.signature());
 }
 @Test void refusesRegressingInvoiceDateAndGlobalClock(){
  engine.append(scope,series,snap());
  assertThatThrownBy(()->engine.append(scope,series,snap(UUID.randomUUID(),LocalDate.of(2026,10,29),LocalDateTime.of(2026,10,30,13,0)))).hasMessageContaining("chronology");
  String other="H207TOTHER";engine.newSeries(scope,other);
  assertThatThrownBy(()->engine.append(scope,other,snap(UUID.randomUUID(),LocalDate.of(2026,10,30),LocalDateTime.of(2026,10,30,11,59)))).hasMessageContaining("chronology");assertThat(count()).isOne();
 }
 @Test void cannotRotateKeyInsideUnapprovedTrialChain(){
  engine.append(scope,series,snap());var other=new TrialProofEngine(db,tm,EphemeralTrialSigner.createForExperiment());
  assertThatThrownBy(()->other.append(scope,series,snap())).hasMessageContaining("chain");assertThat(count()).isOne();
 }
 @Test void failureRollsBackJpaJdbcNumberAndScopeClock(){
  UUID witness=UUID.randomUUID();var r=snap();
  assertThatThrownBy(()->new TransactionTemplate(tm).execute(status->{
   EntityManager em=EntityManagerFactoryUtils.getTransactionalEntityManager(emf);em.persist(new TrialWitness(witness));em.flush();
   engine.append(scope,series,r);throw new IllegalStateException("Injected after proof");
  })).hasMessageContaining("Injected");
  assertThat(db.queryForObject("select count(*) from signature_trial.jpa_witness where id=?",Long.class,witness)).isZero();assertThat(count()).isZero();
  assertThat(db.queryForObject("select last_system_at from signature_trial.scope where id=?",Object.class,scope)).isNull();
  assertThat(engine.append(scope,series,r).invoiceNumber()).endsWith("/1");
 }
 @Test void sqlProofIsImmutableAndDeferredConstraintsCannotBypass(){
  engine.append(scope,series,snap());
  assertThatThrownBy(()->db.update("update signature_trial.proof set gross_total=1 where scope=?",scope)).hasMessageContaining("immutable");
  assertThatThrownBy(()->db.update("delete from signature_trial.proof where scope=?",scope)).hasMessageContaining("immutable");
  assertThatThrownBy(()->db.execute("truncate signature_trial.proof")).hasMessageContaining("immutable");
  assertThatThrownBy(()->new TransactionTemplate(tm).execute(status->{db.execute("set constraints all immediate");db.execute("set constraints all deferred");db.update("update signature_trial.proof set sequence=2 where scope=?",scope);return null;})).hasMessageContaining("immutable");
 }
 @Test void sqlCannotSkipSequenceOrForgeCanonical(){
  var p=engine.append(scope,series,snap());
  assertThatThrownBy(()->db.update("insert into signature_trial.proof select scope,series,fiscal_type,sequence+2,?,invoice_date,system_at,gross_total,canonical,signature,signature,public_key,key_version,nonfiscal from signature_trial.proof where scope=?",UUID.randomUUID(),scope)).hasMessageContaining("chain");
  assertThatThrownBy(()->db.update("insert into signature_trial.proof select scope,series,fiscal_type,sequence+1,?,invoice_date,system_at,gross_total,'forged',signature,signature,public_key,key_version,nonfiscal from signature_trial.proof where scope=?",UUID.randomUUID(),scope)).hasMessageContaining("canonical");
  assertThat(count()).isOne();assertThat(engine.find(scope,p.document())).isPresent();
 }
 @Test void concurrentReplayHasOnlyOneProof() throws Exception {var r=snap();race(()->engine.append(scope,series,r),()->engine.append(scope,series,r));assertThat(count()).isOne();}
 @Test void concurrentAppendHasOneLinearChain() throws Exception {race(()->engine.append(scope,series,snap()),()->engine.append(scope,series,snap()));assertThat(count()).isEqualTo(2);}
 @Test void oppositeSeriesOrderHasNoDeadlock() throws Exception {
  String second="H207TSECOND";engine.newSeries(scope,second);
  race(()->new TransactionTemplate(tm).execute(s->{engine.append(scope,series,snap());return engine.append(scope,second,snap());}),
       ()->new TransactionTemplate(tm).execute(s->{engine.append(scope,second,snap());return engine.append(scope,series,snap());}));assertThat(count()).isEqualTo(4);
 }
 @Test void reReadAfterEphemeralKeyLossUsesOnlyStoredPublicProof() {
  var p=engine.append(scope,series,snap());var reader=new TrialProofEngine(db,tm,EphemeralTrialSigner.createForExperiment());
  assertThat(reader.find(scope,p.document()).orElseThrow()).isEqualTo(p);
 }
 @Test void invalidTransactionManagerFailsBeforeWritingProof() {
  var wrong=new JpaTransactionManager(emf);wrong.setDataSource(ds);
  var unsafe=new TrialProofEngine(db,wrong,signer);
  assertThatThrownBy(()->unsafe.append(scope,series,snap())).hasMessageContaining("transactional JDBC");assertThat(count()).isZero();
 }
 @Test void structurallyValidSqlForgeryIsDetectedOnReadNotClaimedAsDatabaseCrypto() {
  var request=snap();String number="FT "+series+"/1",canonical=request.bytesText(number,"");
  String forged=signer.sign(canonical+"tampered");
  db.update("insert into signature_trial.proof(scope,series,fiscal_type,sequence,document,invoice_date,system_at,gross_total,canonical,previous_signature,signature,public_key,key_version) values(?,?,'FT',1,?,?,?,?,?,'',?,?,1)",
   scope,series,request.document(),request.invoiceDate(),java.sql.Timestamp.valueOf(request.systemEntryDate()),request.grossTotal(),canonical,forged,signer.publicKey());
  assertThatThrownBy(()->engine.find(scope,request.document())).hasMessageContaining("unverifiable");
  assertThatThrownBy(()->engine.append(scope,series,snap())).hasMessageContaining("unverifiable");assertThat(count()).isOne();
 }
 @org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(ints={2,4,8})
 void isolationLevelsDoNotAllowTwoConflictingHeads(int isolation) throws Exception {
  var pool=Executors.newFixedThreadPool(2);var start=new java.util.concurrent.CyclicBarrier(2);
  Callable<Boolean> action=()->{var tx=new TransactionTemplate(tm);tx.setIsolationLevel(isolation);
   try {tx.execute(status->{ db.queryForObject("select count(*) from signature_trial.proof where scope=?",Long.class,scope);
    try{start.await(10,TimeUnit.SECONDS);}catch(Exception e){throw new IllegalStateException(e);}
    return engine.append(scope,series,snap());});return true;
   }catch(org.springframework.dao.DataAccessException failure){
    Throwable cause=failure;while(cause!=null){if(cause instanceof java.sql.SQLException sql && "40001".equals(sql.getSQLState()))return false;cause=cause.getCause();}throw failure;
   }
  };
  try{var a=pool.submit(action);var b=pool.submit(action);boolean x=a.get(20,TimeUnit.SECONDS),y=b.get(20,TimeUnit.SECONDS);
   assertThat(x||y).isTrue();assertThat(count()).isEqualTo((x?1:0)+(y?1:0));assertThat(count()).isEqualTo(isolation==2?2:1);
  }finally{pool.shutdownNow();}
 }
 void race(Callable<?> a,Callable<?> b)throws Exception {
  var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
  try{var f=pool.submit(()->{start.await();return a.call();});var g=pool.submit(()->{start.await();return b.call();});start.countDown();f.get(15,TimeUnit.SECONDS);g.get(15,TimeUnit.SECONDS);}finally{pool.shutdownNow();}
 }
}
