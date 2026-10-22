package com.ar2lda.fac;

import com.ar2lda.fac.mapper.DocumentoFinanceiroMapper;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class FinancialDocumentClassificationTests {
    @Autowired JdbcTemplate db;
    @Autowired DocumentoFinanceiroRepository documents;
    @Autowired TipoDocumentoRepository types;
    @Autowired DocumentoFinanceiroMapper mapper;
    @Autowired PlatformTransactionManager manager;
    long client,document;
    String originalType,otherType;
    final LocalDate day=LocalDate.of(2026,10,22);
    String newType(String fiscal,int sign) {
        String id;
        do {id=UUID.randomUUID().toString().substring(0,3).toUpperCase();}
        while(db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico) values(?,'Classificação',?,3,1,?) on conflict do nothing",id,fiscal,sign)==0);
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador) values(?,'A','Teste',0)",id);
        return id;
    }
    @BeforeEach void fixture() {
        db.update("insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('HCT','Teste') on conflict do nothing");
        db.update("insert into mpagamento(id,nome) values('HCP','Teste') on conflict do nothing");
        db.update("insert into utilizador(codigo,nome,email,password_hash,inativo) values('HCUSER','Teste','hc@fac.test','unused',false) on conflict do nothing");
        client=db.queryForObject("select nextval('cliente_id_seq')",Long.class);
        db.update("insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values(?,'Teste','Rua','1000-020',?,'EUR','CON','HCT','PT')",client,Long.toString(900000000+client));
        originalType=newType("ND",1);otherType=newType("NC",2);
        document=db.queryForObject("""
          insert into documento_financeiro(id_cliente,id_tipo_documento,serie,numero_documento,data_emissao,id_moeda,
          id_mpagamento,data_hora_operacao,id_emissor,momento_emissao,valor_pagamento_bruto,valor_pagamento_liquido)
          values(?,?,'A',nextval('documento_financeiro_id_seq'),?,'EUR','HCP',now(),'HCUSER',now(),27.5,27.5) returning id
          """,Long.class,client,originalType,day);
    }
    @AfterEach void cleanup() {
        db.update("delete from documento_financeiro where id=?",document);
        db.update("delete from serie where id_tipo_documento in (?,?)",originalType,otherType);
        db.update("delete from cliente where id=?",client);
        db.update("delete from tipodocumento where id in (?,?) and not configuracao_bloqueada",originalType,otherType);
    }
    void assertReadersStable() {
        new TransactionTemplate(manager).executeWithoutResult(status->{
            var dto=mapper.toDTO(documents.findById(document).orElseThrow(),List.of());
            assertThat(dto.tipoDocumentoId()).isEqualTo(originalType);
            assertThat(dto.tipoDocumentoCodigoFiscal()).isEqualTo("ND");
            assertThat(documents.findExtratoMovimentos(client,day,day)).singleElement()
                .satisfies(row->assertThat(row.getSinalContabilistico()).isEqualTo(1));
            assertThat(documents.findExtratosMovimentos(List.of(client),day,day)).singleElement()
                .satisfies(row->assertThat(row.getSinalContabilistico()).isEqualTo(1));
            var individual=documents.findExtratoAnterior(client,day.plusDays(1)).getFirst();
            var collective=documents.findExtratosAnterior(List.of(client),day.plusDays(1)).getFirst();
            assertThat(individual.getDebito()).isEqualByComparingTo("27.5");
            assertThat(individual.getCredito()).isZero();
            assertThat(collective.getDebito()).isEqualByComparingTo("27.5");
            assertThat(collective.getCredito()).isZero();
        });
    }
    @Test void documentCannotBeReclassifiedBySqlAndReadersKeepTheEmittedClassification() {
        assertReadersStable();
        assertThatThrownBy(()->db.update("update documento_financeiro set id_tipo_documento=? where id=?",otherType,document))
            .hasMessageContaining("Classificação do documento financeiro emitido é imutável");
        assertReadersStable();
    }
    @Test void failedReclassificationRollsBackAllPriorWritesAndTargetMasterMarker() {
        String before=db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,document);
        assertThatThrownBy(()->new TransactionTemplate(manager).executeWithoutResult(s->{
            db.update("update documento_financeiro set observacoes='Rollback esperado' where id=?",document);
            db.update("update documento_financeiro set id_tipo_documento=? where id=?",otherType,document);
        })).hasMessageContaining("Classificação do documento financeiro emitido é imutável");
        assertThat(db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,document)).isEqualTo(before);
        assertThat(db.queryForObject("select configuracao_bloqueada from tipodocumento where id=?",Boolean.class,otherType)).isFalse();
        assertReadersStable();
    }
    @Test void upsertCannotReplaceTheClassificationOfAnIssuedDocument() {
        assertThatThrownBy(()->db.update("""
            insert into documento_financeiro(id,id_cliente,id_tipo_documento,serie,numero_documento,data_emissao,
              id_moeda,id_mpagamento,data_hora_operacao,id_emissor,momento_emissao,valor_pagamento_bruto,valor_pagamento_liquido)
            select id,id_cliente,?,serie,numero_documento,data_emissao,id_moeda,id_mpagamento,data_hora_operacao,
              id_emissor,momento_emissao,valor_pagamento_bruto,valor_pagamento_liquido from documento_financeiro where id=?
            on conflict(id) do update set id_tipo_documento=excluded.id_tipo_documento
            """,otherType,document)).hasMessageContaining("Classificação do documento financeiro emitido é imutável");
        assertThat(db.queryForObject("select configuracao_bloqueada from tipodocumento where id=?",Boolean.class,otherType)).isFalse();
        assertReadersStable();
    }
    @Test void documentCannotBeReclassifiedByJpa() {
        assertThatThrownBy(()->new TransactionTemplate(manager).executeWithoutResult(s->{
            var doc=documents.findById(document).orElseThrow();doc.setTipoDocumento(types.findById(otherType).orElseThrow());
            documents.saveAndFlush(doc);
        })).hasStackTraceContaining("Classificação do documento financeiro emitido é imutável");
        assertReadersStable();
    }
    @ParameterizedTest @ValueSource(strings={"codigo_fiscal='NC'","area_gestao=2","entidade=2","sinal_contabilistico=2","liquidacao_imediata=true"})
    void v25ProtectsMasterClassificationUsedByReaders(String assignment) {
        assertThatThrownBy(()->db.update("update tipodocumento set "+assignment+" where id=?",originalType))
            .hasMessageContaining("Configuração fiscal e financeira bloqueada");
        assertReadersStable();
    }
    @Test void cancellationDoesNotAllowReclassification() {
        db.update("update documento_financeiro set anulado=true where id=?",document);
        assertThatThrownBy(()->db.update("update documento_financeiro set id_tipo_documento=? where id=?",otherType,document))
            .hasMessageContaining("Classificação do documento financeiro emitido é imutável");
        new TransactionTemplate(manager).executeWithoutResult(s->assertThat(mapper.toDTO(documents.findById(document).orElseThrow(),List.of()).tipoDocumentoCodigoFiscal()).isEqualTo("ND"));
        assertThat(documents.findExtratoMovimentos(client,day,day)).isEmpty();
    }
    @Test void unchangedTypeAndAllowedDescriptiveMetadataRemainEditable() {
        db.update("update tipodocumento set descricao='Descrição nova',modelo_emissao1='Modelo novo' where id=?",originalType);
        db.update("update documento_financeiro set id_tipo_documento=id_tipo_documento,impresso=true,observacoes='Teste' where id=?",document);
        assertReadersStable();
        assertThat(documents.findExtratoMovimentos(client,day,day)).singleElement()
            .satisfies(row->assertThat(row.getDescricao()).isEqualTo("Descrição nova"));
    }
    @ParameterizedTest @ValueSource(ints={2,4}) // JDBC READ_COMMITTED and REPEATABLE_READ
    void concurrentReclassificationsBothRollback(int isolation) throws Exception {
        var pool=Executors.newFixedThreadPool(2);var start=new CyclicBarrier(2);
        try {
            List<Future<?>> attempts=new ArrayList<>();
            for(int i=0;i<2;i++)attempts.add(pool.submit(()->{
                var tx=new TransactionTemplate(manager);tx.setIsolationLevel(isolation);
                tx.executeWithoutResult(s->{
                    try{start.await(10,TimeUnit.SECONDS);}catch(Exception e){throw new RuntimeException(e);}
                    db.update("update documento_financeiro set id_tipo_documento=? where id=?",otherType,document);
                });
            }));
            for(var attempt:attempts)assertThatThrownBy(()->attempt.get(15,TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class).hasStackTraceContaining("Classificação do documento financeiro emitido é imutável");
            assertReadersStable();
        } finally {pool.shutdownNow();}
    }
    @Test void reproduceV25GapAndUpgradeWithoutBackfillOrChangesToHistoricalReaders() {
        String url=System.getenv("FAC_TEST_DATASOURCE_URL");assertThat(url).endsWith("_test");
        String user=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_USERNAME","postgres");
        String password=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_PASSWORD","postgres");
        String name="op22b_history_"+UUID.randomUUID().toString().replace("-","")+"_test";
        db.execute("create database "+name);
        String fixtureUrl=url.substring(0,url.lastIndexOf('/')+1)+name;
        JdbcTemplate primary=db;long primaryClient=client,primaryDocument=document;String primaryType=originalType,primaryOther=otherType;
        try {
            Flyway.configure().dataSource(fixtureUrl,user,password).target("24").load().migrate();
            db=new JdbcTemplate(new DriverManagerDataSource(fixtureUrl,user,password));fixture();
            // Controlled pre-V25 historical change: not evidence about any real historical database.
            db.update("update tipodocumento set codigo_fiscal='XX' where id=?",originalType);
            Flyway.configure().dataSource(fixtureUrl,user,password).target("25").load().migrate();
            assertThat(codeBySql()).isEqualTo("XX"); // V25 cannot reconstruct the original ND.
            assertThat(db.update("update documento_financeiro set id_tipo_documento=? where id=?",otherType,document)).isEqualTo(1);
            assertThat(codeBySql()).isEqualTo("NC");
            assertThat(db.queryForObject("select t.sinal_contabilistico from documento_financeiro d join tipodocumento t on t.id=d.id_tipo_documento where d.id=?",Integer.class,document)).isEqualTo(2);
            System.out.println("OP22-B V25 reproduction: original ND; master changed pre-V25 to XX; document reassigned under V25 to NC, sign=2.");
            String before=db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,document);
            var flyway=Flyway.configure().dataSource(fixtureUrl,user,password).load();
            assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
            assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("27");
            assertThat(db.queryForObject("select to_jsonb(d)::text from documento_financeiro d where id=?",String.class,document)).isEqualTo(before);
            assertThat(codeBySql()).isEqualTo("NC"); // No reinterpretation or invented restoration of ND/XX.
            assertThatThrownBy(()->db.update("update documento_financeiro set id_tipo_documento=? where id=?",originalType,document))
                .hasMessageContaining("Classificação do documento financeiro emitido é imutável");
            assertThat(flyway.migrate().migrationsExecuted).isZero();flyway.validate();
        } finally {db=primary;client=primaryClient;document=primaryDocument;originalType=primaryType;otherType=primaryOther;db.execute("drop database "+name+" with (force)");}
    }
    String codeBySql() {
        return db.queryForObject("select t.codigo_fiscal from documento_financeiro d join tipodocumento t on t.id=d.id_tipo_documento where d.id=?",String.class,document);
    }
}
