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
@Import({ProjectedFrIntegrationTests.CatalogFixture.class, FiscalSignatureTestConfiguration.class})
class ProjectedFrIntegrationTests {
    static final String DEFINITION = """
        {"schemaVersion":1,"fiscalCode":"FT","revision":1,"semantics":{"allowedConfigurations":[
        {"codigoFiscal":"FT","areaGestao":2,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":false}]}}
        """;
    static final String FR_DEFINITION="""
        {"schemaVersion":2,"fiscalCode":"FT","revision":9000100,"semantics":{"specialization":"FR","externalType":"FR","documentLabel":"Fatura-recibo","allowedConfigurations":[{"codigoFiscal":"FT","areaGestao":2,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":true}]}}
        """;
    @TestConfiguration
    static class CatalogFixture {
        @Bean @Primary FiscalCatalog governedFixture(JdbcTemplate db) {
            String manifest = "{\"schemaVersion\":1,\"revisions\":[" + DEFINITION
                + ","+FR_DEFINITION+"],\"creationBindings\":{\"ftDeferred\":{\"fiscalCode\":\"FT\",\"revision\":1},\"frImmediate\":{\"fiscalCode\":\"FT\",\"revision\":9000100}}}";
            return new FiscalCatalog(db,new ByteArrayResource(manifest.getBytes(StandardCharsets.UTF_8)));
        }
    }
    @Autowired com.ar2lda.fac.financial.FinancialFrIssuer issuer;
    @Autowired com.ar2lda.fac.financial.FinancialCore core;
    @Autowired GovernedFrConfiguration configurations;
    @Autowired com.ar2lda.fac.service.DocumentoComercialService commercial;
    @Autowired com.ar2lda.fac.financial.FinancialQueries queries;
    @Autowired org.springframework.transaction.support.TransactionTemplate tx;
    @org.springframework.boot.test.mock.mockito.SpyBean FiscalSignatureTestConfiguration.TestSource signer;
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
        db.update("insert into fiscal_catalog_revision values('FT',9000100,?::jsonb)",FR_DEFINITION);
        authenticate();
        configurations.create(config("FRE"));
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('FRE','TESTFR','No fiscal validity',0,'TESTFR01','2026-01-01'),('FRE','FRNOAT','Negative',0,null,null)");
    }
    com.ar2lda.fac.controller.dto.TipoDocumentoCreateDto config(String id) throws Exception {
        return json.readValue("{\"id\":\""+id+"\",\"descricao\":\"Not a fiscal title\",\"codigoFiscal\":\"FT\",\"areaGestao\":2,\"entidade\":1,\"sinalContabilistico\":1,\"liquidacaoImediata\":true}",com.ar2lda.fac.controller.dto.TipoDocumentoCreateDto.class);
    }
    @BeforeEach void isolatedCustomer() {
        customer=db.queryForObject("select nextval('cliente_id_seq')",Long.class);
        db.update("insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values(?,'Synthetic FR','Rua','1000-010',?,'EUR','CON','M3T','PT')",customer,Long.toString(900000000+customer));
    }
    @BeforeEach void authenticate() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("M3ADMIN","",List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_EMITIR"),new org.springframework.security.core.authority.SimpleGrantedAuthority("CONFIGURACAO_GERIR"))));
    }
    @AfterEach void clear() {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
    long immediateDraft(String series) throws Exception {
        long created=body(http.perform(auth(post("/documentos-comerciais"),admin).contentType(MediaType.APPLICATION_JSON)
            .content(draft(series.equals("SYNTH")?"TESTFR":"FRNOAT","M3ART",1).replace("GFT","FRE"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
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
        org.mockito.Mockito.clearInvocations(signer);
        var result=issuer.issueProjected(key(),request(id));
        var signed=org.mockito.ArgumentCaptor.forClass(com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto.class);
        org.mockito.Mockito.verify(signer).commercial(signed.capture());
        assertThat(signed.getValue().documento().mPagamentoId()).isEqualTo("M3P");
        assertThat(signed.getValue().documento().liquidado()).isTrue();
        assertThat(result.documentId()).isEqualTo(id);
        var identity=db.queryForMap("select projection_external_type,projection_catalog_code,projection_revision,tipo_documento_codigo_fiscal,numero_documento_completo,atcud,qr_payload from documento_comercial where id=?",id);
        assertThat(identity).containsEntry("projection_external_type","FR").containsEntry("projection_catalog_code","FT").containsEntry("tipo_documento_codigo_fiscal","FT");
        String number=(String)identity.get("numero_documento_completo");assertThat(number).startsWith("FRE TESTFR/");
        assertThat((String)identity.get("qr_payload")).contains("*D:FR*").contains("*G:"+number+"*").contains("*H:"+identity.get("atcud")+"*");
        var response=http.perform(auth(get(doc(id)+"/pdf"),admin)).andExpect(status().isOk()).andReturn().getResponse();
        byte[] pdf=response.getContentAsByteArray();
        try(var printed=org.apache.pdfbox.Loader.loadPDF(pdf)) {
            String text=new org.apache.pdfbox.text.PDFTextStripper().getText(printed);
            assertThat(text).contains("Fatura-recibo",number,"Quitação integral");
            var codes=new ArrayList<com.ar2lda.fac.testinfra.PdfQrInspection.Qr>();
            for(var page:printed.getPages())codes.addAll(com.ar2lda.fac.testinfra.PdfQrInspection.inspect(page));
            assertThat(codes).isNotEmpty();for(var qr:codes)assertThat(qr.payload()).isEqualTo(identity.get("qr_payload"));
        }
        Files.createDirectories(Path.of(System.getenv("M402_EVIDENCE")));Files.write(Path.of(System.getenv("M402_EVIDENCE"),"projected-fr-"+id+".pdf"),pdf);
        authenticate();

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
        long id=immediateDraft("SYNTH");String key=key();var first=issuer.issueProjected(key,request(id));var after=state(id);
        assertThat(issuer.issueProjected(key,request(id))).isEqualTo(first);noSideEffects(id,after);
        var changed=new com.ar2lda.fac.financial.FinancialFrIssuer.Request(id,money("12.31"),"M3P","Synthetic cash declaration",false);
        assertThatThrownBy(()->issuer.issueProjected(key,changed)).hasMessageContaining("different request");noSideEffects(id,after);
        assertThatThrownBy(()->issuer.issueProjected(key(),request(id))).isInstanceOf(com.ar2lda.fac.exception.ConflictException.class);noSideEffects(id,after);
    }
    @Test void permissionsAndAuthenticatedIdentityAreRequired() throws Exception {
        long id=immediateDraft("SYNTH");var before=state(id);
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        assertThatThrownBy(()->issuer.issueProjected(key(),request(id))).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        noSideEffects(id,before);
    }
    @Test void missingSignatureRollsBackNumberAndAllMoney() throws Exception {
        long id=immediateDraft("SYNTH");var before=state(id);signer.unavailable=true;
        try {assertThatThrownBy(()->issuer.issueProjected(key(),request(id))).hasMessageContaining("assinatura");}
        finally {signer.unavailable=false;}
        noSideEffects(id,before);
    }
    @Test void missingAtCodeAndCashMismatchRollBack() throws Exception {
        long id=immediateDraft("NOAT");var before=state(id);
        assertThatThrownBy(()->issuer.issueProjected(key(),request(id))).isInstanceOf(RuntimeException.class);noSideEffects(id,before);
        long other=immediateDraft("SYNTH");var beforeOther=state(other);
        var wrong=new com.ar2lda.fac.financial.FinancialFrIssuer.Request(other,money("12.29"),"M3P","Synthetic",false);
        assertThatThrownBy(()->issuer.issueProjected(key(),wrong)).hasMessageContaining("full invoice");noSideEffects(other,beforeOther);
    }
    @Test void deferredFtCannotEnterImmediatePath() throws Exception {
        long id=create("SYNTH",admin);authenticate();var before=state(id);
        assertThatThrownBy(()->issuer.issueProjected(key(),request(id))).hasMessageContaining("Projected FR");noSideEffects(id,before);
    }
    @Test void faultsDuringSettlementAndBindingRollBackWholeAct() throws Exception {
        for(String table:List.of("financial_allocation","financial_immediate_invoice")) {
            long id=immediateDraft("SYNTH");var before=state(id);long audit=count("auditoria_evento");
            db.execute("create function fr_fault() returns trigger language plpgsql as $$ begin raise exception 'synthetic FR fault'; end $$");
            db.execute("create trigger fr_fault before insert on "+table+" for each row execute function fr_fault()");
            try {assertThatThrownBy(()->issuer.issueProjected(key(),request(id))).isInstanceOf(RuntimeException.class).hasStackTraceContaining("synthetic FR fault");}
            finally {db.execute("drop trigger fr_fault on "+table);db.execute("drop function fr_fault()");}
            noSideEffects(id,before);assertThat(count("auditoria_evento")).isEqualTo(audit);
        }
    }
    List<Object> concurrent(long id,boolean sameKey) throws Exception {
        String key=key();var start=new java.util.concurrent.CountDownLatch(1);
        try(var pool=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var futures=new ArrayList<java.util.concurrent.Future<Object>>();
            for(int i=0;i<2;i++){String requestKey=sameKey?key:key();futures.add(pool.submit(()->{
                authenticate();try{start.await();return issuer.issueProjected(requestKey,request(id));}
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
        long id=immediateDraft("SYNTH");var issued=issuer.issueProjected(key(),request(id));var before=state(id);
        for(String sql:List.of("update financial_immediate_invoice set received=1 where document_id="+id,
            "truncate financial_immediate_invoice",
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
    @Test void ordinaryGovernedEmissionAndSqlIncompleteProjectionAreRefused() throws Exception {
        long id=immediateDraft("SYNTH");var before=state(id);
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isConflict());
        noSideEffects(id,before);authenticate();
        assertThatThrownBy(()->tx.execute(ignored->{
            db.update("update documento_comercial set liquidado=true,id_mpagamento='M3P' where id=?",id);
            commercial.emitirFrProjectado(id,new com.ar2lda.fac.controller.dto.DocumentoComercialEmitirDto("M3ADMIN",false));
            return null;
        })).hasStackTraceContaining("complete V30 quittance");
        noSideEffects(id,before);
    }
    @Test void linkedFrDoesNotPermitLegacyPendingOrReclassification() throws Exception {
        long id=immediateDraft("SYNTH");issuer.issueProjected(key(),request(id));
        long ft=create("SYNTH",admin);
        http.perform(auth(post(doc(ft)+"/emitir"),admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        var before=state(id);
        assertThatThrownBy(()->db.update("update pendente set id_documento_comercial=? where id_documento_comercial=?",id,ft))
            .isInstanceOf(org.springframework.dao.DataAccessException.class).hasStackTraceContaining("legacy pending");
        assertThatThrownBy(()->db.update("update tipodocumento set liquidacao_imediata=false where id='FRE'"))
            .isInstanceOf(org.springframework.dao.DataAccessException.class);
        noSideEffects(id,before);
    }

    @Test void i04bNewUnboundImmediateFtRetainsLegacyWithoutQuittance() throws Exception {
        http.perform(auth(post("/tipos-documento"),admin).contentType(MediaType.APPLICATION_JSON).content("""
            {"id":"C6F","descricao":"Legacy immediate FT","codigoFiscal":"FT","areaGestao":2,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":true}
            """)).andExpect(status().isCreated());
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('C6F','C6TEST','Synthetic',0,'C6TEST01','2026-01-01')");
        long id=body(http.perform(auth(post("/documentos-comerciais"),admin).contentType(MediaType.APPLICATION_JSON)
            .content(draft("C6TEST","M3ART",1).replace("GFT","C6F"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        long acts=count("financial_immediate_invoice"),cash=count("financial_component"),debts=count("financial_position");
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType(MediaType.APPLICATION_JSON).content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        assertThat(db.queryForObject("select qr_payload from documento_comercial where id=?",String.class,id)).contains("*D:FT*");
        assertThat(db.queryForObject("select liquidado from documento_comercial where id=?",Boolean.class,id)).isFalse();
        assertThat(count("financial_immediate_invoice")).isEqualTo(acts);assertThat(count("financial_component")).isEqualTo(cash);assertThat(count("financial_position")).isEqualTo(debts);
        record("I04b-C6-OPEN",state(id));
    }
    @Test void historicalInternalCodeCannotBeReclassifiedInJavaOrSql() throws Exception {
        long id=create("SYNTH",admin);
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType(MediaType.APPLICATION_JSON).content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        authenticate();var dto=config("GFT");var before=state(id);
        assertThatThrownBy(()->configurations.create(dto)).hasMessageContaining("different external");
        assertThatThrownBy(()->db.update("insert into tipodocumento(id,descricao,codigo_fiscal,area_gestao,entidade,sinal_contabilistico,liquidacao_imediata,fiscal_catalog_code,fiscal_catalog_revision) values('GFT','Reuse','FT',2,1,1,true,'FT',9000100)"))
            .isInstanceOf(org.springframework.dao.DataAccessException.class);
        noSideEffects(id,before);
    }
    @Test void catalogueTupleAndAliasSeriesAreProtected() throws Exception {
        authenticate();configurations.create(config("FRB"));
        assertThatThrownBy(()->db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('FRB','TESTFR','Duplicate',0,'TESTFR02','2026-01-01')")).hasStackTraceContaining("collides");
        assertThatThrownBy(()->db.update("update tipodocumento set liquidacao_imediata=false where id='FRB'")).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(()->db.update("update fiscal_catalog_revision set definition=definition where fiscal_code='FT' and revision=9000100")).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(()->db.update("delete from fiscal_type_identity where internal_code='FRB'")).isInstanceOf(org.springframework.dao.DataAccessException.class);
    }

}
