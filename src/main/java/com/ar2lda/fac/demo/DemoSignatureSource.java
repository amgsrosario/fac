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
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Explicitly non-fiscal demo provider. Never a fallback for the production signer. */
@Component
@ConditionalOnProperty(name="fac.demo.signature.enabled", havingValue="true")
@org.springframework.boot.autoconfigure.condition.ConditionalOnExpression("'${fac.demo.saft.short-signature.enabled:false}' != 'true'")
public class DemoSignatureSource implements FiscalDocumentSignatureSource, InitializingBean {
    public static final String VERSION="TUULI-DEMO-1";
    public static final String SAFT_TEST_VERSION="TUULI-DEMO-SAFT-1";
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
        validateExistingDocuments(jdbc,environment);
        Path file=Path.of(environment.getRequiredProperty("fac.demo.signature.key-file"));
        if (!file.isAbsolute() || Files.isSymbolicLink(file) || !Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS))
            throw rejected();
        if (file.toRealPath().startsWith(Path.of(System.getProperty("user.dir")).toRealPath())) throw rejected();
        var permissions=Files.getPosixFilePermissions(file);
        if (permissions.stream().anyMatch(p -> p!=PosixFilePermission.OWNER_READ && p!=PosixFilePermission.OWNER_WRITE))
            throw rejected();
        key=KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(file)));
        if (!(key instanceof java.security.interfaces.RSAPrivateKey rsa) || !acceptsKeyBits(rsa.getModulus().bitLength()))
            throw rejected();
    }

    protected boolean acceptsKeyBits(int bits) { return bits>=2048; }

    static String versionFor(Environment environment) {
        return environment.getProperty("fac.demo.saft.short-signature.enabled",Boolean.class,false) ? SAFT_TEST_VERSION : VERSION;
    }

    private void validateEnvironment() { validateProfile(environment); }

    private static void validateProfile(Environment environment) {
        var profiles=Arrays.asList(environment.getActiveProfiles());
        if (!profiles.equals(List.of("demo")) || !environment.getProperty("fac.demo.enabled",Boolean.class,false)
                || environment.containsProperty("spring.flyway.url"))
            throw rejected();
    }

    private void validateDatabaseAndSeries() {
        validateIdentity(environment,jdbc); validateSeries(jdbc,environment);
    }

    static void validateIdentity(Environment environment,JdbcTemplate jdbc) {
        validateProfile(environment);
        String database=jdbc.queryForObject("select current_database()",String.class);
        String cluster=jdbc.queryForObject("select system_identifier::text from pg_control_system()",String.class);
        String authorized=environment.getProperty("fac.demo.signature.authorized-cluster","");
        if (!"fac_demo".equals(database) || !authorized.matches("[0-9]{19}") || !authorized.equals(cluster)
                || Set.of("7689035507522658342","7701042457621884972").contains(cluster)) throw rejected();
        if (environment.getProperty("fac.demo.saft.short-signature.enabled",Boolean.class,false)
                && (!Set.of("7712163055710986279", "7712437121396502565", "7712504161523511334", "7712812072026869798", "7712817541562777638").contains(cluster)
                    || !cluster.equals(environment.getProperty("fac.demo.saft.short-signature.authorized-cluster",""))
                    || !environment.getProperty("fac.demo.saft.synthetic-catalog",Boolean.class,false))) throw rejected();
    }

    static void validateSeries(JdbcTemplate jdbc, Environment environment) {
        // Deliberately invalid AT codes (hyphen), restricted to this local demo seed.
        Long incompatible=jdbc.queryForObject("""
                select count(*) from serie where not (
                  (serie='DEMO26' and ((id_tipo_documento='FT' and codigo_at='DEMO-FT')
                                  or (id_tipo_documento='RC' and codigo_at='DEMO-RC')))
                  or (%s and serie='FR26' and id_tipo_documento='FRE' and codigo_at='DEMO-FRE'))
                   or codigo_at is null
                """.formatted(environment.getProperty("fac.demo.fr.enabled",Boolean.class,false)),Long.class);
        if (incompatible==null || incompatible!=0) throw rejected();
    }

    static void validateExistingDocuments(JdbcTemplate jdbc,Environment environment) {
        String version=versionFor(environment);
        Long foreignDocuments=jdbc.queryForObject("""
                select (select count(*) from documento_comercial where numero_documento > 0
                         and (qr_payload_version is distinct from '%s'
                              or qr_payload is null or qr_payload not like '%s' || chr(10) || '%%'))
                     + (select count(*) from documento_financeiro where
                            qr_payload_version is distinct from '%s'
                            or qr_payload is null or qr_payload not like '%s' || chr(10) || '%%')
                """.formatted(version,version,version,version),Long.class);
        if (foreignDocuments==null || foreignDocuments!=0) throw rejected();
    }

    private QrFiscal payload(String type,String series,Long id,String number,String atcud,Object snapshot) {
        // Repeat the checks on use: post-start configuration changes cannot silently enable real series.
        validateEnvironment(); validateDatabaseAndSeries();
        boolean supported=Set.of("FT","RC").contains(type)
                || environment.getProperty("fac.demo.fr.enabled",Boolean.class,false) && "FRE".equals(type);
        boolean approvedSeries = "FRE".equals(type) ? "FR26".equals(series) : "DEMO26".equals(series);
        if (key==null || id==null || !approvedSeries || !supported
                || !("DEMO-"+type+"-").concat(String.valueOf(number)).equals(atcud)) throw rejected();
        try {
            var signer=java.security.Signature.getInstance("SHA256withRSA");
            byte[] signedBytes = mapper.writeValueAsBytes(snapshot);
            signer.initSign(key); signer.update(signedBytes);
            byte[] signed = signer.sign();
            String signature=Base64.getEncoder().encodeToString(signed);
            if (snapshot instanceof DocumentoComercialImpressaoDto commercial
                    && environment.getProperty("fac.demo.saft.capture-evidence", Boolean.class, false)) {
                archiveCommercialEvidence(commercial, signedBytes, signed);
            }
            if (snapshot instanceof DocumentoFinanceiroImpressaoDto receipt
                    && environment.getProperty("fac.demo.saft.receipts-enabled", Boolean.class, false)) {
                archiveReceiptEvidence(receipt, signedBytes, signed);
            }
            String q=""+signature.charAt(0)+signature.charAt(10)+signature.charAt(20)+signature.charAt(30);
            Map<String,Object> fields=new LinkedHashMap<>();
            String version=versionFor(environment);
            fields.put("origin",version); fields.put("notice","DEMONSTRAÇÃO — SEM VALIDADE FISCAL");
            fields.put("documentId",id); fields.put("type",type); fields.put("series",series);
            fields.put("number",number); fields.put("demoIdentifier",atcud);
            fields.put("Q","DEMO:"+q); fields.put("R","DEMO-SEM-CERTIFICADO"); fields.put("signature",signature);
            return new QrFiscal(version+"\n"+mapper.writeValueAsString(fields),version);
        } catch (GeneralSecurityException | java.io.IOException error) {
            throw new IllegalStateException("Não foi possível produzir assinatura exclusivamente demonstrativa",error);
        }
    }

    private void archiveReceiptEvidence(DocumentoFinanceiroImpressaoDto receipt, byte[] bytes, byte[] signature)
            throws GeneralSecurityException {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !(key instanceof java.security.interfaces.RSAPrivateCrtKey rsa)) throw rejected();
        byte[] publicKey = KeyFactory.getInstance("RSA").generatePublic(
                new RSAPublicKeySpec(rsa.getModulus(), rsa.getPublicExponent())).getEncoded();
        if (!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(publicKey))
                .equals(environment.getProperty("fac.demo.saft.public-key-sha256", ""))) throw rejected();
        jdbc.update("insert into demo_saft_receipt_evidence (documento_id,public_key,signed_bytes,signature) values (?,?,?,?)",
                receipt.documento().id(), publicKey, bytes, signature);
    }

    private void archiveCommercialEvidence(DocumentoComercialImpressaoDto commercial, byte[] bytes, byte[] signature)
            throws GeneralSecurityException, java.io.IOException {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !(key instanceof java.security.interfaces.RSAPrivateCrtKey rsa)) throw rejected();
        byte[] publicKey = KeyFactory.getInstance("RSA").generatePublic(
                new RSAPublicKeySpec(rsa.getModulus(), rsa.getPublicExponent())).getEncoded();
        String fingerprint = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(publicKey));
        if (!fingerprint.equals(environment.getProperty("fac.demo.saft.public-key-sha256", ""))) throw rejected();
        Path manifestPath = Path.of(environment.getRequiredProperty("fac.demo.saft.manifest-file"));
        if (!manifestPath.isAbsolute() || Files.isSymbolicLink(manifestPath) || !Files.isRegularFile(manifestPath, LinkOption.NOFOLLOW_LINKS)) throw rejected();
        byte[] manifestBytes = Files.readAllBytes(manifestPath);
        String manifestDigest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(manifestBytes));
        if (!manifestDigest.equals(environment.getRequiredProperty("fac.demo.saft.manifest-sha256"))) throw rejected();
        var manifest = mapper.readTree(manifestBytes);
        if (!"SYNTHETIC_ONLY".equals(manifest.path("scope").asText())
                || !"OWN_BILLING".equals(manifest.path("provenance").asText())
                || !"NORMAL_NO_CASH_VAT".equals(manifest.path("vatRegime").asText())) throw rejected();
        var facts = jdbc.queryForList("""
                select l.id, a.codigo, a.tipo_artigo, a.codigo_identificacao
                from linha_documento_comercial l join artigo a on a.codigo=l.id_artigo
                where l.id_documento_comercial=? and l.tipo_linha='COMERCIAL' order by l.id
                for share of a
                """, commercial.documento().id());
        if (facts.size() != commercial.linhas().stream()
                .filter(line -> line.tipoLinha() == com.ar2lda.fac.model.TipoLinhaDocumento.COMERCIAL).count()) throw rejected();
        for (var fact : facts) {
            var declaration = manifest.path("articles").path(fact.get("codigo").toString());
            if (declaration.isMissingNode() || !declaration.path("eanAbsent").asBoolean(false)
                    || fact.get("codigo_identificacao") != null
                    || !declaration.path("type").asText().equals(fact.get("tipo_artigo"))) throw rejected();
        }
        Map<String,Object> envelope = new LinkedHashMap<>();
        envelope.put("scope", "SYNTHETIC_ONLY");
        envelope.put("documentId", commercial.documento().id());
        envelope.put("documentNumber", commercial.documento().numeroDocumentoCompleto());
        envelope.put("documentSignature", Base64.getEncoder().encodeToString(signature));
        envelope.put("keyVersion", "1");
        envelope.put("manifestSha256", manifestDigest);
        envelope.put("provenance", manifest.path("provenance").asText());
        envelope.put("vatRegime", manifest.path("vatRegime").asText());
        envelope.put("lineFacts", facts);
        byte[] envelopeBytes = mapper.writeValueAsBytes(envelope);
        var envelopeSigner = java.security.Signature.getInstance("SHA256withRSA");
        envelopeSigner.initSign(key);
        envelopeSigner.update(envelopeBytes);
        byte[] envelopeSignature = envelopeSigner.sign();
        jdbc.update("""
                insert into demo_saft_signature_evidence
                  (documento_id,numero_documento,key_version,public_key,signed_bytes,signature,line_facts,envelope,envelope_signature)
                values (?,?,?,?,?,?,?::jsonb,?,?)
                """, commercial.documento().id(), commercial.documento().numeroDocumentoCompleto(),
                "1", publicKey, bytes, signature, mapper.writeValueAsString(facts), envelopeBytes, envelopeSignature);
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
    public static boolean isDemoPayload(String payload) { return payload!=null && (payload.startsWith(VERSION+"\n") || payload.startsWith(SAFT_TEST_VERSION+"\n")); }
    private static IllegalStateException rejected() {
        return new IllegalStateException("Fornecedor demo recusado: exige perfil demo exclusivo, cluster/base autorizados e séries exclusivamente demonstrativas");
    }
}
