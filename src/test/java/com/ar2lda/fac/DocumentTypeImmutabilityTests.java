package com.ar2lda.fac;

import com.ar2lda.fac.controller.dto.TipoDocumentoUpdateDto;
import com.ar2lda.fac.service.TipoDocumentoService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentTypeImmutabilityTests {
    @Autowired JdbcTemplate db;
    @Autowired TipoDocumentoService service;
    @Autowired MockMvc mvc;
    @Autowired PlatformTransactionManager manager;
    String type;
    long client;
    @BeforeEach void fixture() {
        do { type = java.util.UUID.randomUUID().toString().substring(0,3).toUpperCase(); }
        while (db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico) values(?,'Tipo','ND',3,1,1) on conflict do nothing",type)==0);
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador) values(?,'A','Teste',0)",type);
        db.update("insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('IMT','Teste') on conflict do nothing");
        db.update("insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais) values('IMA','Teste','Rua','1000-020','Lisboa','PT') on conflict do nothing");
        db.update("insert into mpagamento(id,nome) values('IMP','Teste') on conflict do nothing");
        db.update("insert into utilizador(codigo,nome,email,password_hash,inativo) values('IMUSER','Teste','immutable@fac.test','unused',false) on conflict do nothing");
        client=db.queryForObject("select nextval('cliente_id_seq')",Long.class);
        db.update("insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values(?,'Teste','Rua','1000-020',?,'EUR','CON','IMT','PT')",client,Long.toString(900000000+client));
    }
    @AfterEach void cleanupOwnRows() {
        db.update("delete from documento_financeiro where id_tipo_documento=?",type);
        db.update("delete from documento_comercial where id_tipo_documento=?",type);
        db.update("delete from serie where id_tipo_documento=?",type);
        db.update("delete from cliente where id=?",client);
        // A used type remains locked even after the test's source documents are removed.
        db.update("delete from tipodocumento where id=? and not configuracao_bloqueada",type);
    }
    long financial(boolean cancelled) {
        return db.queryForObject("""
          insert into documento_financeiro(id_cliente,id_tipo_documento,serie,numero_documento,data_emissao,id_moeda,
          id_mpagamento,data_hora_operacao,id_emissor,momento_emissao,anulado)
          values(?,?,'A',nextval('documento_financeiro_id_seq'),'2026-10-21','EUR','IMP',now(),'IMUSER',now(),?) returning id
          """,Long.class,client,type,cancelled);
    }
    long commercial(String state) {
        return db.queryForObject("""
          insert into documento_comercial(id_tipo_documento,serie,numero_documento,estado,data_emissao,id_cliente,
          id_armazem_carga,id_moeda,id_riva,id_ppagamento,id_transporte,cliente_nome,cliente_nif,cliente_morada,
          cliente_cod_postal,cliente_pais,data_carga,carga_nome,carga_morada,carga_cod_postal,carga_localidade,carga_pais,data_vencimento,anulado)
          values(?,'A',nextval('documento_comercial_id_seq'),?,'2026-10-21',?,'IMA','EUR','CON','P30','IMT','Teste','999999990','Rua',
          '1000-020','PT','2026-10-21','Teste','Rua','1000-020','Lisboa','PT','2026-11-21',?) returning id
          """,Long.class,type,state,client,state.equals("ANULADO"));
    }
    boolean locked() { return service.getById(type).configuracaoBloqueada(); }
    TipoDocumentoUpdateDto update(String code) {
        var t=service.getById(type);
        return new TipoDocumentoUpdateDto("Descrição editável",code,"Outro modelo",null,null,null,
                t.areaGestao(),t.entidade(),t.sinalContabilistico(),t.liquidacaoImediata());
    }
    @Test void upgradeFromV24LocksCancelledHistoryWithoutChangingDocumentsAndRestarts() {
        String originalUrl=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_URL","jdbc:postgresql://localhost:25432/fac_test");
        String name="fac_type_upgrade_"+java.util.UUID.randomUUID().toString().replace("-", "")+"_test";
        db.execute("create database "+name);
        String url=originalUrl.substring(0,originalUrl.lastIndexOf('/')+1)+name;
        String user=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_USERNAME","postgres");
        String password=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_PASSWORD","postgres");
        JdbcTemplate primary=db;
        String originalType=type; long originalClient=client;
        try {
            org.flywaydb.core.Flyway.configure().dataSource(url,user,password).target("24").load().migrate();
            db=new JdbcTemplate(new org.springframework.jdbc.datasource.DriverManagerDataSource(url,user,password));
            fixture();
            long financial=financial(true), commercial=commercial("ANULADO");
            String financeBefore=db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,financial);
            String commercialBefore=db.queryForObject("select to_jsonb(d)::text from documento_comercial d where id=?",String.class,commercial);
            var flyway=org.flywaydb.core.Flyway.configure().dataSource(url,user,password).load();
            assertThat(flyway.migrate().migrationsExecuted).isEqualTo(4);
            assertThat(db.queryForObject("select configuracao_bloqueada from tipodocumento where id=?",Boolean.class,type)).isTrue();
            assertThat(db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,financial)).isEqualTo(financeBefore);
            assertThat(db.queryForObject("select to_jsonb(d)::text from documento_comercial d where id=?",String.class,commercial)).isEqualTo(commercialBefore);
            assertThatThrownBy(()->db.update("update tipodocumento set codigo_fiscal='NC' where id=?",type)).hasMessageContaining("bloqueada");
            assertThat(flyway.migrate().migrationsExecuted).isZero();
            flyway.validate();
        } finally { db=primary; type=originalType; client=originalClient; db.execute("drop database "+name+" with (force)"); }
    }
    @Test void unusedTypeAndDraftRemainEditable() {
        commercial("RASCUNHO");assertThat(locked()).isFalse();
        service.update(type,update("NC"));
        assertThat(service.getById(type).codigoFiscal()).isEqualTo("NC");
    }
    @ParameterizedTest @ValueSource(strings={"codigo_fiscal='NC'","area_gestao=2","entidade=2","sinal_contabilistico=2","liquidacao_imediata=true","configuracao_bloqueada=false"})
    void sqlCannotRewriteConfigurationOrUnlock(String assignment) {
        financial(false);
        assertThatThrownBy(()->db.update("update tipodocumento set "+assignment+" where id=?",type)).hasMessageContaining("bloqueada");
        assertThat(locked()).isTrue();
    }
    @Test void backendRejectsAndReadExposesLockWhileDescriptionAndModelsRemainEditable() throws Exception {
        long id=financial(false);
        String before=db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,id);
        mvc.perform(get("/tipos-documento/"+type)).andExpect(status().isOk()).andExpect(jsonPath("$.configuracaoBloqueada").value(true));
        String json="""
          {"descricao":"Alterada","codigoFiscal":"NC","areaGestao":3,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":false}
          """;
        mvc.perform(put("/tipos-documento/"+type).contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isConflict());
        service.update(type,update("ND"));
        assertThat(service.getById(type).descricao()).isEqualTo("Descrição editável");
        assertThat(service.getById(type).modeloEmissao1()).isEqualTo("Outro modelo");
        assertThat(db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,id)).isEqualTo(before);
    }
    @Test void cancellationOfAllFinancialDocumentsAndDeletionNeverUnlock() {
        long first=financial(false);financial(true);
        db.update("update documento_financeiro set anulado=true where id=?",first);
        assertThat(locked()).isTrue();
        db.update("delete from documento_financeiro where id_tipo_documento=?",type);
        assertThatThrownBy(()->service.update(type,update("NC"))).hasMessageContaining("bloqueada");
        assertThatThrownBy(()->db.update("delete from tipodocumento where id=?",type)).hasMessageContaining("histórica");
    }
    @ParameterizedTest @ValueSource(strings={"EMITIDO","ANULADO"})
    void commercialEmissionAndCancellationLockNonInvoiceTypes(String state) {
        long id=commercial(state);
        db.update("update documento_comercial set estado='ANULADO',anulado=true where id=?",id);
        assertThat(locked()).isTrue();
        assertThatThrownBy(()->service.update(type,update("NC"))).hasMessageContaining("bloqueada");
    }
    @Test void draftTransitionLocksAndRollbackDoesNotInventEmission() {
        long id=commercial("RASCUNHO");
        TransactionTemplate tx=new TransactionTemplate(manager);
        tx.executeWithoutResult(s->{db.update("update documento_comercial set estado='EMITIDO' where id=?",id);s.setRollbackOnly();});
        assertThat(locked()).isFalse();
        db.update("update documento_comercial set estado='EMITIDO' where id=?",id);
        assertThat(locked()).isTrue();
    }
    @ParameterizedTest @ValueSource(strings={"READ_COMMITTED","REPEATABLE_READ"})
    void firstEmissionSerializesWithSqlConfigurationChange(String isolation) throws Exception {
        race(isolation,true);
    }
    @ParameterizedTest @ValueSource(strings={"READ_COMMITTED","REPEATABLE_READ"})
    void editSerializesBeforeFirstEmission(String isolation) throws Exception {
        race(isolation,false);
    }
    void race(String isolation,boolean emissionFirst) throws Exception {
        TransactionTemplate first=new TransactionTemplate(manager),second=new TransactionTemplate(manager);
        second.setIsolationLevelName("ISOLATION_"+isolation);
        var acquired=new CountDownLatch(1);var release=new CountDownLatch(1);var attempting=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Future<?> a=pool.submit(()->first.executeWithoutResult(s->{
                if(emissionFirst)financial(false);else service.update(type,update("NC"));
                acquired.countDown();
                try {if(!release.await(10,TimeUnit.SECONDS))throw new AssertionError("release timeout");}catch(InterruptedException e){throw new RuntimeException(e);}
            }));
            assertThat(acquired.await(10,TimeUnit.SECONDS)).isTrue();
            Future<?> b=pool.submit(()->second.executeWithoutResult(s->{
                db.queryForObject("select configuracao_bloqueada from tipodocumento where id=?",Boolean.class,type);
                attempting.countDown();
                if(emissionFirst)db.update("update tipodocumento set codigo_fiscal='NC' where id=?",type);else financial(false);
            }));
            assertThat(attempting.await(10,TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(()->b.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            release.countDown();a.get(10,TimeUnit.SECONDS);
            if(emissionFirst || isolation.equals("REPEATABLE_READ")) {
                assertThatThrownBy(()->b.get(10,TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                    .satisfies(error -> {
                        Throwable cause=error;
                        while(cause.getCause()!=null) cause=cause.getCause();
                        assertThat(cause).isInstanceOf(java.sql.SQLException.class);
                        assertThat(((java.sql.SQLException)cause).getSQLState())
                            .isEqualTo(isolation.equals("REPEATABLE_READ")?"40001":"23514");
                    });
            } else b.get(10,TimeUnit.SECONDS);
            assertThat(service.getById(type).codigoFiscal()).isEqualTo(emissionFirst?"ND":"NC");
            if(!emissionFirst && isolation.equals("REPEATABLE_READ"))financial(false); // retry with a fresh snapshot
            assertThat(locked()).isTrue();
        } finally {release.countDown();pool.shutdownNow();}
    }
}
