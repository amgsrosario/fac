package com.ar2lda.fac.fiscal.catalog;

import com.ar2lda.fac.fiscal.saft.SaftFtExport;
import com.ar2lda.fac.fiscal.saft.SaftFtExport.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.LongPredicate;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("saft-ft-eur-lab")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProjectedFrSaftFtExportTests extends ProjectedFrFixture {
    @Autowired SaftFtExport exporter;
    private static final LocalDate DAY = LocalDate.of(2026,11,3);
    private static final Installation INSTALLATION = new Installation(DAY.withDayOfYear(1),DAY,DAY,
            ZoneId.of("Europe/Lisbon"),"Global","CRC SYNTH","509999999","TUULI/Synthetic","0.0.1",1234,
            Map.of("GFT","FT"),false);

    private record SignedFixture(String number, Signature signature) {}
    private final Map<Long,SignedFixture> signed = new ConcurrentHashMap<>();
    private final java.security.KeyPair key = syntheticKey();

    private static java.security.KeyPair syntheticKey() {
        try {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            return generator.generateKeyPair();
        } catch (java.security.GeneralSecurityException e) { throw new IllegalStateException(e); }
    }

    // Capture a separate synthetic archival proof at emission, never regenerate it during export.
    private void captureSignature(long id) throws Exception {
        var number=correctionIdentity(id).get("numero_documento_completo").toString();
        var signing=java.security.Signature.getInstance("SHA256withRSA");
        signing.initSign(key.getPrivate());
        signing.update(number.getBytes(StandardCharsets.UTF_8));
        signed.put(id,new SignedFixture(number,new Signature(Base64.getEncoder().encodeToString(signing.sign()),"1")));
    }

    private long issueFt(boolean voided) throws Exception {
        db.update("update cliente set localidade='Lisboa' where id=?",customer);
        long id = create("SYNTH",admin);
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType("application/json")
                .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        captureSignature(id);
        if (voided) http.perform(auth(post(doc(id)+"/anular"),admin).contentType("application/json")
                .content("{\"motivo\":\"Anulacao sintetica\"}")).andExpect(status().isOk());
        return id;
    }

    private Evidence evidence(LongPredicate check, boolean date, boolean hash) throws Exception {
        return new Evidence() {
            public Signature signature(long id,String number) {
                if (!hash) return null;
                var stored=signed.get(id);
                if (stored==null || !stored.number().equals(number)) return null;
                try {
                    var sig=java.security.Signature.getInstance("SHA256withRSA");
                    sig.initVerify(key.getPublic());
                    sig.update(number.getBytes(StandardCharsets.UTF_8));
                    assertThat(sig.verify(Base64.getDecoder().decode(stored.signature().hash()))).isTrue();
                    return stored.signature();
                } catch (java.security.GeneralSecurityException e) { throw new IllegalStateException(e); }
            }
            public LineFacts line(long id,long line) { return new LineFacts(date?DAY.minusDays(1):null,"NOR","S","M3ART"); }
            public boolean isOwnNormalVatInvoice(long id) { return check.test(id); }
        };
    }

    private void validate(byte[] xml) {
        try {
            var p = new ProcessBuilder("python3","src/test/resources/saft/validate.py",
                    java.util.Objects.requireNonNull(System.getenv("SAFT_XSD_PATH"),"SAFT_XSD_PATH")).redirectErrorStream(true).start();
            try (var stdin=p.getOutputStream()) { stdin.write(xml); }
            String output=new String(p.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
            assertThat(p.waitFor()).as(output).isZero();
            assertThat(output).contains("PASS: XSD 1.1");
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @Test @Order(1) void validatesXmlReconcilesReferencesAndTotalsAndPreservesAnnulled() throws Exception {
        long normal=issueFt(false);
        long annulled=issueFt(true);
        var before=state(normal);
        long audits=count("auditoria_evento");
        var result=exporter.export(INSTALLATION,evidence(id -> {
            assertThat(db.queryForObject("show transaction_isolation",String.class)).isEqualTo("repeatable read");
            assertThat(db.queryForObject("show transaction_read_only",String.class)).isEqualTo("on");
            return true;
        },true,true),this::validate);
        noSideEffects(normal,before);
        assertThat(count("auditoria_evento")).isEqualTo(audits);
        assertThat(result.count()).isEqualTo(2);
        assertThat(result.totalCredit()).isEqualByComparingTo("10.00");
        String xml=new String(result.xml(),StandardCharsets.UTF_8);
        assertThat(xml).contains("<NumberOfEntries>2</NumberOfEntries>",
                "<InvoiceStatus>A</InvoiceStatus>","<InvoiceStatus>N</InvoiceStatus>",
                "<TaxPointDate>2026-11-02</TaxPointDate>","<ProductCode>M3ART</ProductCode>",
                "<CustomerID>"+customer+"</CustomerID>","<HashControl>1</HashControl>")
                .doesNotContain("<Payments>");
        assertThat(xml).contains(signed.get(normal).signature().hash(),signed.get(annulled).signature().hash());
        verifyContent(result.xml(),2,"10",customer);
        for (long id: new long[]{normal,annulled}) {
            var fiscal=correctionIdentity(id);
            assertThat(xml).contains(fiscal.get("numero_documento_completo").toString(),fiscal.get("atcud").toString());
        }
        Files.write(Path.of(System.getenv("M402_EVIDENCE"),"SAF-T-SYNTHETIC.xml"),result.xml());
        assertThatThrownBy(() -> validate(xml.replace("<InvoiceType>FT</InvoiceType>","<InvoiceType>ZZ</InvoiceType>")
                .getBytes(StandardCharsets.UTF_8))).isInstanceOf(AssertionError.class);
        // Schema accepts a wrong aggregate. Content reconciliation is an independent obligation.
        var invalidContent=xml.replaceFirst("<TotalCredit>[^<]+</TotalCredit>","<TotalCredit>999</TotalCredit>")
                .getBytes(StandardCharsets.UTF_8);
        validate(invalidContent);
        assertThatThrownBy(() -> verifyContent(invalidContent,2,"10",customer)).isInstanceOf(AssertionError.class);
    }

    private static void verifyContent(byte[] xml,int invoices,String credit,long customer) throws Exception {
        var parser=javax.xml.parsers.DocumentBuilderFactory.newInstance();
        parser.setNamespaceAware(true);
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        var document=parser.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml));
        assertThat(document.getElementsByTagNameNS(NAMESPACE,"Invoice").getLength()).isEqualTo(invoices);
        assertThat(document.getElementsByTagNameNS(NAMESPACE,"Customer").getLength()).isOne();
        assertThat(document.getElementsByTagNameNS(NAMESPACE,"Product").getLength()).isOne();
        assertThat(document.getElementsByTagNameNS(NAMESPACE,"TaxTableEntry").getLength()).isOne();
        for (String field: new String[]{"CustomerID","ProductCode","TaxCode"}) {
            var nodes=document.getElementsByTagNameNS(NAMESPACE,field);
            String expected=switch(field) { case "CustomerID" -> Long.toString(customer); case "ProductCode" -> "M3ART"; default -> "NOR"; };
            for (int n=0;n<nodes.getLength();n++) assertThat(nodes.item(n).getTextContent()).isEqualTo(expected);
        }
        assertThat(new java.math.BigDecimal(document.getElementsByTagNameNS(NAMESPACE,"TotalCredit").item(0).getTextContent()))
                .isEqualByComparingTo(credit);
        for (var entry:Map.of("NetTotal","10","GrossTotal","12.30","TaxPayable","2.30","UnitPrice","10").entrySet()) {
            var nodes=document.getElementsByTagNameNS(NAMESPACE,entry.getKey());
            assertThat(nodes.getLength()).isEqualTo(invoices);
            for (int n=0;n<nodes.getLength();n++)
                assertThat(new java.math.BigDecimal(nodes.item(n).getTextContent())).isEqualByComparingTo(entry.getValue());
        }
    }

    @Test @Order(2) void usesOneSnapshotAcrossConcurrentCommittedEmission() throws Exception {
        long next = create("SYNTH",admin);
        var inventoryDone = new CountDownLatch(1);
        var committed = new CountDownLatch(1);
        try (var pool=Executors.newSingleThreadExecutor()) {
            var writer=pool.submit(() -> {
                assertThat(inventoryDone.await(15,TimeUnit.SECONDS)).isTrue();
                try {
                    http.perform(auth(post(doc(next)+"/emitir"),admin).contentType("application/json")
                            .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
                    captureSignature(next);
                } finally { committed.countDown(); }
                return null;
            });
            var first=exporter.export(INSTALLATION,evidence(id -> {
                inventoryDone.countDown();
                try {
                    assertThat(committed.await(15,TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
                return true;
            },true,true),this::validate);
            writer.get(15,TimeUnit.SECONDS);
            var second=exporter.export(INSTALLATION,evidence(id -> true,true,true),this::validate);
            assertThat(first.count()).isEqualTo(2);
            assertThat(second.count()).isEqualTo(3);
            assertThat(first.totalCredit()).isEqualByComparingTo("10");
            assertThat(second.totalCredit()).isEqualByComparingTo("20");
            String number=correctionIdentity(next).get("numero_documento_completo").toString();
            assertThat(new String(first.xml(),StandardCharsets.UTF_8)).doesNotContain(number);
            assertThat(new String(second.xml(),StandardCharsets.UTF_8)).contains(number);
            record("saft-snapshot",Map.of("first",first.count(),"nextTransaction",second.count(),"concurrentDocument",number,
                    "firstTotalCredit",first.totalCredit(),"nextTotalCredit",second.totalCredit()));
        }
    }

    @Test @Order(3) void refusesMissingEvidenceAndUnsupportedDocumentsBeforeFiltering() throws Exception {
        var withoutDate=evidence(id -> true,false,true);
        var withoutHash=evidence(id -> true,true,false);
        assertThatThrownBy(() -> exporter.export(INSTALLATION,withoutDate,this::validate)).hasMessageContaining("TaxPointDate");
        assertThatThrownBy(() -> exporter.export(INSTALLATION,withoutHash,this::validate)).hasMessageContaining("Hash/HashControl");
        var i=INSTALLATION;
        var supplier=new Installation(i.start(),i.end(),i.generated(),i.zone(),i.taxEntity(),i.companyId(),
                i.producerTaxId(),i.productId(),i.version(),i.certificate(),i.enabledTypes(),true);
        assertThatThrownBy(() -> exporter.export(supplier,withoutDate,this::validate)).hasMessageContaining("Supplier");
        long fr=immediateDraft("SYNTH");
        issuer.issueProjected(key(),request(fr));
        assertThatThrownBy(() -> exporter.export(INSTALLATION,withoutDate,this::validate))
                .hasMessageContaining("Document without coverage").hasMessageContaining("id="+fr).hasMessageContaining("type=FR");
    }
}
