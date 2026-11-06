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

/** M4-09: six available MockMvc journeys; R1 selects only its new case, independent rational expected values; synthetic signer only. */
@SpringBootTest(properties={"fac.security.enabled=true","spring.jpa.show-sql=false","fac.demo.seed-on-startup=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named="M409_DISPOSABLE",matches="true")
@Import({QrAmountsJourneyTests.CatalogFixture.class, FiscalSignatureTestConfiguration.class})
class QrAmountsJourneyTests {
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
    @org.springframework.boot.test.mock.mockito.SpyBean(name="fiscalTestSignatureSource") FiscalSignatureTestConfiguration.TestSource signer;
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
        assertThat(db.queryForObject("select current_database()",String.class)).startsWith("tuuli_m409_").endsWith("_test");
        assertThat(db.queryForObject("select system_identifier::text from pg_control_system()",String.class)).isEqualTo(System.getenv("M409_EXPECTED_CLUSTER"));
        db.update("insert into codpostal(id,nome) values('1000-010','Sintetico') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('M3T','Sintetico')");
        db.update("insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais) values('M3A','Sintetico','Rua Teste','1000-010','Sintetico','PT')");
        db.update("insert into mpagamento(id,nome) values('M3P','Sintetico')");
        family=db.queryForObject("insert into familia(descricao) values('Familia sintetica M409') returning id",Long.class);
        for(var row:List.of(new String[]{"M3ADMIN","ADMINISTRADOR"},new String[]{"M3OPER","OPERADOR"},new String[]{"M3READ","CONSULTA"})) {
            db.update("insert into utilizador(codigo,nome,email,password_hash,inativo,papel) values(?,?,?,?,false,?)",row[0],"Synthetic",row[0].toLowerCase()+"@example.test",encoder.encode("Journey8!Fixture"),row[1]);
        }
        admin=login("M3ADMIN");
        http.perform(auth(post("/empresa"),admin).contentType(MediaType.APPLICATION_JSON).content(company("Empresa sintetica M409"))).andExpect(status().isCreated());
        customer=body(http.perform(auth(post("/clientes"),admin).contentType(MediaType.APPLICATION_JSON).content(client("Cliente sintetico M409"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
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
        {%s"descricao":"Artigo sintetico M409","tipoArtigo":"SERVICO","unidade":"UN","familiaId":%d,"ivaCompraId":"NORMAL","ivaVendaId":"NORMAL","pvp":10,"inativo":false,"retencao":false}
        """.formatted(create?"\"codigo\":\"M3ART\",":"",family);}

    static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> cases() throws Exception {
        try(var stream=QrAmountsJourneyTests.class.getResourceAsStream("/m409/expected.json")) {
            var data=new ObjectMapper().readTree(java.util.Objects.requireNonNull(stream));
            String selected=System.getenv("M409_CASE_FILTER");
            return java.util.stream.StreamSupport.stream(data.get("cases").spliterator(),false)
                    .filter(c -> selected==null || selected.isBlank() || selected.equals(c.get("id").asText()))
                    .map(c -> org.junit.jupiter.params.provider.Arguments.of(c.get("id").asText(),c));
        }
    }
    Path evidence() throws Exception {
        Path p=Path.of(System.getenv("M409_EVIDENCE"));Files.createDirectories(p);return p;
    }
    void record(String name,Object value) throws Exception {
        json.writerWithDefaultPrettyPrinter().writeValue(evidence().resolve(name+".json").toFile(),value);
    }
    JsonNode request(String name,org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,int status) throws Exception {
        var response=http.perform(auth(request,admin)).andReturn();
        record(name,Map.of("status",response.getResponse().getStatus(),"body",response.getResponse().getContentAsString()));
        assertThat(response.getResponse().getStatus()).as(name).isEqualTo(status);
        return body(response);
    }
    com.fasterxml.jackson.databind.node.ObjectNode line(JsonNode expected) {
        var line=(com.fasterxml.jackson.databind.node.ObjectNode)expected.get("input").deepCopy();
        line.put("artigoId","M3ART");line.put("tipoTaxaIvaId","NORMAL");return line;
    }
    void amount(JsonNode actual,String field,JsonNode expected,String key) {
        assertThat(actual.hasNonNull(field)).as("present "+field).isTrue();
        assertThat(actual.get(field).decimalValue()).as(field).isEqualByComparingTo(expected.get(key).asText());
    }
    void persistedAmount(Map<String,Object> actual,String field,JsonNode expected,String key) {
        assertThat((java.math.BigDecimal)actual.get(field)).as("persisted "+field).isEqualByComparingTo(expected.get(key).asText());
    }
    @org.junit.jupiter.params.ParameterizedTest(name="{0}")
    @org.junit.jupiter.params.provider.MethodSource("cases")
    void persistedFtAmountsMatchIndependentOracleAndQr(String code,JsonNode scenario) throws Exception {
        record(code+"-expected",scenario);
        var document=json.createObjectNode().put("tipoDocumentoId","GFT").put("serie","SYNTH")
                .put("dataEmissao","2026-11-06").put("clienteId",customer).put("armazemCargaId","M3A")
                .put("moedaId","EUR").put("rivaId","CON").put("pPagamentoId","P30").put("transporteId","M3T");
        var create=json.createObjectNode();create.set("documento",document);create.set("linha",line(scenario.get("lines").get(0)));
        record(code+"-create-request",create);
        var draft=request(code+"-created",post("/documentos-comerciais").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(create)),201);
        long id=draft.get("id").asLong();String uri="/documentos-comerciais/"+id;
        for(int i=1;i<scenario.get("lines").size();i++) {
            var input=line(scenario.get("lines").get(i));record(code+"-line-"+i+"-request",input);
            request(code+"-line-"+i+"-created",post(uri+"/linhas").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(input)),201);
        }
        var before=request(code+"-draft",get(uri),200);
        var expected=scenario.get("totals");
        amount(before,"valorBruto",expected,"gross");amount(before,"valorDesconto",expected,"discount");
        amount(before,"valorTotal",expected,"total");amount(before,"valorIvaTotal",expected,"tax");
        record(code+"-emission-request",Map.of("emissorId","M3ADMIN","recalculoConfirmado",true));
        request(code+"-emission-response",post(uri+"/emitir").contentType(MediaType.APPLICATION_JSON)
                .content("{\"emissorId\":\"M3ADMIN\",\"recalculoConfirmado\":true}"),200);
        var emitted=request(code+"-emitted",get(uri),200);
        var lines=request(code+"-lines",get(uri+"/linhas"),200);
        assertThat(emitted.get("estado").asText()).isEqualTo("EMITIDO");
        assertThat(emitted.get("moedaId").asText()).isEqualTo("EUR");
        assertThat(emitted.get("fiscalSnapshotVersion").asInt()).isGreaterThan(0);
        amount(emitted,"valorBruto",expected,"gross");amount(emitted,"valorDesconto",expected,"discount");
        amount(emitted,"valorSujeitoNormal",expected,"base");amount(emitted,"valorIvaNormal",expected,"tax");
        amount(emitted,"valorIvaTotal",expected,"tax");amount(emitted,"valorTotal",expected,"total");
        var header=db.queryForMap("select * from documento_comercial where id=?",id);
        var persisted=db.queryForList("select * from linha_documento_comercial where id_documento_comercial=? order by numero_linha",id);
        record(code+"-persisted",Map.of("document",header,"lines",persisted));
        persistedAmount(header,"valor_bruto",expected,"gross");persistedAmount(header,"valor_desconto",expected,"discount");
        persistedAmount(header,"valor_sujeito_normal",expected,"base");persistedAmount(header,"valor_iva_normal",expected,"tax");
        persistedAmount(header,"valor_iva_total",expected,"tax");persistedAmount(header,"valor_total",expected,"total");
        assertThat(lines.size()).isEqualTo(scenario.get("lines").size());assertThat(persisted).hasSize(lines.size());
        for(int i=0;i<lines.size();i++) {
            var ex=scenario.get("lines").get(i).get("expected");var l=lines.get(i);var sql=persisted.get(i);
            assertThat(l.get("numeroLinha").asInt()).isEqualTo(i+1);
            amount(l,"valorBruto",ex,"gross");amount(l,"valorDesconto",ex,"discount");amount(l,"valorLinha",ex,"base");
            amount(l,"baseTributavel",ex,"base");amount(l,"valorImposto",ex,"tax");amount(l,"totalLinha",ex,"total");
            amount(l.get("resultadoFiscal"),"ivaCalculado",ex,"tax");amount(l.get("resultadoFiscal"),"ivaLiquidado",ex,"tax");
            persistedAmount(sql,"valor_bruto",ex,"gross");persistedAmount(sql,"valor_desconto",ex,"discount");
            persistedAmount(sql,"base_tributavel",ex,"base");persistedAmount(sql,"valor_imposto",ex,"tax");persistedAmount(sql,"total_linha",ex,"total");
        }
        String payload=(String)header.get("qr_payload");assertThat(emitted.get("qrPayload").asText()).isEqualTo(payload);
        var qr=new LinkedHashMap<String,String>();
        for(String part:payload.split("\\*")) {
            String[] field=part.split(":",2);assertThat(field).hasSize(2);assertThat(qr.put(field[0],field[1])).isNull();
        }
        assertThat(qr).containsEntry("D","FT").containsEntry("I1","PT")
                .containsEntry("I7",expected.get("base").asText()).containsEntry("I8",expected.get("tax").asText())
                .containsEntry("N",expected.get("tax").asText()).containsEntry("O",expected.get("total").asText());
        assertThat(qr).doesNotContainKeys("I2","I3","I4","I5","I6","P");
        record(code+"-qr-fields",qr);
        var type=db.queryForMap("select codigo_fiscal,area_gestao,entidade,sinal_contabilistico,liquidacao_imediata,fiscal_catalog_code,fiscal_catalog_revision from tipodocumento where id='GFT'");
        assertThat(type.get("codigo_fiscal")).isEqualTo("FT");assertThat(type.get("fiscal_catalog_code")).isEqualTo("FT");
        assertThat(((Number)type.get("fiscal_catalog_revision")).longValue()).isEqualTo(1);
        assertThat(type.get("liquidacao_imediata")).isEqualTo(false);record(code+"-type",type);
        record(code+"-pass",Map.of("case",code,"documentId",id,"result","PASS"));
    }
}
