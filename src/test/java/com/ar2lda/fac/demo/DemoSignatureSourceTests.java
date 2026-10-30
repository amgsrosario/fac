package com.ar2lda.fac.demo;

import com.ar2lda.fac.controller.dto.*;
import com.ar2lda.fac.service.FiscalQrIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import javax.sql.DataSource;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPairGenerator;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DemoSignatureSourceTests {
    @TempDir Path directory;
    private DemoSignatureSource source(String profile,String database,String cluster,long incompatible)throws Exception {
        var env=new MockEnvironment();env.setActiveProfiles(profile);
        env.setProperty("fac.demo.enabled","true");env.setProperty("fac.demo.signature.authorized-cluster","7702000000000000000");
        Path key=directory.resolve("demo.pkcs8");var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);
        Files.write(key,generator.generateKeyPair().getPrivate().getEncoded());Files.setPosixFilePermissions(key,PosixFilePermissions.fromString("rw-------"));
        env.setProperty("fac.demo.signature.key-file",key.toString());
        var result=new DemoSignatureSource(env,mock(DataSource.class),new ObjectMapper().findAndRegisterModules());
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForObject("select current_database()",String.class)).thenReturn(database);
        when(jdbc.queryForObject("select system_identifier::text from pg_control_system()",String.class)).thenReturn(cluster);
        when(jdbc.queryForObject(anyString(),eq(Long.class))).thenReturn(incompatible);
        ReflectionTestUtils.setField(result,"jdbc",jdbc);return result;
    }
    @ParameterizedTest @ValueSource(strings={"prod","production","test","default"})
    void rejectsDemoProviderOutsideExclusiveDemoProfile(String profile)throws Exception {
        var provider=source(profile,"fac_demo","7702000000000000000",0);
        new ApplicationContextRunner().withPropertyValues("fac.demo.signature.enabled=true").withBean(DemoSignatureSource.class,()->provider)
                .run(context->assertThat(context).hasFailed());
    }
    @ParameterizedTest @ValueSource(strings={"fac","fac_prod","other_demo"})
    void rejectsUnauthorizedDatabaseAtStartup(String database)throws Exception {
        var provider=source("demo",database,"7702000000000000000",0);
        new ApplicationContextRunner().withPropertyValues("fac.demo.signature.enabled=true").withBean(DemoSignatureSource.class,()->provider)
                .run(context->assertThat(context).hasFailed());
    }
    @Test void rejectsDifferentCluster()throws Exception {
        assertThatThrownBy(()->source("demo","fac_demo","7702999999999999999",0).afterPropertiesSet()).isInstanceOf(IllegalStateException.class);
    }
    @Test void rejectsRealOrForeignSeriesAtStartup()throws Exception {
        var provider=source("demo","fac_demo","7702000000000000000",1);
        new ApplicationContextRunner().withPropertyValues("fac.demo.signature.enabled=true").withBean(DemoSignatureSource.class,()->provider)
                .run(context->assertThat(context).hasFailed());
    }
    @Test void syntheticPayloadCannotPassProductiveCertificateContract()throws Exception {
        var provider=source("demo","fac_demo","7702000000000000000",0);provider.afterPropertiesSet();
        var d=mock(DocumentoComercialDto.class);when(d.id()).thenReturn(1L);when(d.tipoDocumentoId()).thenReturn("FT");
        when(d.serie()).thenReturn("DEMO26");when(d.numeroDocumento()).thenReturn(1L);when(d.atcud()).thenReturn("DEMO-FT-1");
        var result=provider.commercialQr(new DocumentoComercialImpressaoDto(null,d,java.util.List.of()));
        assertThat(result.version()).isEqualTo("TUULI-DEMO-1");assertThat(result.payload()).startsWith("TUULI-DEMO-1\n")
                .contains("SEM VALIDADE FISCAL","\"Q\":\"DEMO:","\"R\":\"DEMO-SEM-CERTIFICADO\"");
        assertThatThrownBy(()->FiscalQrIdentity.derive("anything","DEMO-SEM-CERTIFICADO")).isInstanceOf(com.ar2lda.fac.exception.BadRequestException.class);
    }
    @Test void rejectsNewSeriesAfterStartup()throws Exception {
        var provider=source("demo","fac_demo","7702000000000000000",0);provider.afterPropertiesSet();
        var jdbc=(JdbcTemplate)ReflectionTestUtils.getField(provider,"jdbc");when(jdbc.queryForObject(anyString(),eq(Long.class))).thenReturn(1L);
        var d=mock(DocumentoComercialDto.class);
        assertThatThrownBy(()->provider.commercialQr(new DocumentoComercialImpressaoDto(null,d,java.util.List.of()))).isInstanceOf(IllegalStateException.class);
    }
    @Test void defaultIsNoDemoBean() {
        new ApplicationContextRunner().withUserConfiguration(DemoSignatureSource.class)
                .run(context->assertThat(context).doesNotHaveBean(DemoSignatureSource.class));
    }
    @Test void pinAndSmokeLibraryContract()throws Exception { QrJarSmoke.main(new String[0]); }
}
