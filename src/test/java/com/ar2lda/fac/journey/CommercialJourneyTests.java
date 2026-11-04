package com.ar2lda.fac.journey;

import com.fasterxml.jackson.databind.*;
import com.ar2lda.fac.service.FiscalDocumentSignatureSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real committed HTTP transactions, no outer test transaction, fiscal source or catalog double. */
@SpringBootTest(properties={"fac.security.enabled=true","spring.jpa.show-sql=false","fac.demo.seed-on-startup=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named="M03_DISPOSABLE",matches="true")
class CommercialJourneyTests {
    @Autowired JdbcTemplate db;
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    @Autowired ApplicationContext context;
    String admin,operator,consulta;
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
        admin=login("M3ADMIN");operator=login("M3OPER");consulta=login("M3READ");
        http.perform(auth(post("/empresa"),admin).contentType(MediaType.APPLICATION_JSON).content(company("Empresa sintetica M03"))).andExpect(status().isCreated());
        customer=body(http.perform(auth(post("/clientes"),admin).contentType(MediaType.APPLICATION_JSON).content(client("Cliente sintetico M03"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        http.perform(auth(post("/artigos"),admin).contentType(MediaType.APPLICATION_JSON).content(article(true))).andExpect(status().isCreated());
        // An unclassified legacy-shaped TEST fixture, NOT a governed FT or fiscal approval.
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,liquidacao_imediata) values('M3N','Fixture nao homologada M03',1,1,1,false)");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('M3N','SYNTH','Apenas teste negativo',0,'SYNM0301','2026-01-01'),('M3N','NOAT','Sem codigo de teste',0,null,null)");
    }
    @BeforeEach void cleanDrafts() throws Exception {
        for(Long id:db.queryForList("select id from documento_comercial",Long.class))
            http.perform(auth(delete("/documentos-comerciais/"+id),admin)).andExpect(status().isNoContent());
        db.update("delete from parametros_documento_comercial");db.update("delete from parametros_cliente");
    }
    @AfterEach void financialGatesRemainClosed() {
        assertThat(context.getBeansOfType(FiscalDocumentSignatureSource.class)).isEmpty();
        for(String table:List.of("fiscal_catalog_revision","pendente","documento_financeiro","financial_position","financial_operation","financial_document_origin","financial_receipt"))
            assertThat(db.queryForObject("select count(*) from "+table,Long.class)).as(table).isZero();
        assertThat(db.queryForObject("select coalesce(sum(numerador),0) from serie",Long.class)).isZero();
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
        {"documento":{"tipoDocumentoId":"M3N","serie":"%s","dataEmissao":"2026-11-03","clienteId":%d,"armazemCargaId":"M3A","moedaId":"EUR","rivaId":"CON","pPagamentoId":"P30","transporteId":"M3T"},"linha":%s}
        """.formatted(series,customer,line(item,quantity,10));}
    long create(String series,String token) throws Exception {return body(http.perform(auth(post("/documentos-comerciais"),token).contentType(MediaType.APPLICATION_JSON).content(draft(series,"M3ART",1))).andExpect(status().isCreated()).andReturn()).get("id").asLong();}
    String doc(long id){return "/documentos-comerciais/"+id;}
    @Test void authenticatedMastersDraftLinesQueriesAndAudit() throws Exception {
        http.perform(auth(put("/empresa"),admin).contentType(MediaType.APPLICATION_JSON).content(company("Empresa M03 actualizada"))).andExpect(status().isOk());
        http.perform(auth(get("/empresa"),admin)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Empresa M03 actualizada"));
        http.perform(auth(put("/clientes/"+customer),admin).contentType(MediaType.APPLICATION_JSON).content(client("Cliente M03 actualizado"))).andExpect(status().isNoContent());
        http.perform(auth(get("/clientes/"+customer),consulta)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Cliente M03 actualizado"));
        http.perform(auth(put("/artigos/M3ART"),operator).contentType(MediaType.APPLICATION_JSON).content(article(false))).andExpect(status().isNoContent());
        http.perform(auth(get("/artigos/M3ART"),consulta)).andExpect(status().isOk());
        long id=create("SYNTH",operator);
        long first=body(http.perform(auth(get(doc(id)+"/linhas"),consulta)).andExpect(status().isOk()).andReturn()).get(0).get("id").asLong();
        long second=body(http.perform(auth(post(doc(id)+"/linhas"),operator).contentType(MediaType.APPLICATION_JSON).content(line("M3ART",2,5))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        http.perform(auth(get(doc(id)),consulta)).andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(24.6));
        http.perform(auth(put(doc(id)+"/linhas/ordem"),operator).contentType(MediaType.APPLICATION_JSON).content("{\"linhaIds\":["+second+","+first+"]}")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(second));
        http.perform(auth(put(doc(id)),operator).contentType(MediaType.APPLICATION_JSON).content("{\"dataEmissao\":\"2026-11-03\",\"armazemCargaId\":\"M3A\",\"observacoes\":\"M03 rascunho\"}")).andExpect(status().isNoContent());
        http.perform(auth(get(doc(id)),consulta)).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("RASCUNHO")).andExpect(jsonPath("$.observacoes").value("M03 rascunho"));
        http.perform(auth(get("/documentos-comerciais?estado=RASCUNHO&size=20"),consulta)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        http.perform(auth(get("/auditoria?entidadeTipo=DOCUMENTO_COMERCIAL&entidadeId="+id),admin)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements",org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        http.perform(auth(delete(doc(id)),operator)).andExpect(status().isNoContent());
        http.perform(auth(get(doc(id)),consulta)).andExpect(status().isNotFound());
        assertThat(db.queryForObject("select count(*) from auditoria_evento where tipo_evento='DOCUMENTO_CRIADO' and entidade_id=? and utilizador_id='M3OPER'",Long.class,Long.toString(id))).isEqualTo(1);
    }
    @Test void invalidFirstLineRollsBackHeaderAndAuditWithoutOuterTestTransaction() throws Exception {
        long documents=db.queryForObject("select count(*) from documento_comercial",Long.class);
        long audit=db.queryForObject("select count(*) from auditoria_evento where tipo_evento='DOCUMENTO_CRIADO'",Long.class);
        http.perform(auth(post("/documentos-comerciais"),operator).contentType(MediaType.APPLICATION_JSON).content(draft("SYNTH","MISSING",1))).andExpect(status().isNotFound());
        http.perform(auth(post("/documentos-comerciais"),operator).contentType(MediaType.APPLICATION_JSON).content(draft("SYNTH","M3ART",0))).andExpect(status().isBadRequest());
        assertThat(db.queryForObject("select count(*) from documento_comercial",Long.class)).isEqualTo(documents);
        assertThat(db.queryForObject("select count(*) from auditoria_evento where tipo_evento='DOCUMENTO_CRIADO'",Long.class)).isEqualTo(audit);
    }
    @Test void realProfilesRestrictWritesAndAdministrativeReads() throws Exception {
        http.perform(get("/documentos-comerciais")).andExpect(status().isUnauthorized());
        http.perform(auth(post("/documentos-comerciais"),consulta).contentType(MediaType.APPLICATION_JSON).content(draft("SYNTH","M3ART",1))).andExpect(status().isForbidden());
        http.perform(auth(put("/empresa"),operator).contentType(MediaType.APPLICATION_JSON).content(company("Denied"))).andExpect(status().isForbidden());
        http.perform(auth(get("/auditoria"),consulta)).andExpect(status().isForbidden());
        long id=create("SYNTH",operator);
        http.perform(auth(get(doc(id)),consulta)).andExpect(status().isOk());
        http.perform(auth(post("/clientes"),consulta).contentType(MediaType.APPLICATION_JSON).content(client("Denied"))).andExpect(status().isForbidden());
    }
    @Test void absentSignatureRefuses409AndRollsBackNumberAndFiscalFields() throws Exception {
        long id=create("SYNTH",admin);
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType(MediaType.APPLICATION_JSON).content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.message",org.hamcrest.Matchers.containsString("assinatura")));
        http.perform(auth(get(doc(id)),admin)).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("RASCUNHO")).andExpect(jsonPath("$.numeroDocumento").doesNotExist()).andExpect(jsonPath("$.qrPayload").doesNotExist());
        assertThat(db.queryForObject("select count(*) from documento_comercial where numero_documento_completo is not null or atcud is not null or qr_payload is not null",Long.class)).isZero();
        assertThat(db.queryForObject("select count(*) from auditoria_evento where tipo_evento='DOCUMENTO_EMITIDO'",Long.class)).isZero();
    }
    @Test void missingSeriesValidationCodeIsRefusedBeforeNumbering() throws Exception {
        long id=create("NOAT",operator);
        http.perform(auth(post(doc(id)+"/emitir"),operator).contentType(MediaType.APPLICATION_JSON).content("{\"emissorId\":\"M3OPER\"}")).andExpect(status().isBadRequest());
        assertThat(db.queryForObject("select numero_documento is null from documento_comercial where id=?",Boolean.class,id)).isTrue();
    }
    @Test void governedFtCreationRemainsClosedWithEmptyCatalog() throws Exception {
        http.perform(auth(post("/tipos-documento"),admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":\"M3F\",\"descricao\":\"Sintetico nao homologado\",\"codigoFiscal\":\"FT\",\"areaGestao\":2,\"entidade\":1,\"sinalContabilistico\":1,\"liquidacaoImediata\":false}"))
            .andExpect(status().isConflict());
        assertThat(db.queryForObject("select count(*) from tipodocumento where id='M3F'",Long.class)).isZero();
    }
    @Test void nonFiscalExportsRespectExistingPermissionsAndFormats() throws Exception {
        String csv=http.perform(auth(get("/exportacoes/clientes?formato=csv"),consulta)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(csv).contains("Cliente","client@example.test");
        byte[] xlsx=http.perform(auth(get("/exportacoes/artigos?formato=xlsx"),operator)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try(var workbook=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(xlsx))){assertThat(workbook.getSheet("dados").getLastRowNum()).isGreaterThanOrEqualTo(1);}
        http.perform(auth(get("/exportacoes/clientes?formato=unsupported"),admin)).andExpect(status().isBadRequest());
    }
    @Test void childScopeAndUnsupportedSortAreControlledErrors() throws Exception {
        long a=create("SYNTH",operator),b=create("SYNTH",operator);
        long line=body(http.perform(auth(get(doc(a)+"/linhas"),consulta)).andExpect(status().isOk()).andReturn()).get(0).get("id").asLong();
        http.perform(auth(get(doc(b)+"/linhas/"+line),consulta)).andExpect(status().isNotFound());
        http.perform(auth(delete(doc(b)+"/linhas/"+line),operator)).andExpect(status().isNotFound());
        http.perform(auth(get("/documentos-comerciais?sort=unsupported,asc"),admin)).andExpect(status().isBadRequest());
        assertThat(db.queryForObject("select count(*) from linha_documento_comercial where id=?",Long.class,line)).isEqualTo(1);
    }
    @Test void parameterConfigurationAndMissingDependenciesAreExplicit() throws Exception {
        http.perform(auth(post("/parametros-cliente"),admin).contentType(MediaType.APPLICATION_JSON).content("{\"paisId\":\"PT\",\"moedaId\":\"EUR\",\"rivaId\":\"CON\",\"transporteId\":\"M3T\",\"retencao\":false}")).andExpect(status().isCreated());
        http.perform(auth(get("/parametros-cliente"),admin)).andExpect(status().isOk()).andExpect(jsonPath("$.moedaId").value("EUR"));
        http.perform(auth(post("/parametros-cliente"),admin).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isConflict());
        http.perform(auth(post("/parametros-documento-comercial"),admin).contentType(MediaType.APPLICATION_JSON).content("{\"tipoDocumentoId\":\"M3N\",\"serie\":\"SYNTH\",\"armazemCargaId\":\"M3A\"}")).andExpect(status().isCreated());
        http.perform(auth(get("/parametros-documento-comercial"),admin)).andExpect(status().isOk()).andExpect(jsonPath("$.serie").value("SYNTH"));
        http.perform(auth(put("/parametros-cliente"),admin).contentType(MediaType.APPLICATION_JSON).content("{\"transporteId\":\"BAD\"}")).andExpect(status().isNotFound());
    }
}
