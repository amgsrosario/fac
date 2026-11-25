package com.ar2lda.fac.demo;

import com.ar2lda.fac.fiscal.catalog.FiscalCatalog;
import com.ar2lda.fac.fiscal.saft.SaftFtExport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Local synthetic demonstration only; no productive SAF-T capability is enabled. */
@RestController
@Profile("demo")
@ConditionalOnProperty(name="fac.demo.saft.enabled",havingValue="true")
@RequestMapping("/saft-demo")
@PreAuthorize("hasAuthority('CONFIGURACAO_GERIR')")
public class DemoSaftController {
    private final SaftFtExport exporter;
    private final JdbcTemplate db;
    private final ObjectMapper mapper;
    private final Environment env;
    private final FiscalCatalog catalog;

    public DemoSaftController(SaftFtExport exporter, JdbcTemplate db, ObjectMapper mapper, Environment env, FiscalCatalog catalog) {
        this.exporter=exporter; this.db=db; this.mapper=mapper; this.env=env; this.catalog=catalog;
    }

    public record ExportResponse(String aviso, String inicio, String fim, int documentos,
                                 java.math.BigDecimal totalCredit, List<SaftFtExport.Reconciliation> reconciliacao,
                                 String xmlBase64) {}

    @GetMapping("/exportar")
    public ExportResponse export(@RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        DemoSignatureSource.validateIdentity(env,db);
        DemoSignatureSource.validateSeries(db);
        if (!env.getProperty("fac.demo.saft.capture-evidence",Boolean.class,false)
                || !env.getProperty("fac.demo.saft.synthetic-catalog",Boolean.class,false)
                || !env.getProperty("fac.demo.saft.ft-only",Boolean.class,false))
            throw new SaftFtExport.Refusal("Configuração FT/EUR exclusiva e captura de evidência demo não comprovadas");
        try {
            byte[] bytes=Files.readAllBytes(Path.of(env.getRequiredProperty("fac.demo.saft.manifest-file")));
            if (!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).equals(env.getRequiredProperty("fac.demo.saft.manifest-sha256")))
                throw new SaftFtExport.Refusal("Manifesto sintético da instalação não corresponde à referência");
            var manifest=mapper.readTree(bytes);
            if (!"SYNTHETIC_ONLY".equals(manifest.path("scope").asText())
                    || !"OWN_BILLING".equals(manifest.path("provenance").asText())
                    || !"NORMAL_NO_CASH_VAT".equals(manifest.path("vatRegime").asText())
                    || !manifest.has("supplierRequired") || manifest.path("supplierRequired").asBoolean(true))
                throw new SaftFtExport.Refusal("Manifesto demo incompleto ou família Supplier sem cobertura");
            catalog.verifyCoherence();
            var types=new HashSet<>(db.queryForList("select id from tipodocumento",String.class));
            if (!types.remove("FT")) throw new SaftFtExport.Refusal("Tipo FT governado indisponível");
            var installation=new SaftFtExport.Installation(inicio,fim,LocalDate.now(ZoneId.of("Europe/Lisbon")),ZoneId.of("Europe/Lisbon"),
                    "Global",manifest.path("companyId").asText(),manifest.path("producerTaxId").asText(),manifest.path("productId").asText(),
                    manifest.path("version").asText(),manifest.path("syntheticCertificate").asInt(),Map.of("FT","FT"),types,false);
            var result=exporter.export(installation,new DemoSaftEvidence(db,mapper,env),this::validateXsd);
            return new ExportResponse("DEMONSTRAÇÃO — XML SINTÉTICO SEM VALIDADE FISCAL",inicio.toString(),fim.toString(),result.count(),
                    result.totalCredit(),result.reconciliation(),Base64.getEncoder().encodeToString(result.xml()));
        } catch (SaftFtExport.Refusal ex) { throw ex; }
        catch (Exception ex) { throw new SaftFtExport.Refusal("Não foi possível comprovar a configuração ou validar o XML da demo"); }
    }

    private void validateXsd(byte[] xml) {
        Process process=null;
        try {
            var builder=new ProcessBuilder(env.getRequiredProperty("fac.demo.saft.validator-python"),
                    env.getRequiredProperty("fac.demo.saft.validator-script"),env.getRequiredProperty("fac.demo.saft.xsd-file"));
            builder.environment().put("PYTHONPATH",env.getRequiredProperty("fac.demo.saft.pythonpath"));
            process=builder.redirectErrorStream(true).start();
            try (var out=process.getOutputStream()) { out.write(xml); }
            if (!process.waitFor(30,TimeUnit.SECONDS)) throw new SaftFtExport.Refusal("Validação XSD excedeu o tempo limite");
            String output=new String(process.getInputStream().readNBytes(16384),java.nio.charset.StandardCharsets.UTF_8);
            if (process.exitValue()!=0 || !output.contains("PASS: XSD 1.1")) throw new SaftFtExport.Refusal("XML recusado pelo validador XSD 1.1 da demo");
        } catch (SaftFtExport.Refusal ex) { throw ex; }
        catch (Exception ex) { throw new SaftFtExport.Refusal("Validador XSD 1.1 indisponível"); }
        finally { if (process!=null && process.isAlive()) process.destroyForcibly(); }
    }

    @ExceptionHandler(SaftFtExport.Refusal.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String,String> refusal(SaftFtExport.Refusal ex) { return Map.of("message",ex.getMessage()); }
}
