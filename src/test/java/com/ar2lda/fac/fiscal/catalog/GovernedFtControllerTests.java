package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.AbstractResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;

/** Synthetic 98/9/2 and 97/8/1 tuples test mechanics, not fiscal homologation. */
@SpringBootTest(properties={"fac.security.enabled=true","spring.jpa.show-sql=false"})
@AutoConfigureMockMvc @Transactional @Import(GovernedFtControllerTests.Configuration.class)
@WithMockUser(authorities="CONFIGURACAO_GERIR")
class GovernedFtControllerTests {
    static String source;
    @TestConfiguration static class Configuration {
        @Bean @Primary FiscalCatalog governedTestCatalog(JdbcTemplate db) {
            return new FiscalCatalog(db,new AbstractResource() {
                public String getDescription(){return "synthetic governed contract fixture";}
                public InputStream getInputStream(){return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));}
            });
        }
    }
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate db;
    @Autowired ObjectMapper json;
    @Autowired FiscalConfigurationReadiness readiness;
    long operationsBefore;
    String otherDefinitions;
    static String definition(long rev) {
        return "{\"schemaVersion\":1,\"fiscalCode\":\"FT\",\"revision\":"+rev+",\"semantics\":{\"allowedConfigurations\":["
            +"{\"codigoFiscal\":\"FT\",\"areaGestao\":98,\"entidade\":9,\"sinalContabilistico\":2,\"liquidacaoImediata\":false},"
            +"{\"codigoFiscal\":\"FT\",\"areaGestao\":97,\"entidade\":8,\"sinalContabilistico\":1,\"liquidacaoImediata\":false}]}}";
    }
    @BeforeEach void fixture() {
        operationsBefore=db.queryForObject("select count(*) from financial_operation",Long.class);
        otherDefinitions=db.query("select definition::text from fiscal_catalog_revision order by fiscal_code,revision",
            (rs,n)->rs.getString(1)).stream().map(x->","+x).collect(java.util.stream.Collectors.joining());
        db.update("insert into fiscal_catalog_revision values('FT',1,?::jsonb)",definition(1));
        source="{\"schemaVersion\":1,\"revisions\":["+definition(1)+otherDefinitions+"],\"creationBindings\":{\"ftDeferred\":{\"fiscalCode\":\"FT\",\"revision\":1}}}";
    }
    ObjectNode create(String id) {
        return json.createObjectNode().put("id",id).put("descricao","Synthetic contract fixture").put("codigoFiscal","FT")
            .put("areaGestao",98).put("entidade",9).put("sinalContabilistico",2).put("liquidacaoImediata",false);
    }
    void postType(ObjectNode data,int expected)throws Exception {
        mvc.perform(post("/tipos-documento").contentType("application/json").content(data.toString())).andExpect(status().is(expected));
    }
    void governed()throws Exception {postType(create("G01"),201);}
    @Test void creationPersistsAndReturnsExplicitBinding()throws Exception {
        governed();mvc.perform(get("/tipos-documento/G01")).andExpect(status().isOk())
            .andExpect(jsonPath("$.fiscalCatalogCode").value("FT")).andExpect(jsonPath("$.fiscalCatalogRevision").value(1))
            .andExpect(jsonPath("$.configuracaoBloqueada").value(false));
        assertThat(db.queryForObject("select fiscal_catalog_revision from tipodocumento where id='G01'",Long.class)).isEqualTo(1);
    }
    @Test void releasePointerNeverSelectsLatestOrRetargetsExistingTypes()throws Exception {
        db.update("insert into fiscal_catalog_revision values('FT',2,?::jsonb)",definition(2));
        source=source.replace("\"revisions\":["+definition(1),"\"revisions\":["+definition(2)+","+definition(1));
        governed();source=source.replace("\"revision\":1}}}","\"revision\":2}}}");
        mvc.perform(get("/tipos-documento/G01")).andExpect(jsonPath("$.fiscalCatalogRevision").value(1));
        postType(create("G02"),201);
        assertThat(db.queryForObject("select fiscal_catalog_revision from tipodocumento where id='G02'",Long.class)).isEqualTo(2);
    }
    @Test void absentProductionPointerRefusesFtAndDoesNotFallback()throws Exception {
        source="{\"schemaVersion\":1,\"revisions\":["+definition(1)+"]}";
        postType(create("G01"),409);assertThat(db.queryForObject("select count(*) from tipodocumento where id='G01'",Long.class)).isZero();
    }
    @Test void missingModalityIsNotAnImplicitBusinessChoice()throws Exception {
        var d=create("G01");d.remove("liquidacaoImediata");postType(d,400);
    }
    @Test void clientCannotSupplyRevisionOrDefinition()throws Exception {
        postType(create("G01").put("fiscalCatalogRevision",1),400);
    }
    @Test void invalidTupleRefusedBeforeAnyPersistence()throws Exception {
        postType(create("G01").put("areaGestao",99),400);
        assertThat(db.queryForObject("select count(*) from tipodocumento where id='G01'",Long.class)).isZero();
    }
    @ParameterizedTest @ValueSource(strings={"codigoFiscal","areaGestao","entidade","sinalContabilistico","liquidacaoImediata"})
    void allClassificationFieldsRemainConstrainedBeforeFirstEmission(String field)throws Exception {
        governed();var d=create("G01");d.remove("id");
        switch(field){case "codigoFiscal"->d.put(field,"NC");case "liquidacaoImediata"->d.put(field,true);default->d.put(field,field.equals("areaGestao")?99:1);}
        mvc.perform(put("/tipos-documento/G01").contentType("application/json").content(d.toString())).andExpect(status().isBadRequest());
        assertThat(db.queryForObject("select area_gestao from tipodocumento where id='G01'",Integer.class)).isEqualTo(98);
    }
    @Test void compatibleTupleAndDescriptionCanChangeWithoutRebinding()throws Exception {
        governed();var d=create("G01");d.remove("id");d.put("areaGestao",97).put("entidade",8).put("sinalContabilistico",1).put("descricao","Changed description");
        mvc.perform(put("/tipos-documento/G01").contentType("application/json").content(d.toString())).andExpect(status().isNoContent());
        assertThat(db.queryForObject("select fiscal_catalog_revision from tipodocumento where id='G01'",Long.class)).isEqualTo(1);
    }
    @ParameterizedTest @ValueSource(strings={"RC","RL","GUI"})
    void outOfPerimeterCodeNullStillWorksAndNeverReceivesAFiscalIdentity(String id)throws Exception {
        source="missing invalid manifest is deliberately unused";
        var d=create(id);d.remove("codigoFiscal");d.remove("liquidacaoImediata");d.put("areaGestao",id.equals("GUI")?1:3);
        postType(d,201);mvc.perform(get("/tipos-documento/"+id)).andExpect(jsonPath("$.codigoFiscal").isEmpty()).andExpect(jsonPath("$.fiscalCatalogCode").isEmpty());
    }
    @Test void conversionIntoFtRequiresNewConfigurationInsteadOfHistoricalAssignment()throws Exception {
        var d=create("U01");d.remove("codigoFiscal");d.put("areaGestao",3);postType(d,201);d=create("U01");d.remove("id");
        mvc.perform(put("/tipos-documento/U01").contentType("application/json").content(d.toString())).andExpect(status().isConflict());
    }
    @Test void readinessInventoriesUnclassifiedEmissionPathsWithoutInferringFromId()throws Exception {
        var d=create("FT");d.remove("codigoFiscal");d.put("areaGestao",2);postType(d,201);
        var r=readiness.inspect();assertThat(r.configurationReady()).isFalse();assertThat(r.operationalActivationPermitted()).isFalse();
        assertThat(r.rows()).anySatisfy(x->{assertThat(x.id()).isEqualTo("FT");assertThat(x.fiscalCode()).isNull();assertThat(x.findings()).contains(FiscalConfigurationReadiness.Finding.UNCLASSIFIED_EMISSION_PATH);});
        assertThat(db.queryForObject("select count(*) from financial_operation",Long.class)).isEqualTo(operationsBefore);
    }
    @Test @WithMockUser(authorities="DOCUMENTO_CONSULTAR") void permissionIsNotExpanded()throws Exception {postType(create("G01"),403);}
    @Test void anonymousCannotCreateConfigurations()throws Exception {
        mvc.perform(post("/tipos-documento").with(anonymous()).contentType("application/json").content(create("G01").toString())).andExpect(status().isUnauthorized());
    }
}
