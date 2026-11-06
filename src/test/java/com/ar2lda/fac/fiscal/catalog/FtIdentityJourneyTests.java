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

/** M4-04: one normal MockMvc emission, persisted identity and actual PDF QR; synthetic signer only. */
@SpringBootTest(properties={"fac.security.enabled=true","spring.jpa.show-sql=false","fac.demo.seed-on-startup=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named="M404_DISPOSABLE",matches="true")
@Import({FtIdentityJourneyTests.CatalogFixture.class, FiscalSignatureTestConfiguration.class})
class FtIdentityJourneyTests {
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
        assertThat(db.queryForObject("select current_database()",String.class)).startsWith("tuuli_m404_").endsWith("_test");
        assertThat(db.queryForObject("select system_identifier::text from pg_control_system()",String.class)).isEqualTo(System.getenv("M404_EXPECTED_CLUSTER"));
        db.update("insert into codpostal(id,nome) values('1000-010','Sintetico') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('M3T','Sintetico')");
        db.update("insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais) values('M3A','Sintetico','Rua Teste','1000-010','Sintetico','PT')");
        db.update("insert into mpagamento(id,nome) values('M3P','Sintetico')");
        family=db.queryForObject("insert into familia(descricao) values('Familia sintetica M404') returning id",Long.class);
        for(var row:List.<String[]>of(new String[]{"M3ADMIN","ADMINISTRADOR"})) {
            db.update("insert into utilizador(codigo,nome,email,password_hash,inativo,papel) values(?,?,?,?,false,?)",row[0],"Synthetic",row[0].toLowerCase()+"@example.test",encoder.encode("Journey8!Fixture"),row[1]);
        }
        admin=login("M3ADMIN");
        http.perform(auth(post("/empresa"),admin).contentType(MediaType.APPLICATION_JSON).content(company("Empresa sintetica M404"))).andExpect(status().isCreated());
        customer=body(http.perform(auth(post("/clientes"),admin).contentType(MediaType.APPLICATION_JSON).content(client("Cliente sintetico M404"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        http.perform(auth(post("/artigos"),admin).contentType(MediaType.APPLICATION_JSON).content(article(true))).andExpect(status().isCreated());

        db.update("insert into fiscal_catalog_revision values('FT',1,?::jsonb)", DEFINITION);
        http.perform(auth(post("/tipos-documento"),admin).contentType(MediaType.APPLICATION_JSON).content("""
            {"id":"GFT","descricao":"Synthetic governed FT","codigoFiscal":"FT","areaGestao":2,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":false}
            """)).andExpect(status().isCreated());
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('GFT','M404A','Synthetic M404 only',0,'SYNM404A','2026-01-01')");
    }
    String company(String name){return """
        {"nome":"%s","nif":"509999999","morada":"Rua Teste","codPostalId":"1000-010","localidade":"Sintetico","paisId":"PT","capitalSocial":0,"matriculaRegistoComercial":"CRC SYNTH","cae":"62010","descricaoCae":"Sintetico","email":"company@example.test"}
        """.formatted(name);}
    String client(String name){return """
        {"nome":"%s","morada":"Rua Teste","localidade":"Sintetico","nif":"509123456","email":"client@example.test","retencao":false,"inativo":false,"codPostalId":"1000-010","paisId":"PT","moedaId":"EUR","rivaId":"CON","transporteId":"M3T"}
        """.formatted(name);}
    String article(boolean create){return """
        {%s"descricao":"Artigo sintetico M404","tipoArtigo":"SERVICO","unidade":"UN","familiaId":%d,"ivaCompraId":"NORMAL","ivaVendaId":"NORMAL","pvp":10,"inativo":false,"retencao":false}
        """.formatted(create?"\"codigo\":\"M3ART\",":"",family);}


    Path evidence() throws Exception {
        Path p=Path.of(System.getenv("M404_EVIDENCE"));Files.createDirectories(p);return p;
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
    Map<String,String> fields(String payload) {
        var fields=new LinkedHashMap<String,String>();
        for(String part:payload.split("\\*")) {
            var pair=part.split(":",2);assertThat(pair).hasSize(2);
            assertThat(fields.put(pair[0],pair[1])).as("No duplicate QR field").isNull();
        }
        return fields;
    }
    @Test void governedFtIdentityMatchesPersistedSnapshotAndActualPdfQr() throws Exception {
        JsonNode expected;
        try(var in=getClass().getResourceAsStream("/m404/expected.json")) {
            expected=json.readTree(Objects.requireNonNull(in));
        }
        record("M404-01-expected",expected);
        var type=db.queryForMap("select id,codigo_fiscal,area_gestao,entidade,sinal_contabilistico,liquidacao_imediata,fiscal_catalog_code,fiscal_catalog_revision from tipodocumento where id='GFT'");
        record("M404-01-configuration",type);
        assertThat(type).containsEntry("id","GFT").containsEntry("codigo_fiscal","FT")
            .containsEntry("area_gestao",2).containsEntry("entidade",1).containsEntry("sinal_contabilistico",1)
            .containsEntry("liquidacao_imediata",false).containsEntry("fiscal_catalog_code","FT");
        assertThat(((Number)type.get("fiscal_catalog_revision")).longValue()).isEqualTo(1);
        record("M404-01-catalog",db.queryForList("select * from fiscal_catalog_revision"));
        var seriesBefore=db.queryForMap("select id_tipo_documento,serie,numerador,codigo_at from serie where id_tipo_documento='GFT' and serie='M404A'");
        assertThat(((Number)seriesBefore.get("numerador")).longValue()).isZero();
        assertThat(seriesBefore).containsEntry("codigo_at",expected.get("validationCode").asText());
        record("M404-01-series-before",seriesBefore);
        var create=json.createObjectNode();
        create.set("documento",json.createObjectNode().put("tipoDocumentoId","GFT").put("serie","M404A")
            .put("dataEmissao","2026-11-06").put("clienteId",customer).put("armazemCargaId","M3A")
            .put("moedaId","EUR").put("rivaId","CON").put("pPagamentoId","P30").put("transporteId","M3T"));
        create.set("linha",json.createObjectNode().put("artigoId","M3ART").put("quantidade",1)
            .put("precoUnitario",10).put("tipoDesconto","VALOR").put("desconto",0).put("tipoTaxaIvaId","NORMAL"));
        record("M404-01-create-request",create);
        var draft=request("M404-01-created",post("/documentos-comerciais").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(create)),201);
        long id=draft.get("id").asLong();String uri="/documentos-comerciais/"+id;
        var emit=json.createObjectNode().put("emissorId","M3ADMIN").put("recalculoConfirmado",true);
        record("M404-01-emission-request",emit);
        request("M404-01-emission-response",post(uri+"/emitir").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(emit)),200);
        var emitted=request("M404-01-read-after-emission",get(uri),200);
        assertThat(emitted.get("estado").asText()).isEqualTo("EMITIDO");
        assertThat(emitted.get("tipoDocumentoId").asText()).isEqualTo(expected.get("internalType").asText());
        assertThat(emitted.get("tipoDocumentoCodigoFiscal").asText()).isEqualTo(expected.get("fiscalType").asText());
        assertThat(emitted.get("moedaId").asText()).isEqualTo("EUR");
        assertThat(emitted.get("serie").asText()).isEqualTo(expected.get("series").asText());
        assertThat(emitted.get("numeroDocumento").asLong()).isEqualTo(expected.get("sequence").asLong());
        assertThat(emitted.get("numeroDocumentoCompleto").asText()).isEqualTo(expected.get("number").asText());
        assertThat(emitted.get("atcud").asText()).isEqualTo(expected.get("atcud").asText());
        var persisted=db.queryForMap("select * from documento_comercial where id=?",id);
        record("M404-01-persisted",persisted);
        assertThat(persisted).containsEntry("id_tipo_documento",expected.get("internalType").asText())
            .containsEntry("tipo_documento_codigo",expected.get("internalType").asText())
            .containsEntry("tipo_documento_codigo_fiscal",expected.get("fiscalType").asText())
            .containsEntry("serie",expected.get("series").asText())
            .containsEntry("numero_documento_completo",expected.get("number").asText())
            .containsEntry("codigo_validacao_at",expected.get("validationCode").asText())
            .containsEntry("atcud",expected.get("atcud").asText()).containsEntry("moeda_codigo","EUR");
        assertThat(((Number)persisted.get("numero_documento")).longValue()).isEqualTo(1);
        assertThat(((Number)persisted.get("fiscal_snapshot_version")).intValue()).isGreaterThan(0);
        String payload=(String)persisted.get("qr_payload");
        assertThat(emitted.get("qrPayload").asText()).isEqualTo(payload);
        var qr=fields(payload);record("M404-01-persisted-qr-fields",qr);
        assertThat(qr).containsEntry("D",expected.get("D").asText())
            .containsEntry("G",expected.get("G").asText()).containsEntry("H",expected.get("H").asText());
        assertThat(qr.get("D")).isNotEqualTo(expected.get("internalType").asText());
        assertThat(qr.get("G")).startsWith("GFT ");assertThat(qr.get("H")).doesNotContain("ATCUD:");
        var pdfResult=http.perform(auth(get(uri+"/pdf"),admin)).andExpect(status().isOk()).andReturn();
        byte[] pdf=pdfResult.getResponse().getContentAsByteArray();
        Files.write(evidence().resolve("M404-01-issued-synthetic.pdf"),pdf);
        record("M404-01-pdf-response",Map.of("status",pdfResult.getResponse().getStatus(),"bytes",pdf.length,"contentType",pdfResult.getResponse().getContentType()));
        try(var parsed=org.apache.pdfbox.Loader.loadPDF(pdf)) {
            String text=new org.apache.pdfbox.text.PDFTextStripper().getText(parsed);
            Files.writeString(evidence().resolve("M404-01-pdf-text.txt"),text);
            String normalized=text.replaceAll("\\s+"," ");
            assertThat(normalized).contains(expected.get("number").asText(),"ATCUD: "+expected.get("atcud").asText());
            assertThat(normalized).contains("Synthetic governed FT"); // Preserved configured description, not a mandated fiscal label.
            var codes=new ArrayList<com.ar2lda.fac.testinfra.PdfQrInspection.Qr>();
            for(var page:parsed.getPages()) codes.addAll(com.ar2lda.fac.testinfra.PdfQrInspection.inspect(page));
            assertThat(codes).hasSize(1);
            assertThat(codes.getFirst().payload()).isEqualTo(payload);
            var image=new org.apache.pdfbox.rendering.PDFRenderer(parsed).renderImageWithDPI(0,300);
            javax.imageio.ImageIO.write(image,"png",evidence().resolve("M404-01-rendered-page-1.png").toFile());
            var bitmap=new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(
                new com.google.zxing.client.j2se.BufferedImageLuminanceSource(image)));
            var decoded=new com.google.zxing.qrcode.QRCodeReader().decode(bitmap,Map.of(com.google.zxing.DecodeHintType.TRY_HARDER,true)).getText();
            assertThat(decoded).isEqualTo(payload);
            var imageQr=fields(decoded);
            assertThat(imageQr).containsEntry("D",expected.get("D").asText())
                .containsEntry("G",expected.get("G").asText()).containsEntry("H",expected.get("H").asText());
            record("M404-01-pdf-qr-fields",imageQr);
            record("M404-01-pdf-inspection",Map.of("pages",parsed.getNumberOfPages(),"qrCount",codes.size(),"decodedFromImage",true,"qr",codes));
        }
        var after=db.queryForMap("select * from documento_comercial where id=?",id);
        assertThat(after).isEqualTo(persisted); // PDF audit is allowed, document identity must not change.
        var seriesAfter=db.queryForMap("select id_tipo_documento,serie,numerador,codigo_at from serie where id_tipo_documento='GFT' and serie='M404A'");
        assertThat(((Number)seriesAfter.get("numerador")).longValue()).isEqualTo(1);
        record("M404-01-series-after",seriesAfter);
        record("M404-01-pass",Map.of("result","PASS","documentId",id,"scope","MockMvc/test signer/synthetic FT deferred EUR identity only"));
    }
}
