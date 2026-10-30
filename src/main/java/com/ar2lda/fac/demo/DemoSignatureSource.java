package com.ar2lda.fac.demo;

import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import com.ar2lda.fac.service.FiscalDocumentSignatureSource;
import com.ar2lda.fac.service.FiscalQrService.QrFiscal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

/** Explicitly non-fiscal demo provider. Never a fallback for the production signer. */
@Component
@ConditionalOnProperty(name="fac.demo.signature.enabled", havingValue="true")
public final class DemoSignatureSource implements FiscalDocumentSignatureSource, InitializingBean {
    public static final String VERSION="TUULI-DEMO-1";
    private final Environment environment;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private PrivateKey key;

    public DemoSignatureSource(Environment environment, DataSource dataSource, ObjectMapper mapper) {
        this.environment=environment; this.jdbc=new JdbcTemplate(dataSource); this.mapper=mapper;
    }

    @Override public void afterPropertiesSet() throws Exception {
        validateEnvironment();
        validateDatabaseAndSeries();
        validateExistingDocuments(jdbc);
        Path file=Path.of(environment.getRequiredProperty("fac.demo.signature.key-file"));
        if (!file.isAbsolute() || Files.isSymbolicLink(file) || !Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS))
            throw rejected();
        if (file.toRealPath().startsWith(Path.of(System.getProperty("user.dir")).toRealPath())) throw rejected();
        var permissions=Files.getPosixFilePermissions(file);
        if (permissions.stream().anyMatch(p -> p!=PosixFilePermission.OWNER_READ && p!=PosixFilePermission.OWNER_WRITE))
            throw rejected();
        key=KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(file)));
        if (!(key instanceof java.security.interfaces.RSAPrivateKey rsa) || rsa.getModulus().bitLength()<2048)
            throw rejected();
    }

    private void validateEnvironment() { validateProfile(environment); }

    private static void validateProfile(Environment environment) {
        var profiles=Arrays.asList(environment.getActiveProfiles());
        if (!profiles.equals(List.of("demo")) || !environment.getProperty("fac.demo.enabled",Boolean.class,false)
                || environment.containsProperty("spring.flyway.url"))
            throw rejected();
    }

    private void validateDatabaseAndSeries() {
        validateIdentity(environment,jdbc); validateSeries(jdbc);
    }

    static void validateIdentity(Environment environment,JdbcTemplate jdbc) {
        validateProfile(environment);
        String database=jdbc.queryForObject("select current_database()",String.class);
        String cluster=jdbc.queryForObject("select system_identifier::text from pg_control_system()",String.class);
        String authorized=environment.getProperty("fac.demo.signature.authorized-cluster","");
        if (!"fac_demo".equals(database) || !authorized.matches("[0-9]{19}") || !authorized.equals(cluster)
                || Set.of("7689035507522658342","7701042457621884972").contains(cluster)) throw rejected();
    }

    static void validateSeries(JdbcTemplate jdbc) {
        // Deliberately invalid AT codes (hyphen), restricted to this local demo seed.
        Long incompatible=jdbc.queryForObject("""
                select count(*) from serie where not (
                  serie='DEMO26' and ((id_tipo_documento='FT' and codigo_at='DEMO-FT')
                                  or (id_tipo_documento='RC' and codigo_at='DEMO-RC')))
                   or codigo_at is null
                """,Long.class);
        if (incompatible==null || incompatible!=0) throw rejected();
    }

    static void validateExistingDocuments(JdbcTemplate jdbc) {
        Long foreignDocuments=jdbc.queryForObject("""
                select (select count(*) from documento_comercial where numero_documento > 0
                         and (qr_payload_version is distinct from 'TUULI-DEMO-1'
                              or qr_payload is null or qr_payload not like 'TUULI-DEMO-1' || chr(10) || '%'))
                     + (select count(*) from documento_financeiro where
                            qr_payload_version is distinct from 'TUULI-DEMO-1'
                            or qr_payload is null or qr_payload not like 'TUULI-DEMO-1' || chr(10) || '%')
                """,Long.class);
        if (foreignDocuments==null || foreignDocuments!=0) throw rejected();
    }

    private QrFiscal payload(String type,String series,Long id,String number,String atcud,Object snapshot) {
        // Repeat the checks on use: post-start configuration changes cannot silently enable real series.
        validateEnvironment(); validateDatabaseAndSeries();
        if (key==null || id==null || !"DEMO26".equals(series) || !Set.of("FT","RC").contains(type)
                || !("DEMO-"+type+"-").concat(String.valueOf(number)).equals(atcud)) throw rejected();
        try {
            var signer=java.security.Signature.getInstance("SHA256withRSA");
            signer.initSign(key); signer.update(mapper.writeValueAsBytes(snapshot));
            String signature=Base64.getEncoder().encodeToString(signer.sign());
            String q=""+signature.charAt(0)+signature.charAt(10)+signature.charAt(20)+signature.charAt(30);
            Map<String,Object> fields=new LinkedHashMap<>();
            fields.put("origin",VERSION); fields.put("notice","DEMONSTRAÇÃO — SEM VALIDADE FISCAL");
            fields.put("documentId",id); fields.put("type",type); fields.put("series",series);
            fields.put("number",number); fields.put("demoIdentifier",atcud);
            fields.put("Q","DEMO:"+q); fields.put("R","DEMO-SEM-CERTIFICADO"); fields.put("signature",signature);
            return new QrFiscal(VERSION+"\n"+mapper.writeValueAsString(fields),VERSION);
        } catch (GeneralSecurityException | com.fasterxml.jackson.core.JsonProcessingException error) {
            throw new IllegalStateException("Não foi possível produzir assinatura exclusivamente demonstrativa",error);
        }
    }

    public QrFiscal commercialQr(DocumentoComercialImpressaoDto d) {
        var doc=d.documento();return payload(doc.tipoDocumentoId(),doc.serie(),doc.id(),
                String.valueOf(doc.numeroDocumento()),doc.atcud(),d);
    }
    public QrFiscal financialQr(DocumentoFinanceiroImpressaoDto d) {
        var doc=d.documento();return payload(doc.tipoDocumentoId(),doc.serie(),doc.id(),
                String.valueOf(doc.numeroDocumento()),doc.atcud(),d);
    }
    // The demo format intentionally cannot masquerade as a fiscal Signature/certificate.
    @Override public Optional<Signature> commercial(DocumentoComercialImpressaoDto d) { return Optional.empty(); }
    public static boolean isDemoPayload(String payload) { return payload!=null && payload.startsWith(VERSION+"\n"); }
    private static IllegalStateException rejected() {
        return new IllegalStateException("Fornecedor demo recusado: exige perfil demo exclusivo, cluster/base autorizados e séries exclusivamente demonstrativas");
    }
}
