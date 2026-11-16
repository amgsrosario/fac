package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.databind.*;
import com.ar2lda.fac.testinfra.FiscalSignatureTestConfiguration;
import com.ar2lda.fac.fiscal.catalog.FiscalCatalog;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** One committed request journey. No enclosing test transaction. Synthetic signer only. */
@SpringBootTest(properties={"fac.security.enabled=true","spring.jpa.show-sql=false","fac.demo.seed-on-startup=false"})
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles({"test","fr-inactive-lab"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named="M03_DISPOSABLE",matches="true")
@Import({FinancialFrIntegrationTests.CatalogFixture.class, FiscalSignatureTestConfiguration.class})
class FinancialFrIntegrationTests {
    static final String DEFINITION = """
        {"schemaVersion":1,"fiscalCode":"FT","revision":1,"semantics":{"allowedConfigurations":[
        {"codigoFiscal":"FT","areaGestao":2,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":false}]}}
        """;
    @TestConfiguration
    static class CatalogFixture {
        @Bean @Primary FiscalCatalog governedFixture(JdbcTemplate db) {
            String manifest = "{\"schemaVersion\":1,\"revisions\":[" + DEFINITION
                + "],\"creationBindings\":{\"ftDeferred\":{\"fiscalCode\":\"FT\",\"revision\":1}}}";
            return new FiscalCatalog(db,new ByteArrayResource(manifest.getBytes(StandardCharsets.UTF_8)));
        }
    }
    @Autowired com.ar2lda.fac.financial.FinancialFrIssuer issuer;
    @Autowired com.ar2lda.fac.financial.FinancialCore core;
    @Autowired com.ar2lda.fac.financial.FinancialQueries queries;
    @Autowired org.springframework.transaction.support.TransactionTemplate tx;
    @Autowired FiscalSignatureTestConfiguration.TestSource signer;
    @Autowired JdbcTemplate db;
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    String admin;
    long customer,family;
    JsonNode body(MvcResult r) throws Exception {return json.readTree(r.getResponse().getContentAsByteArray());}
    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder auth(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder r,String token){return r.header("Authorization","Bearer "+token);}
    String login(String code) throws Exception {
        return body(http.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\""+code+"\",\"password\":\"Journey8!Fixture\"}"))
            .andExpect(status().isOk()).andReturn()).get("token").asText();
    }
    @BeforeAll void fixture() throws Exception {
        assertThat(db.queryForObject("select current_database()",String.class)).startsWith("tuuli_fr_").endsWith("_test");
        assertThat(db.queryForObject("select system_identifier::text from pg_control_system()",String.class)).isEqualTo(System.getenv("M03_EXPECTED_CLUSTER"));
        db.update("insert into codpostal(id,nome) values('1000-010','Sintetico') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('M3T','Sintetico')");
        db.update("insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais) values('M3A','Sintetico','Rua Teste','1000-010','Sintetico','PT')");
        db.update("insert into mpagamento(id,nome) values('M3P','Sintetico')");
        family=db.queryForObject("insert into familia(descricao) values('Familia sintetica M03') returning id",Long.class);
        for(var row:List.of(new String[]{"M3ADMIN","ADMINISTRADOR"},new String[]{"M3OPER","OPERADOR"},new String[]{"M3READ","CONSULTA"})) {
            db.update("insert into utilizador(codigo,nome,email,password_hash,inativo,papel) values(?,?,?,?,false,?)",row[0],"Synthetic",row[0].toLowerCase()+"@example.test",encoder.encode("Journey8!Fixture"),row[1]);
        }
        admin=login("M3ADMIN");
        http.perform(auth(post("/empresa"),admin).contentType(MediaType.APPLICATION_JSON).content(company("Empresa sintetica M03"))).andExpect(status().isCreated());
        customer=body(http.perform(auth(post("/clientes"),admin).contentType(MediaType.APPLICATION_JSON).content(client("Cliente sintetico M03"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        http.perform(auth(post("/artigos"),admin).contentType(MediaType.APPLICATION_JSON).content(article(true))).andExpect(status().isCreated());

        db.update("insert into fiscal_catalog_revision values('FT',1,?::jsonb)", DEFINITION);
        http.perform(auth(post("/tipos-documento"),admin).contentType(MediaType.APPLICATION_JSON).content("""
            {"id":"GFT","descricao":"Synthetic governed FT","codigoFiscal":"FT","areaGestao":2,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":false}
            """)).andExpect(status().isCreated());
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('GFT','SYNTH','MockMvc only',0,'SYNM402','2026-01-01'),('GFT','NOAT','Negative',0,null,null)");
    }
    String company(String name){return """
        {"nome":"%s","nif":"509999999","morada":"Rua Teste","codPostalId":"1000-010","localidade":"Sintetico","paisId":"PT","capitalSocial":0,"matriculaRegistoComercial":"CRC SYNTH","cae":"62010","descricaoCae":"Sintetico","email":"company@example.test"}
        """.formatted(name);}
    String client(String name){return """
        {"nome":"%s","morada":"Rua Teste","localidade":"Sintetico","nif":"509123456","email":"client@example.test","retencao":false,"inativo":false,"codPostalId":"1000-010","paisId":"PT","moedaId":"EUR","rivaId":"CON","transporteId":"M3T"}
        """.formatted(name);}
    String article(boolean create){return """
        {%s"descricao":"Artigo sintetico M03","tipoArtigo":"SERVICO","unidade":"UN","familiaId":%d,"ivaCompraId":"NORMAL","ivaVendaId":"NORMAL","pvp":10,"inativo":false,"retencao":false}
        """.formatted(create?"\"codigo\":\"M3ART\",":"",family);}
    String line(String item,int quantity,int price){return "{\"artigoId\":\""+item+"\",\"quantidade\":"+quantity+",\"precoUnitario\":"+price+",\"tipoDesconto\":\"VALOR\",\"desconto\":0,\"tipoTaxaIvaId\":\"NORMAL\"}";}
    String draft(String series,String item,int quantity){return """
        {"documento":{"tipoDocumentoId":"GFT","serie":"%s","dataEmissao":"2026-11-03","clienteId":%d,"armazemCargaId":"M3A","moedaId":"EUR","rivaId":"CON","pPagamentoId":"P30","transporteId":"M3T"},"linha":%s}
        """.formatted(series,customer,line(item,quantity,10));}
    long create(String series,String token) throws Exception {return body(http.perform(auth(post("/documentos-comerciais"),token).contentType(MediaType.APPLICATION_JSON).content(draft(series,"M3ART",1))).andExpect(status().isCreated()).andReturn()).get("id").asLong();}
    String doc(long id){return "/documentos-comerciais/"+id;}

    Map<String,Object> state(long id) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("document",db.queryForMap("select estado,valor_total,numero_documento,atcud,qr_payload from documento_comercial where id=?",id));
        result.put("pending",db.queryForList("select id_cliente,valor_documento,valor_pendente from pendente where id_documento_comercial=?",id));
        result.put("positions",db.queryForList("select cliente_id,nature,original,available,cancelled from financial_position order by id"));
        result.put("series",db.queryForList("select serie,numerador from serie order by serie"));
        for (String table:List.of("financial_operation","financial_document_origin","financial_receipt","financial_allocation","financial_component","documento_financeiro","linha_documento_financeiro")) {
            result.put(table,db.queryForList("select * from "+table));
        }
        return result;
    }
    void record(String name,Object value) throws Exception {
        Path dir=Path.of(System.getenv("M402_EVIDENCE"));
        Files.createDirectories(dir);
        json.writerWithDefaultPrettyPrinter().writeValue(dir.resolve(name+".json").toFile(),value);
    }

    @BeforeAll void immediateConfiguration() throws Exception {
        http.perform(auth(post("/tipos-documento"),admin).contentType(MediaType.APPLICATION_JSON).content("""
            {"id":"IFR","descricao":"INACTIVE synthetic immediate invoice","codigoFiscal":"FT",
             "areaGestao":2,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":true}
            """)).andExpect(status().isCreated());
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('IFR','SYNTH','No fiscal validity',0,'SYNFR','2026-01-01'),('IFR','NOAT','Negative',0,null,null)");
    }
    @BeforeEach void authenticate() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("M3ADMIN","",List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_EMITIR"))));
    }
    @AfterEach void clear() {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
    long immediateDraft(String series) throws Exception {
        long created=body(http.perform(auth(post("/documentos-comerciais"),admin).contentType(MediaType.APPLICATION_JSON)
            .content(draft(series,"M3ART",1).replace("GFT","IFR"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        authenticate();return created;
    }
    java.math.BigDecimal money(String s){return new java.math.BigDecimal(s);}
    com.ar2lda.fac.financial.FinancialFrIssuer.Request request(long id) {
        return new com.ar2lda.fac.financial.FinancialFrIssuer.Request(id,money("12.30"),"M3P","Synthetic cash declaration",false);
    }
    com.ar2lda.fac.financial.FinancialCore.Scope scope(){return new com.ar2lda.fac.financial.FinancialCore.Scope(1,customer,"EUR");}
    String key(){return UUID.randomUUID().toString();}
    long count(String table){return db.queryForObject("select count(*) from "+table,Long.class);}
    void noSideEffects(long id,Map<String,Object> before){assertThat(state(id)).isEqualTo(before);}

    @Test void oneDocumentFullCashZeroResidualAndHistoricalDates() throws Exception {
        long id=immediateDraft("SYNTH");long receipts=count("documento_financeiro"),pending=count("pendente");
        var result=issuer.issue(key(),request(id));
        assertThat(result.documentId()).isEqualTo(id);
        assertThat(db.queryForMap("select estado,liquidado,valor_total from documento_comercial where id=?",id))
            .containsEntry("estado","EMITIDO").containsEntry("liquidado",true);
        assertThat(count("documento_financeiro")).isEqualTo(receipts);
        assertThat(count("pendente")).isEqualTo(pending);
        assertThat(db.queryForObject("select count(*) from financial_receipt where operation_id=?",Long.class,result.settlementId())).isZero();
        var position=queries.position(scope(),result.positionId());
        assertThat(position.original()).isEqualByComparingTo("12.30");
        assertThat(position.residual()).isEqualByComparingTo("0");
        assertThat(position.state()).isEqualTo(com.ar2lda.fac.financial.FinancialQueries.State.EXHAUSTED);
        var operation=queries.operation(scope(),result.settlementId());
        assertThat(operation.creationDocument().orElseThrow().documentId()).isEqualTo(id);
        assertThat(operation.financialDocument()).isEmpty(); // Single commercial quittance, never a fabricated RC.
        assertThat(operation.components()).hasSize(1);
        assertThat(operation.components().getFirst().kind()).isEqualTo(com.ar2lda.fac.financial.FinancialQueries.ComponentKind.CASH_IN);
        assertThat(operation.components().getFirst().amount()).isEqualByComparingTo("12.30");
        assertThat(operation.financialEffectDate()).contains(java.time.LocalDate.of(2026,11,3));
        assertThat(operation.operationAt()).contains(db.queryForObject("select momento_emissao from documento_comercial where id=?",java.sql.Timestamp.class,id).toInstant());
        assertThat(operation.operationAt().orElseThrow().atOffset(java.time.ZoneOffset.UTC).toLocalDate()).isAfter(java.time.LocalDate.of(2026,11,3));
        assertThat(queries.balanceAt(scope(),java.time.LocalDate.of(2026,11,2)).netDebt()).isEqualByComparingTo("0");
        assertThat(queries.balanceAt(scope(),java.time.LocalDate.of(2026,11,3)).netDebt()).isEqualByComparingTo("0");
        record("single-invoice-"+id,Map.of("state",state(id),"binding",db.queryForList("select * from financial_immediate_invoice where document_id=?",id),"effect",operation.toString()));
    }
    @Test void replayAndDifferentPayloadDoNotDuplicate() throws Exception {
        long id=immediateDraft("SYNTH");String key=key();var first=issuer.issue(key,request(id));var after=state(id);
        assertThat(issuer.issue(key,request(id))).isEqualTo(first);noSideEffects(id,after);
        var changed=new com.ar2lda.fac.financial.FinancialFrIssuer.Request(id,money("12.31"),"M3P","Synthetic cash declaration",false);
        assertThatThrownBy(()->issuer.issue(key,changed)).hasMessageContaining("different request");noSideEffects(id,after);
        assertThatThrownBy(()->issuer.issue(key(),request(id))).isInstanceOf(com.ar2lda.fac.exception.ConflictException.class);noSideEffects(id,after);
    }
    @Test void permissionsAndAuthenticatedIdentityAreRequired() throws Exception {
        long id=immediateDraft("SYNTH");var before=state(id);
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        assertThatThrownBy(()->issuer.issue(key(),request(id))).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        noSideEffects(id,before);
    }
    @Test void missingSignatureRollsBackNumberAndAllMoney() throws Exception {
        long id=immediateDraft("SYNTH");var before=state(id);signer.unavailable=true;
        try {assertThatThrownBy(()->issuer.issue(key(),request(id))).hasMessageContaining("assinatura");}
        finally {signer.unavailable=false;}
        noSideEffects(id,before);
    }
    @Test void missingAtCodeAndCashMismatchRollBack() throws Exception {
        long id=immediateDraft("NOAT");var before=state(id);
        assertThatThrownBy(()->issuer.issue(key(),request(id))).isInstanceOf(RuntimeException.class);noSideEffects(id,before);
        long other=immediateDraft("SYNTH");var beforeOther=state(other);
        var wrong=new com.ar2lda.fac.financial.FinancialFrIssuer.Request(other,money("12.29"),"M3P","Synthetic",false);
        assertThatThrownBy(()->issuer.issue(key(),wrong)).hasMessageContaining("full invoice");noSideEffects(other,beforeOther);
    }
    @Test void deferredFtCannotEnterImmediatePath() throws Exception {
        long id=create("SYNTH",admin);authenticate();var before=state(id);
        assertThatThrownBy(()->issuer.issue(key(),request(id))).hasMessageContaining("FT/immediate");noSideEffects(id,before);
    }
    @Test void faultsDuringSettlementAndBindingRollBackWholeAct() throws Exception {
        for(String table:List.of("financial_allocation","financial_immediate_invoice")) {
            long id=immediateDraft("SYNTH");var before=state(id);long audit=count("auditoria_evento");
            db.execute("create function fr_fault() returns trigger language plpgsql as $$ begin raise exception 'synthetic FR fault'; end $$");
            db.execute("create trigger fr_fault before insert on "+table+" for each row execute function fr_fault()");
            try {assertThatThrownBy(()->issuer.issue(key(),request(id))).isInstanceOf(RuntimeException.class);}
            finally {db.execute("drop trigger fr_fault on "+table);db.execute("drop function fr_fault()");}
            noSideEffects(id,before);assertThat(count("auditoria_evento")).isEqualTo(audit);
        }
    }
    List<Object> concurrent(long id,boolean sameKey) throws Exception {
        String key=key();var start=new java.util.concurrent.CountDownLatch(1);
        try(var pool=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var futures=new ArrayList<java.util.concurrent.Future<Object>>();
            for(int i=0;i<2;i++){String requestKey=sameKey?key:key();futures.add(pool.submit(()->{
                authenticate();try{start.await();return issuer.issue(requestKey,request(id));}
                catch(Exception e){return e;}finally{clear();}
            }));}start.countDown();var results=new ArrayList<Object>();
            for(var f:futures) results.add(f.get(30,java.util.concurrent.TimeUnit.SECONDS));return results;
        }
    }
    @Test void concurrentSameKeyReturnsSingleAct() throws Exception {
        long id=immediateDraft("SYNTH");var result=concurrent(id,true);
        assertThat(result.get(0)).isInstanceOf(com.ar2lda.fac.financial.FinancialFrIssuer.Issued.class).isEqualTo(result.get(1));
        assertThat(db.queryForObject("select count(*) from financial_immediate_invoice where document_id=?",Long.class,id)).isEqualTo(1);
    }
    @Test void concurrentDifferentKeysCannotIssueTwice() throws Exception {
        long id=immediateDraft("SYNTH");var result=concurrent(id,false);
        assertThat(result.stream().filter(com.ar2lda.fac.financial.FinancialFrIssuer.Issued.class::isInstance).count()).isEqualTo(1);
        assertThat(result.stream().filter(com.ar2lda.fac.exception.ConflictException.class::isInstance).count()).isEqualTo(1);
    }
    @Test void sqlSealingImmutabilityAndNoUndocumentedReversal() throws Exception {
        long id=immediateDraft("SYNTH");var issued=issuer.issue(key(),request(id));var before=state(id);
        for(String sql:List.of("update financial_immediate_invoice set received=1 where document_id="+id,
            "delete from financial_immediate_invoice where document_id="+id,
            "update documento_comercial set liquidado=false where id="+id,
            "update documento_comercial set data_emissao='2026-11-04' where id="+id,
            "update financial_position set available=original where id="+issued.positionId(),
            "insert into financial_component(operation_id,kind,amount) values("+issued.settlementId()+",'DISCOUNT',1)")) {
            assertThatThrownBy(()->db.execute(sql)).isInstanceOf(org.springframework.dao.DataAccessException.class);
        }
        assertThatThrownBy(()->core.reverse(scope(),key(),issued.settlementId(),"Not authorised", "M3ADMIN"))
            .isInstanceOf(org.springframework.dao.DataAccessException.class);
        noSideEffects(id,before);
        db.update("update documento_comercial set impresso=true where id=?",id);
        assertThat(db.queryForObject("select impresso from documento_comercial where id=?",Boolean.class,id)).isTrue();
    }
    @Test void sqlImmediateOriginCannotCommitWithoutCompleteAct() throws Exception {
        long id=immediateDraft("SYNTH");
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        authenticate();var before=state(id);
        assertThatThrownBy(()->tx.execute(ignored->{
            var position=core.create(scope(),key(),com.ar2lda.fac.financial.FinancialCore.Nature.DEBT,
                "DOCUMENTO_COMERCIAL:"+id,"Synthetic incomplete act",money("12.30"),"M3ADMIN");
            db.update("insert into financial_document_origin(document_id,position_id) values(?,?)",id,position.id());
            return null;
        })).isInstanceOf(RuntimeException.class).hasStackTraceContaining("complete FR act");
        noSideEffects(id,before);
    }
    @Test void linkedFrDoesNotPermitLegacyPendingOrReclassification() throws Exception {
        long id=immediateDraft("SYNTH");issuer.issue(key(),request(id));
        long ft=create("SYNTH",admin);
        http.perform(auth(post(doc(ft)+"/emitir"),admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        var before=state(id);
        assertThatThrownBy(()->db.update("update pendente set id_documento_comercial=? where id_documento_comercial=?",id,ft))
            .isInstanceOf(org.springframework.dao.DataAccessException.class).hasStackTraceContaining("legacy pending");
        assertThatThrownBy(()->db.update("update tipodocumento set liquidacao_imediata=false where id='IFR'"))
            .isInstanceOf(org.springframework.dao.DataAccessException.class);
        noSideEffects(id,before);
    }

}
