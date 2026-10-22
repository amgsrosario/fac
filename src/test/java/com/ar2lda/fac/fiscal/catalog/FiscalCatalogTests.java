package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/** All definitions here are synthetic mechanism fixtures, never production fiscal rules. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FiscalCatalogTests {
    String adminUrl, url, user, password, name;
    JdbcTemplate db;
    final AtomicInteger sequence = new AtomicInteger();
    final ObjectMapper json = new ObjectMapper();

    @BeforeAll void database() throws Exception {
        adminUrl = System.getenv("FAC_TEST_DATASOURCE_URL");
        assertThat(adminUrl).isNotBlank();
        URI uri = URI.create(adminUrl.substring(5));
        assertThat(uri.getHost()).isIn("127.0.0.1", "localhost");
        assertThat(uri.getPath()).endsWith("_test");
        user = System.getenv().getOrDefault("FAC_TEST_DATASOURCE_USERNAME", "postgres");
        password = System.getenv().getOrDefault("FAC_TEST_DATASOURCE_PASSWORD", "postgres");
        name = "catalog_" + UUID.randomUUID().toString().replace("-", "") + "_test";
        try (var c = DriverManager.getConnection(adminUrl, user, password); var s = c.createStatement()) {
            s.execute("create database " + name);
        }
        url = "jdbc:postgresql://" + uri.getRawAuthority() + "/" + name;
        db = new JdbcTemplate(new DriverManagerDataSource(url, user, password));
        Flyway.configure().dataSource(url,user,password).target("26").load().migrate();
        db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico) values('OLD','Historical','ND',3,1,1)");
        String before = db.queryForObject("select to_jsonb(t)::text from tipodocumento t where id='OLD'",String.class);
        var flyway = Flyway.configure().dataSource(url,user,password).load();
        assertThat(flyway.migrate().migrationsExecuted).isOne();
        flyway.validate();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("27");
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(db.queryForObject("select (to_jsonb(t)-'fiscal_catalog_code'-'fiscal_catalog_revision')::text from tipodocumento t where id='OLD'",String.class)).isEqualTo(before);
        assertThat(db.queryForObject("select count(*) from fiscal_catalog_revision",Long.class)).isZero();
        new FiscalCatalog(db).verifyCoherence();
    }

    @AfterAll void destroy() throws Exception {
        if (name == null) return;
        try (var c = DriverManager.getConnection(adminUrl,user,password); var s = c.createStatement()) {
            s.execute("drop database " + name + " with (force)");
        }
    }

    String definition(long revision) {
        return "{\"schemaVersion\":1,\"fiscalCode\":\"Z9\",\"revision\":" + revision
                + ",\"semantics\":{\"fixtureOnly\":true}}";
    }
    long publish() {
        long rev = sequence.incrementAndGet();
        db.update("insert into fiscal_catalog_revision values('Z9',?,?::jsonb)",rev,definition(rev));
        return rev;
    }
    String type(long rev) {
        String id = String.format("%03d",sequence.incrementAndGet());
        db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico,fiscal_catalog_code,fiscal_catalog_revision) values(?,'Synthetic','Z9',3,1,1,'Z9',?)",id,rev);
        return id;
    }
    FiscalCatalog manifest() {
        String entries = String.join(",",db.queryForList("select definition::text from fiscal_catalog_revision order by revision",String.class));
        return reader("{\"schemaVersion\":1,\"revisions\":[" + entries + "]}");
    }
    FiscalCatalog reader(String source) {
        return new FiscalCatalog(db,new ByteArrayResource(source.getBytes(StandardCharsets.UTF_8)));
    }
    String queryType(String id) {
        return db.queryForObject("select to_jsonb(t)::text from tipodocumento t where id=?",String.class,id);
    }

    @Test void migrationPreservesUnboundHistoryAndReadersDoNotInferClassification() {
        assertThat(manifest().forType("OLD")).isEmpty();
        assertThat(db.queryForObject("select codigo_fiscal from tipodocumento where id='OLD'",String.class)).isEqualTo("ND");
        assertThatThrownBy(()->manifest().forType("???")).hasMessageContaining("Unknown document type");
    }
    @Test void governedDefinitionIsConsumedExactlyAndCopiesCannotMutateCatalog() {
        long rev=publish(); var catalog=manifest();
        var value=catalog.revision("Z9",rev);
        ((com.fasterxml.jackson.databind.node.ObjectNode)value.definition()).put("fiscalCode","XX");
        assertThat(value.definition().get("fiscalCode").asText()).isEqualTo("Z9");
        catalog.verifyCoherence();
        assertThat(catalog.forType(type(rev))).get().extracting(x->x.key().revision()).isEqualTo(rev);
    }
    @ParameterizedTest @ValueSource(strings={"update fiscal_catalog_revision set definition=definition", "delete from fiscal_catalog_revision", "truncate fiscal_catalog_revision cascade"})
    void directSqlCannotRewriteDeleteOrTruncatePublishedRevisions(String sql) {
        publish();
        assertThatThrownBy(()->db.execute(sql)).hasMessageContaining("revisions are immutable");
    }
    @Test void newRevisionDoesNotRetargetBoundType() {
        long first=publish();String id=type(first);long second=publish();
        assertThat(manifest().forType(id).orElseThrow().key().revision()).isEqualTo(first);
        assertThatThrownBy(()->db.update("update tipodocumento set fiscal_catalog_revision=? where id=?",second,id)).hasMessageContaining("binding is immutable");
    }
    @Test void sqlCannotRemoveBindingOrBackfillHistoricalTypes() {
        long rev=publish();String id=type(rev);
        assertThatThrownBy(()->db.update("update tipodocumento set fiscal_catalog_code=null,fiscal_catalog_revision=null where id=?",id)).hasMessageContaining("binding is immutable");
        assertThatThrownBy(()->db.update("update tipodocumento set fiscal_catalog_code='Z9',fiscal_catalog_revision=? where id='OLD'",rev)).hasMessageContaining("no historical reassignment");
    }
    @Test void missingRevisionPairAndWrongFiscalIdentityAreRejected() {
        long rev=publish();String id=type(rev);
        assertThatThrownBy(()->db.update("update tipodocumento set codigo_fiscal='XX' where id=?",id)).hasMessageContaining("fiscal_catalog_binding_pair");
        assertThatThrownBy(()->db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico,fiscal_catalog_code,fiscal_catalog_revision) values('MIS','Test','Z9',3,1,1,'Z9',9223372036854775807)")).hasMessageContaining("fiscal_catalog_binding_fk");
        assertThatThrownBy(()->db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,fiscal_catalog_code) values('PAR','Test',3,1,1,'Z9')")).hasMessageContaining("fiscal_catalog_binding_pair");
    }
    @ParameterizedTest @ValueSource(strings={"{}","[]","null","{\"schemaVersion\":1,\"fiscalCode\":\"XX\",\"revision\":1,\"semantics\":{}}","{\"schemaVersion\":\"1\",\"fiscalCode\":\"Z9\",\"revision\":1,\"semantics\":{}}"})
    void malformedOrMismatchedDefinitionsCannotBeMaterialized(String content) {
        assertThatThrownBy(()->db.update("insert into fiscal_catalog_revision values('Z9',?,?::jsonb)",sequence.incrementAndGet(),content)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
    @Test void ungovernedDatabaseDefinitionsFailClosedOnlyWhenCatalogIsUsed() {
        publish();
        assertThatThrownBy(()->new FiscalCatalog(db).verifyCoherence()).hasMessageContaining("differs from governed manifest");
        assertThat(db.queryForObject("select codigo_fiscal from tipodocumento where id='OLD'",String.class)).isEqualTo("ND");
    }
    @Test void missingMaterializationFailsBeforeUse() {
        long rev=sequence.incrementAndGet();
        String entries=String.join(",",db.queryForList("select definition::text from fiscal_catalog_revision",String.class));
        String all=entries.isEmpty()?definition(rev):entries+","+definition(rev);
        assertThatThrownBy(()->reader("{\"schemaVersion\":1,\"revisions\":["+all+"]}").verifyCoherence()).hasMessageContaining("incomplete");
    }
    @Test void semanticallyDifferentManifestIncludingHighPrecisionNumbersIsRejected() {
        long rev=sequence.incrementAndGet();
        String original=definition(rev).replace("true","12345678901234567890.12345678901234567891");
        db.update("insert into fiscal_catalog_revision values('Z9',?,?::jsonb)",rev,original);
        manifest().verifyCoherence();
        String entries=String.join(",",db.queryForList("select definition::text from fiscal_catalog_revision",String.class));
        String changed=entries.replace("12345678901234567890.12345678901234567891","12345678901234567890.12345678901234567892");
        assertThatThrownBy(()->reader("{\"schemaVersion\":1,\"revisions\":["+changed+"]}").verifyCoherence()).hasMessageContaining("differs");
    }
    @Test void jsonbScaleDifferencesCompareEqualWithoutLosingPrecision() {
        long rev=sequence.incrementAndGet();
        String original=definition(rev).replace("true","123.4500");
        db.update("insert into fiscal_catalog_revision values('Z9',?,?::jsonb)",rev,original);
        String entries=String.join(",",db.queryForList("select definition::text from fiscal_catalog_revision",String.class));
        reader("{\"schemaVersion\":1,\"revisions\":["+entries.replace("123.4500","123.45")+"]}").verifyCoherence();
    }
    @Test void duplicateGovernedIdentitiesAndMalformedManifestAreRejected() {
        assertThatThrownBy(()->reader("{}").verifyCoherence()).hasMessageContaining("Invalid fiscal catalog manifest");
        String entry=definition(1);
        assertThatThrownBy(()->reader("{\"schemaVersion\":1,\"revisions\":["+entry+","+entry+"]}").verifyCoherence()).hasMessageContaining("Duplicate");
    }
    @Test void failedBindingRollsBackPublicationAndOtherWrites() throws Exception {
        long rev=sequence.incrementAndGet();String before=queryType("OLD");
        try(var c=DriverManager.getConnection(url,user,password)) {
            c.setAutoCommit(false);
            try(var s=c.createStatement()) {
                s.execute("insert into fiscal_catalog_revision values('Z9',"+rev+",'"+definition(rev)+"')");
                assertThatThrownBy(()->s.execute("update tipodocumento set descricao='rollback',fiscal_catalog_code='Z9',fiscal_catalog_revision="+rev+" where id='OLD'")).isInstanceOf(SQLException.class);
            }
            c.rollback();
        }
        assertThat(queryType("OLD")).isEqualTo(before);
        assertThat(db.queryForObject("select count(*) from fiscal_catalog_revision where revision=?",Long.class,rev)).isZero();
    }
    @Test void boundTypeAndIssuedFinancialDocumentKeepV25AndV26ProtectionsAfterCancellation() {
        long rev=publish();String id=type(rev);String other=type(rev);
        db.update("insert into codpostal(id,nome) values('1000-027','Fixture') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('CAT','Fixture') on conflict do nothing");
        db.update("insert into mpagamento(id,nome) values('CAT','Fixture') on conflict do nothing");
        db.update("insert into utilizador(codigo,nome,email,password_hash,inativo) values('CATUSER','Fixture','catalog@fac.test','unused',false) on conflict do nothing");
        long client=db.queryForObject("select nextval('cliente_id_seq')",Long.class);
        db.update("insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values(?,'Fixture','Rua','1000-027',?,'EUR','CON','CAT','PT')",client,Long.toString(900000000+client));
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador) values(?,'A','Fixture',0)",id);
        long document=db.queryForObject("insert into documento_financeiro(id_cliente,id_tipo_documento,serie,numero_documento,data_emissao,id_moeda,id_mpagamento,data_hora_operacao,id_emissor,momento_emissao,valor_pagamento_bruto,valor_pagamento_liquido) values(?,?,'A',1,current_date,'EUR','CAT',now(),'CATUSER',now(),10,10) returning id",Long.class,client,id);
        String before=db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,document);
        assertThatThrownBy(()->db.update("update tipodocumento set sinal_contabilistico=2 where id=?",id)).hasMessageContaining("bloqueada");
        assertThatThrownBy(()->db.update("update documento_financeiro set id_tipo_documento=? where id=?",other,document)).hasMessageContaining("Classificação");
        assertThat(db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,document)).isEqualTo(before);
        db.update("update documento_financeiro set anulado=true where id=?",document);
        assertThatThrownBy(()->db.update("update tipodocumento set fiscal_catalog_code=null,fiscal_catalog_revision=null where id=?",id)).hasMessageContaining("binding is immutable");
        assertThatThrownBy(()->db.update("update tipodocumento set liquidacao_imediata=true where id=?",id)).hasMessageContaining("bloqueada");
        assertThat(manifest().forType(id).orElseThrow().key().revision()).isEqualTo(rev);
    }
    @Test void duplicatePublicationCannotReplaceDefinition() {
        long rev=publish();
        assertThatThrownBy(()->db.update("insert into fiscal_catalog_revision values('Z9',?,?::jsonb)",rev,definition(rev)))
                .hasMessageContaining("duplicate key");
        assertThatThrownBy(()->db.update("insert into fiscal_catalog_revision values('Z9',?,?::jsonb) on conflict(fiscal_code,revision) do update set definition=excluded.definition",rev,definition(rev)))
                .hasMessageContaining("revisions are immutable");
    }
    @Test void verificationAndBoundReadSucceedInReadOnlyTransaction() throws Exception {
        long rev=publish();String id=type(rev);
        String entries=String.join(",",db.queryForList("select definition::text from fiscal_catalog_revision",String.class));
        try(var c=DriverManager.getConnection(url,user,password)) {
            c.setReadOnly(true);c.setAutoCommit(false);
            var readOnlyDb=new JdbcTemplate(new org.springframework.jdbc.datasource.SingleConnectionDataSource(c,true));
            var catalog=new FiscalCatalog(readOnlyDb,new ByteArrayResource(("{\"schemaVersion\":1,\"revisions\":["+entries+"]}").getBytes(StandardCharsets.UTF_8)));
            catalog.verifyCoherence();
            assertThat(catalog.forType(id).orElseThrow().key().revision()).isEqualTo(rev);
            c.rollback();
        }
    }
    @ParameterizedTest @ValueSource(ints={Connection.TRANSACTION_READ_COMMITTED,Connection.TRANSACTION_REPEATABLE_READ})
    void concurrentBindingRejectsUncommittedPublicationAndCanRetryAfterCommit(int isolation) throws Exception { race(isolation,false); }
    @ParameterizedTest @ValueSource(ints={Connection.TRANSACTION_READ_COMMITTED,Connection.TRANSACTION_REPEATABLE_READ})
    void publicationRollbackCannotLeaveDanglingBinding(int isolation) throws Exception { race(isolation,true); }

    void race(int isolation,boolean rollback) throws Exception {
        long rev=sequence.incrementAndGet();String id=String.format("%03d",sequence.incrementAndGet());
        ExecutorService pool=Executors.newSingleThreadExecutor();
        CountDownLatch attempting=new CountDownLatch(1);
        try(var c=DriverManager.getConnection(url,user,password);var s=c.createStatement()) {
            c.setAutoCommit(false);
            s.execute("insert into fiscal_catalog_revision values('Z9',"+rev+",'"+definition(rev)+"')");
            Future<?> bind=pool.submit(()->{
                try(var other=DriverManager.getConnection(url,user,password);var stmt=other.createStatement()) {
                    other.setTransactionIsolation(isolation);other.setAutoCommit(false);
                    stmt.execute("set local lock_timeout='8s'");
                    attempting.countDown();
                    stmt.execute("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico,fiscal_catalog_code,fiscal_catalog_revision) values('"+id+"','Race','Z9',3,1,1,'Z9',"+rev+")");
                    other.commit();
                }catch(SQLException e){throw new RuntimeException(e);}
            });
            assertThat(attempting.await(5,TimeUnit.SECONDS)).isTrue();
            // PostgreSQL FK lookup rejects an uncommitted parent; it need not wait.
            // A failed attempt must leave no configuration, before either parent outcome.
            try { bind.get(5,TimeUnit.SECONDS); throw new AssertionError("Uncommitted revision was visible"); }
            catch (ExecutionException rejected) {
                assertThat(rejected.getCause().getCause()).isInstanceOf(SQLException.class);
                assertThat(((SQLException)rejected.getCause().getCause()).getSQLState()).isEqualTo("23503");
            }
            assertThat(db.queryForObject("select count(*) from tipodocumento where id=?",Long.class,id)).isZero();
            if(rollback)c.rollback();else c.commit();
            if(rollback) {
                assertThat(db.queryForObject("select count(*) from fiscal_catalog_revision where revision=?",Long.class,rev)).isZero();
                assertThat(db.queryForObject("select count(*) from tipodocumento where id=?",Long.class,id)).isZero();
            } else {
                db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico,fiscal_catalog_code,fiscal_catalog_revision) values(?,'Retry','Z9',3,1,1,'Z9',?)",id,rev);
                assertThat(manifest().forType(id).orElseThrow().key().revision()).isEqualTo(rev);
            }
        }finally{pool.shutdownNow();}
    }
}
