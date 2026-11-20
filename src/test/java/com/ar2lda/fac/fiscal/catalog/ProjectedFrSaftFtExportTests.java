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

import static com.ar2lda.fac.fiscal.saft.SaftFtExport.NAMESPACE;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("saft-ft-eur-lab")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProjectedFrSaftFtExportTests extends ProjectedFrFixture {
    @Autowired SaftFtExport exporter;
    private static final LocalDate DAY = LocalDate.of(2026,11,3);
    private static final String EMISSION = "2026-11-03T09:12:00";
    private static final String ANNULMENT = "2026-11-03T17:45:00";
    private static final String REASON = "Anulacao sintetica";
    private final Map<Long,java.math.BigDecimal> headerShares = new ConcurrentHashMap<>();
    private static final Installation INSTALLATION = new Installation(DAY.withDayOfYear(1),DAY,DAY,
            ZoneId.of("Europe/Lisbon"),"Global","CRC 123","509999999","TUULI/Synthetic","0.0.1",1234,
            Map.of("GFT","FT"),java.util.Set.of("FRE"),false);

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
        db.update("update documento_comercial set momento_emissao=?::timestamptz where id=?",EMISSION+"+00:00",id);
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
        if (voided) {
            db.update("""
                insert into utilizador(codigo,nome,email,password_hash,inativo,papel)
                select 'M3VOID','Synthetic reviewer','void@example.test',password_hash,false,papel
                from utilizador where codigo='M3ADMIN'
                """);
            http.perform(auth(post(doc(id)+"/anular"),login("M3VOID")).contentType("application/json")
                    .content("{\"motivo\":\""+REASON+"\"}")).andExpect(status().isOk());
            db.update("update documento_comercial set data_hora_anulacao=?::timestamptz where id=?",ANNULMENT+"+00:00",id);
        }
        return id;
    }

    private Evidence evidence(LongPredicate check, boolean date, boolean hash) throws Exception {
        return evidence(check,date,hash,java.util.function.UnaryOperator.identity());
    }

    private Evidence evidence(LongPredicate check,boolean date,boolean hash,java.util.function.UnaryOperator<Signature> candidate) {
        return evidence(check, date, hash, candidate, DAY.minusDays(1));
    }

    private Evidence evidence(LongPredicate check, boolean date, boolean hash,
                              java.util.function.UnaryOperator<Signature> candidate, LocalDate taxPointDate) {
        return new Evidence() {
            public Signature signature(long id,String number) {
                if (!hash) return null;
                var stored=signed.get(id);
                if (stored==null || !stored.number().equals(number)) return null;
                return candidate.apply(stored.signature());
            }
            public boolean matchesHistoricalKeyVersion(long id,String number,String version) {
                var stored=signed.get(id);
                return stored!=null && stored.number().equals(number) && stored.signature().keyVersion().equals(version);
            }
            public boolean verifiesHash(long id,String number,Signature value) {
                try {
                    var verifier=java.security.Signature.getInstance("SHA256withRSA");
                    verifier.initVerify(key.getPublic());
                    verifier.update(number.getBytes(StandardCharsets.UTF_8));
                    return verifier.verify(Base64.getDecoder().decode(value.hash()));
                } catch (java.security.GeneralSecurityException | IllegalArgumentException e) { return false; }
            }
            public LineFacts line(long id,long line) {
                return new LineFacts(date?taxPointDate:null,"NOR","S","M3ART",headerShares.get(line));
            }
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
            System.out.print(output);
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
        assertThat(xml).contains("<SystemEntryDate>"+EMISSION+"</SystemEntryDate>",
                "<InvoiceStatusDate>"+ANNULMENT+"</InvoiceStatusDate>");
        var statusStart=xml.indexOf("<InvoiceStatus>A</InvoiceStatus>");
        assertThat(xml.substring(statusStart,xml.indexOf("</DocumentStatus>",statusStart)))
            .contains("<SourceID>M3VOID</SourceID>","<InvoiceStatusDate>"+ANNULMENT+"</InvoiceStatusDate>",
                    "<Reason>"+REASON+"</Reason>");
        for (long id: new long[]{normal,annulled}) {
            var fiscal=correctionIdentity(id);
            assertThat(xml).contains(fiscal.get("numero_documento_completo").toString(),fiscal.get("atcud").toString());
        }
        Path evidenceDirectory=Files.createDirectories(Path.of(System.getenv("M402_EVIDENCE")));
        Files.write(evidenceDirectory.resolve("SAF-T-SYNTHETIC.xml"),result.xml());
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
        db.update("update cliente set localidade='Lisboa' where id=?",customer);
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

    @Test @Order(3) void discountedLineExportsNetUnitPriceAndKeepsCentTotals() throws Exception {
        db.update("update cliente set localidade='Lisboa' where id=?",customer);
        String discounted=line("M3ART",3,1).replace("\"desconto\":0","\"desconto\":2");
        long id=body(http.perform(auth(post("/documentos-comerciais"),admin).contentType("application/json")
                .content(draft("SYNTH","M3ART",1).replace(line("M3ART",1,10),discounted)))
                .andExpect(status().isCreated()).andReturn()).get("id").asLong();
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType("application/json")
                .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        captureSignature(id);
        var result=exporter.export(INSTALLATION,evidence(doc -> true,true,true),this::validate);
        assertThat(result.count()).isEqualTo(4);
        assertThat(result.totalCredit()).isEqualByComparingTo("21");
        var parser=javax.xml.parsers.DocumentBuilderFactory.newInstance();
        parser.setNamespaceAware(true);
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        var document=parser.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(result.xml()));
        var invoice=(org.w3c.dom.Element)document.getElementsByTagNameNS(NAMESPACE,"Invoice").item(3);
        for (var entry:Map.of("UnitPrice","0.333333333333","CreditAmount","1","SettlementAmount","2",
                "NetTotal","1","TaxPayable","0.23","GrossTotal","1.23").entrySet()) {
            assertThat(new java.math.BigDecimal(invoice.getElementsByTagNameNS(NAMESPACE,entry.getKey()).item(0).getTextContent()))
                    .isEqualByComparingTo(entry.getValue());
        }
        Files.write(Path.of(System.getenv("M402_EVIDENCE"),"SAF-T-discount-SYNTHETIC.xml"),result.xml());
    }

    @Test @Order(4) void refusesDivergentHashAndKeyBeforeValidatorAndRejectsFutureDates() throws Exception {
        Validator mustNotRun = xml -> fail("XML reached the validator");
        var badHash=evidence(id -> true,true,true,s -> new Signature(
                (s.hash().startsWith("A") ? "B" : "A")+s.hash().substring(1),s.keyVersion()));
        assertThatThrownBy(() -> exporter.export(INSTALLATION,badHash,mustNotRun)).hasMessageContaining("Hash does not verify");
        var badKey=evidence(id -> true,true,true,s -> new Signature(s.hash(),"2"));
        assertThatThrownBy(() -> exporter.export(INSTALLATION,badKey,mustNotRun)).hasMessageContaining("HashControl does not match");
        long normal=db.queryForObject("select min(id) from documento_comercial where estado='EMITIDO'",Long.class);
        long cancelled=db.queryForObject("select min(id) from documento_comercial where estado='ANULADO'",Long.class);
        var good=evidence(id -> true,true,true);
        db.update("update documento_comercial set momento_emissao='2026-11-04T00:00:00Z' where id=?",normal);
        try {
            assertThatThrownBy(() -> exporter.export(INSTALLATION,good,mustNotRun)).hasMessageContaining("SystemEntryDate after DateCreated");
        } finally { db.update("update documento_comercial set momento_emissao=?::timestamptz where id=?",EMISSION+"+00:00",normal); }
        db.update("update documento_comercial set data_hora_anulacao='2026-11-04T00:00:00Z' where id=?",cancelled);
        try {
            assertThatThrownBy(() -> exporter.export(INSTALLATION,good,mustNotRun)).hasMessageContaining("InvoiceStatusDate after DateCreated");
        } finally { db.update("update documento_comercial set data_hora_anulacao=?::timestamptz where id=?",ANNULMENT+"+00:00",cancelled); }
    }

    @Test @Order(5) void preservesDocumentOutsidePeriodWithoutIncludingIt() throws Exception {
        db.update("update cliente set localidade='Lisboa' where id=?",customer);
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('GFT','OUTSIDE','Synthetic',0,'OUTSID01','2026-01-01')");
        long id=body(http.perform(auth(post("/documentos-comerciais"),admin).contentType("application/json")
                .content(draft("OUTSIDE","M3ART",1).replace("2026-11-03","2026-11-04")))
                .andExpect(status().isCreated()).andReturn()).get("id").asLong();
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType("application/json")
                .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        var result=exporter.export(INSTALLATION,evidence(doc -> true,true,true),this::validate);
        assertThat(result.count()).isEqualTo(4);
        assertThat(new String(result.xml(),StandardCharsets.UTF_8))
                .doesNotContain(correctionIdentity(id).get("numero_documento_completo").toString());
    }

    @Test @Order(6) void reconcilesExplicitHeaderShareAndRefusesUnallocatedDiscount() throws Exception {
        // Fixture of a historical consolidation with a proven 2 EUR header allocation.
        db.update("update cliente set localidade='Lisboa' where id=?",customer);
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at) values('GFT','SHARE','Synthetic',0,'SHARE001','2026-01-01')");
        String discounted=line("M3ART",3,10).replace("\"desconto\":0","\"desconto\":3");
        long id=body(http.perform(auth(post("/documentos-comerciais"),admin).contentType("application/json")
                .content(draft("SYNTH","M3ART",1).replace("SYNTH","SHARE").replace(line("M3ART",1,10),discounted)))
                .andExpect(status().isCreated()).andReturn()).get("id").asLong();
        http.perform(auth(post(doc(id)+"/emitir"),admin).contentType("application/json")
                .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        captureSignature(id);
        long lineId=db.queryForObject("select id from linha_documento_comercial where id_documento_comercial=?",Long.class,id);
        db.update("update documento_comercial set valor_desconto=5 where id=?",id);
        assertThatThrownBy(() -> exporter.export(INSTALLATION,evidence(doc -> true,true,true),xml -> fail("Unexpected XML")))
                .hasMessageContaining("unapportioned discount");
        db.update("update linha_documento_comercial set base_tributavel=25,valor_imposto=5.75,total_linha=30.75 where id=?",lineId);
        db.update("update documento_comercial set valor_iva_total=5.75,valor_total=30.75 where id=?",id);
        headerShares.put(lineId,new java.math.BigDecimal("2.00"));
        var result=exporter.export(INSTALLATION,evidence(doc -> true,true,true),this::validate);
        assertThat(result.count()).isEqualTo(5);
        assertThat(result.totalCredit()).isEqualByComparingTo("46");
        assertThat(new String(result.xml(),StandardCharsets.UTF_8)).contains("<UnitPrice>8.333333333333</UnitPrice>",
                "<SettlementAmount>5.000000</SettlementAmount>");
        Files.write(Path.of(System.getenv("M402_EVIDENCE"),"SAF-T-header-share-SYNTHETIC.xml"),result.xml());
    }

    @Test @Order(7) void refusesMissingEvidenceAndUnsupportedDocumentsBeforeFiltering() throws Exception {
        var withoutDate=evidence(id -> true,false,true);
        var withoutHash=evidence(id -> true,true,false);
        assertThatThrownBy(() -> exporter.export(INSTALLATION,withoutDate,this::validate)).hasMessageContaining("TaxPointDate");
        assertThatThrownBy(() -> exporter.export(INSTALLATION,withoutHash,this::validate)).hasMessageContaining("Hash/HashControl");
        var i=INSTALLATION;
        var supplier=new Installation(i.start(),i.end(),i.generated(),i.zone(),i.taxEntity(),i.companyId(),
                i.producerTaxId(),i.productId(),i.version(),i.certificate(),i.enabledTypes(),i.disabledTypes(),true);
        assertThatThrownBy(() -> exporter.export(supplier,withoutDate,this::validate)).hasMessageContaining("Supplier");
        var uncovered=new Installation(i.start(),i.end(),i.generated(),i.zone(),i.taxEntity(),i.companyId(),
                i.producerTaxId(),i.productId(),i.version(),i.certificate(),Map.of("GFT","FT","FRE","FR"),java.util.Set.of(),false);
        assertThatThrownBy(() -> exporter.export(uncovered,withoutDate,this::validate)).hasMessageContaining("Enabled type without coverage: FRE/FR");
        assertThatThrownBy(() -> exporter.export(INSTALLATION,evidence(id -> {
            db.update("update empresa set nome=nome where id=1");
            return true;
        },true,true),this::validate)).hasStackTraceContaining("read-only transaction");
        long fr=immediateDraft("SYNTH");
        issuer.issueProjected(key(),request(fr));
        assertThatThrownBy(() -> exporter.export(INSTALLATION,withoutDate,this::validate))
                .hasMessageContaining("Document without coverage").hasMessageContaining("id="+fr).hasMessageContaining("type=FR");
    }

    @Test @Order(8) void acceptsWinterSystemEntryAtLastSecondOfGenerationDate() throws Exception {
        var generated = LocalDate.of(2026, 11, 3);
        var invoiceDate = generated.minusDays(1);
        var id = issueChronologyFixture(invoiceDate);
        var installation = chronologyInstallation(invoiceDate, generated);
        setEntryInstant(id, "2026-11-03T23:59:59Z");

        assertChronologyXml(installation, id, "SystemEntryDate", "2026-11-03T23:59:59", "winter-entry");
        record("saft-r2-runtime", Map.of("jvmZone", ZoneId.systemDefault().getId(),
                "postgresZone", db.queryForObject("show timezone", String.class)));
    }

    @Test @Order(9) void checksSummerSystemEntryAtLisbonMidnight() throws Exception {
        var day = LocalDate.of(2026, 7, 3);
        var id = issueChronologyFixture(day);
        var installation = chronologyInstallation(day, day);
        setEntryInstant(id, "2026-07-03T22:59:59Z");
        assertChronologyXml(installation, id, "SystemEntryDate", "2026-07-03T23:59:59", "summer-entry");

        setEntryInstant(id, "2026-07-03T23:00:00Z");
        try {
            assertThatThrownBy(() -> exporter.export(installation, chronologyEvidence(day),
                    xml -> fail("Rejected SystemEntryDate reached the validator")))
                    .hasMessageContaining("SystemEntryDate after DateCreated");
        } finally {
            setEntryInstant(id, "2026-07-03T22:59:59Z");
        }
    }

    @Test @Order(10) void checksSummerAnnulmentAtLisbonMidnight() throws Exception {
        var day = LocalDate.of(2026, 7, 3);
        var id = issueChronologyFixture(day);
        var installation = chronologyInstallation(day, day);
        http.perform(auth(post(doc(id) + "/anular"), login("M3VOID")).contentType("application/json")
                .content("{\"motivo\":\"" + REASON + "\"}")).andExpect(status().isOk());
        setEntryInstant(id, "2026-07-03T21:00:00Z");
        setAnnulmentInstant(id, "2026-07-03T22:59:59Z");
        assertChronologyXml(installation, id, "InvoiceStatusDate", "2026-07-03T23:59:59", "summer-status");

        setAnnulmentInstant(id, "2026-07-03T23:00:00Z");
        assertThatThrownBy(() -> exporter.export(installation, chronologyEvidence(day),
                xml -> fail("Rejected InvoiceStatusDate reached the validator")))
                .hasMessageContaining("InvoiceStatusDate after DateCreated");
    }

    private long issueChronologyFixture(LocalDate day) throws Exception {
        db.update("update cliente set localidade='Lisboa' where id=?", customer);
        var series = "R2-" + day.getMonthValue();
        db.update("""
                insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at)
                values ('GFT',?,'Synthetic R2',0,?,'2026-01-01') on conflict do nothing
                """, series, "R2SAFT" + day.getMonthValue());
        long id = body(http.perform(auth(post("/documentos-comerciais"), admin).contentType("application/json")
                .content(draft(series, "M3ART", 1).replace("2026-11-03", day.toString())))
                .andExpect(status().isCreated()).andReturn()).get("id").asLong();
        http.perform(auth(post(doc(id) + "/emitir"), admin).contentType("application/json")
                .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk());
        captureSignature(id);
        return id;
    }

    private static Installation chronologyInstallation(LocalDate day, LocalDate generated) {
        var i = INSTALLATION;
        return new Installation(day, day, generated, i.zone(), i.taxEntity(), i.companyId(),
                i.producerTaxId(), i.productId(), i.version(), i.certificate(),
                i.enabledTypes(), i.disabledTypes(), false);
    }

    private Evidence chronologyEvidence(LocalDate day) {
        return evidence(id -> true, true, true, java.util.function.UnaryOperator.identity(), day);
    }

    private void setEntryInstant(long id, String instant) {
        db.update("update documento_comercial set momento_emissao=? where id=?",
                java.time.OffsetDateTime.parse(instant), id);
    }

    private void setAnnulmentInstant(long id, String instant) {
        db.update("update documento_comercial set data_hora_anulacao=? where id=?",
                java.time.OffsetDateTime.parse(instant), id);
    }

    private void assertChronologyXml(Installation installation, long id, String field,
                                     String expected, String proof) throws Exception {
        var result = exporter.export(installation, chronologyEvidence(installation.end()), this::validate);
        var xml = new String(result.xml(), StandardCharsets.UTF_8);
        var number = correctionIdentity(id).get("numero_documento_completo").toString();
        assertThat(xml).contains("<DateCreated>" + installation.generated() + "</DateCreated>");
        var start = xml.indexOf("<InvoiceNo>" + number + "</InvoiceNo>");
        assertThat(start).as("Exported invoice %s", number).isNotNegative();
        var invoice = xml.substring(start, xml.indexOf("</Invoice>", start));
        if ("InvoiceStatusDate".equals(field)) {
            assertThat(invoice).contains("<InvoiceStatus>A</InvoiceStatus>");
        }
        assertThat(invoice).contains("<" + field + ">" + expected + "</" + field + ">");
        Files.write(Path.of(System.getenv("M402_EVIDENCE"), "SAFT-R2-" + proof + ".xml"), result.xml());
    }
}
