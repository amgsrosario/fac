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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named="M03_DISPOSABLE",matches="true")
@Import({CommercialReprintJourneyTests.CatalogFixture.class, FiscalSignatureTestConfiguration.class})
class CommercialReprintJourneyTests {
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
        assertThat(db.queryForObject("select current_database()",String.class)).startsWith("tuuli_m03_").endsWith("_test");
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

    Map<String,Object> persistentRows() {
        Map<String,Object> result=new TreeMap<>();
        for (String table:db.queryForList("select tablename from pg_tables where schemaname='public' and tablename<>'auditoria_evento' order by tablename",String.class)) {
            assertThat(table).matches("[a-zA-Z0-9_]+");
            result.put(table,db.queryForList("select row_to_json(t)::text data from public.\"" + table + "\" t order by row_to_json(t)::text"));
        }
        return result;
    }
    byte[] reprint(long id, String label, String payload, boolean cancelled) throws Exception {
        Map<String,Object> before=state(id);
        Map<String,Object> allRows=persistentRows();
        long audit=db.queryForObject("select count(*) from auditoria_evento",Long.class);
        org.mockito.Mockito.clearInvocations(signer);
        byte[] pdf=http.perform(auth(get(doc(id)+"/pdf"),admin))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertThat(state(id)).isEqualTo(before);
        assertThat(persistentRows()).isEqualTo(allRows);
        assertThat(db.queryForObject("select count(*) from auditoria_evento",Long.class)).isEqualTo(audit+1);
        org.mockito.Mockito.verifyNoInteractions(signer);
        Files.write(Path.of(System.getenv("M402_EVIDENCE"),label+".pdf"),pdf);
        try(var parsed=org.apache.pdfbox.Loader.loadPDF(pdf)) {
            String text=new org.apache.pdfbox.text.PDFTextStripper().getText(parsed);
            if(cancelled) assertThat(text.toUpperCase(java.util.Locale.ROOT)).contains("ANULADO");
            var codes=com.ar2lda.fac.testinfra.PdfQrInspection.inspect(parsed.getPage(0));
            assertThat(codes).hasSize(1);
            assertThat(codes.getFirst().payload()).isEqualTo(payload);
            var image=new org.apache.pdfbox.rendering.PDFRenderer(parsed).renderImageWithDPI(0,300);
            var bitmap=new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(
                new com.google.zxing.client.j2se.BufferedImageLuminanceSource(image)));
            assertThat(new com.google.zxing.qrcode.QRCodeReader().decode(bitmap,
                Map.of(com.google.zxing.DecodeHintType.TRY_HARDER,true)).getText()).isEqualTo(payload);
        }
        return pdf;
    }
    @Test void realCancellationPreservesPersistedQrThroughCommercialReprints() throws Exception {
        long id=create("SYNTH",admin);
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        String payload=db.queryForObject("select qr_payload from documento_comercial where id=?",String.class,id);
        record("emitted",state(id));
        reprint(id,"emitted",payload,false);
        signer.unavailable=true;
        MvcResult result=http.perform(auth(post(doc(id)+"/anular"),admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"motivo\":\"Synthetic cancellation for reprint test\"}")).andExpect(status().isOk()).andReturn();
        record("cancellation-response",body(result));
        assertThat(db.queryForObject("select estado from documento_comercial where id=?",String.class,id)).isEqualTo("ANULADO");
        assertThat(db.queryForObject("select qr_payload from documento_comercial where id=?",String.class,id)).isEqualTo(payload);
        record("cancelled",state(id));
        reprint(id,"cancelled",payload,true);
        reprint(id,"cancelled-repeat",payload,true);
        record("after-reprints",state(id));
        record("audit",db.queryForList("select tipo_evento,entidade_id from auditoria_evento order by id"));
        assertThat(db.queryForObject("select count(*) from auditoria_evento where tipo_evento='DOCUMENTO_PDF_GERADO'",Long.class)).isEqualTo(3);
    }
}
