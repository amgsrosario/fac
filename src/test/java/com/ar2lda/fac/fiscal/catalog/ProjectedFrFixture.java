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

/** Shared PostgreSQL fixture and helpers for synthetic FR integration tests. */
@SpringBootTest(properties={"fac.security.enabled=true","spring.jpa.show-sql=false","fac.demo.seed-on-startup=false"})
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles({"test","fr-inactive-lab","fr-projection-test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named="M03_DISPOSABLE",matches="true")
@Import({ProjectedFrFixture.CatalogFixture.class, FiscalSignatureTestConfiguration.class, ProjectedFrFixture.TestEndpoint.class})
abstract class ProjectedFrFixture {
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
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
    @org.springframework.web.bind.annotation.RestController
    @org.springframework.context.annotation.Profile("fr-projection-test")
    static class TestEndpoint {
        private final com.ar2lda.fac.financial.FinancialFrIssuer issuer;
        TestEndpoint(com.ar2lda.fac.financial.FinancialFrIssuer issuer){this.issuer=issuer;}
        @org.springframework.web.bind.annotation.PostMapping("/_test/fr-emission/{key}")
        @org.springframework.security.access.prepost.PreAuthorize("@functionalAuthorization.has(T(com.ar2lda.fac.model.PermissaoFuncional).DOCUMENTO_EMITIR)")
        com.ar2lda.fac.financial.FinancialFrIssuer.Issued issue(@org.springframework.web.bind.annotation.PathVariable String key,
            @org.springframework.web.bind.annotation.RequestBody com.ar2lda.fac.financial.FinancialFrIssuer.Request request){return issuer.issueProjected(key,request);}
    }
    @Autowired com.ar2lda.fac.financial.FinancialFrIssuer issuer;
    @Autowired com.ar2lda.fac.financial.FinancialFrCorrection correction;
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
        db.update("insert into fiscal_catalog_revision values('FT',9000100,?::jsonb)",FR_DEFINITION);
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
                new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_ANULAR"),
                new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_EMITIR"),new org.springframework.security.core.authority.SimpleGrantedAuthority("CONFIGURACAO_GERIR"))));
    }
    @AfterEach void clear() {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
    long immediateDraft(String series) throws Exception {
        long created=body(http.perform(auth(post("/documentos-comerciais"),admin).contentType(MediaType.APPLICATION_JSON)
            .content(draft(series.equals("SYNTH")?"TESTFR":series.equals("NOAT")?"FRNOAT":series,"M3ART",1).replace("GFT","FRE"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
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

    java.util.Map<String,Object> correctionIdentity(long id) {
        return db.queryForMap("""
            select numero_documento_completo,atcud,qr_payload,projection_external_type,
                projection_catalog_code,projection_revision,projection_label from documento_comercial where id=?
            """,id);
    }
}
