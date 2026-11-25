package com.ar2lda.fac.demo;

import com.ar2lda.fac.fiscal.saft.SaftFtExport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

/** Verification of evidence collected by this synthetic demo at the act of emission. */
final class DemoSaftEvidence implements SaftFtExport.Evidence {
    private final JdbcTemplate db;
    private final ObjectMapper mapper;
    private final String trustedFingerprint;
    private final String trustedManifest;
    private final String expectedVersion;

    DemoSaftEvidence(JdbcTemplate db, ObjectMapper mapper, Environment environment) {
        this.db = db;
        this.mapper = mapper;
        this.trustedFingerprint = environment.getRequiredProperty("fac.demo.saft.public-key-sha256");
        this.trustedManifest = environment.getRequiredProperty("fac.demo.saft.manifest-sha256");
        this.expectedVersion = DemoSignatureSource.versionFor(environment);
    }

    private Map<String,Object> evidence(long id) {
        var records = db.queryForList("select * from demo_saft_signature_evidence where documento_id=?", id);
        if (records.size() != 1) throw new SaftFtExport.Refusal("Factura " + id + ": falta a prova original da assinatura demo");
        return records.get(0);
    }

    private JsonNode verifiedEnvelope(long id) throws Exception {
        var item=evidence(id);
        if (!"1".equals(item.get("key_version")) || item.get("envelope")==null || item.get("envelope_signature")==null)
            throw new SaftFtExport.Refusal("Prova adicional de emissão demo em falta: " + id);
        byte[] keyBytes=(byte[])item.get("public_key");
        if (!trustedFingerprint.equals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(keyBytes))))
            throw new SaftFtExport.Refusal("Chave histórica demo não corresponde à identidade autorizada: " + id);
        var key=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        var verifier=java.security.Signature.getInstance("SHA256withRSA");
        verifier.initVerify(key); verifier.update((byte[])item.get("envelope"));
        if (!verifier.verify((byte[])item.get("envelope_signature"))) throw new SaftFtExport.Refusal("Prova adicional demo inválida: " + id);
        var envelope=mapper.readTree((byte[])item.get("envelope"));
        if (envelope.path("documentId").asLong()!=id || !"SYNTHETIC_ONLY".equals(envelope.path("scope").asText())
                || !"1".equals(envelope.path("keyVersion").asText())
                || !trustedManifest.equals(envelope.path("manifestSha256").asText())
                || !String.valueOf(item.get("numero_documento")).equals(envelope.path("documentNumber").asText())
                || !Base64.getEncoder().encodeToString((byte[])item.get("signature")).equals(envelope.path("documentSignature").asText()))
            throw new SaftFtExport.Refusal("Vínculo da prova demo divergente: " + id);
        return envelope;
    }

    @Override public SaftFtExport.Signature signature(long id, String number) {
        var item = evidence(id);
        if (!number.equals(item.get("numero_documento"))) throw new SaftFtExport.Refusal(number + ": identidade de assinatura divergente");
        return new SaftFtExport.Signature(Base64.getEncoder().encodeToString((byte[]) item.get("signature")), item.get("key_version").toString());
    }

    @Override public boolean matchesHistoricalKeyVersion(long id, String number, String candidate) {
        try {
            var item = evidence(id);
            verifiedEnvelope(id);
            return "1".equals(candidate) && candidate.equals(item.get("key_version"))
                    && number.equals(item.get("numero_documento"))
                    && trustedFingerprint.equals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((byte[]) item.get("public_key"))));
        } catch (Exception ex) { return false; }
    }

    @Override public boolean verifiesHash(long id, String number, SaftFtExport.Signature candidate) {
        try {
            var item = evidence(id);
            var envelope = verifiedEnvelope(id);
            byte[] signed = (byte[]) item.get("signed_bytes");
            var key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec((byte[]) item.get("public_key")));
            var verifier = java.security.Signature.getInstance("SHA256withRSA");
            verifier.initVerify(key);
            verifier.update(signed);
            if (!verifier.verify(Base64.getDecoder().decode(candidate.hash()))) return false;
            var archived = mapper.readTree(signed);
            var doc = archived.path("documento");
            var persisted = db.queryForMap("select * from documento_comercial where id=?", id);
            String qr = (String) persisted.get("qr_payload");
            if (qr == null || !qr.startsWith(expectedVersion + "\n")) return false;
            var qrJson = mapper.readTree(qr.substring(qr.indexOf('\n') + 1));
            if (!candidate.hash().equals(qrJson.path("signature").asText()) || qrJson.path("documentId").asLong() != id
                    || !number.equals(doc.path("numeroDocumentoCompleto").asText()) || doc.path("id").asLong()!=id) return false;
            String[][] documentFields = {{"tipoDocumentoId","id_tipo_documento"},{"serie","serie"},{"moedaId","id_moeda"},
                    {"atcud","atcud"},{"dataEmissao","data_emissao"},{"clienteId","id_cliente"},
                    {"clienteNome","cliente_nome"},{"clienteNif","cliente_nif"},{"clienteMorada","cliente_morada"},
                    {"clienteMorada1","cliente_morada1"},{"clienteLocalidade","cliente_localidade"},
                    {"clienteCodPostal","cliente_cod_postal"},{"clientePais","cliente_pais"},
                    {"valorBruto","valor_bruto"},{"valorDesconto","valor_desconto"},{"valorIvaTotal","valor_iva_total"},
                    {"valorTotal","valor_total"},{"dataPrestacao","data_prestacao"}};
            for (var pair : documentFields) if (!same(doc.path(pair[0]), persisted.get(pair[1]))) return false;
            var rows = db.queryForList("select * from linha_documento_comercial where id_documento_comercial=? order by numero_linha,id", id);
            if (rows.size()!=archived.path("linhas").size()) return false;
            String[][] lineFields={{"id","id"},{"numeroLinha","numero_linha"},{"tipoLinha","tipo_linha"},
                    {"artigoId","artigo_codigo"},{"descricao","descricao"},{"quantidade","quantidade"},
                    {"precoUnitario","preco_unitario"},{"valorBruto","valor_bruto"},{"valorDesconto","valor_desconto"},
                    {"unidade","unidade"},{"tipoTaxaIvaId","tipo_taxa_iva_codigo"},{"percentagemIva","percentagem_iva"},{"baseTributavel","base_tributavel"},
                    {"valorImposto","valor_imposto"},{"totalLinha","total_linha"}};
            for (int i=0;i<rows.size();i++) for (var pair:lineFields)
                if (!same(archived.path("linhas").get(i).path(pair[0]),rows.get(i).get(pair[1]))) return false;
            long commercial=rows.stream().filter(row -> "COMERCIAL".equals(row.get("tipo_linha"))).count();
            if (envelope.path("lineFacts").size()!=commercial) return false;
            var ids=new java.util.HashSet<Long>();
            for (JsonNode fact:envelope.path("lineFacts")) {
                long lineId=fact.path("id").asLong();
                if (!ids.add(lineId) || rows.stream().noneMatch(row -> ((Number)row.get("id")).longValue()==lineId
                        && "COMERCIAL".equals(row.get("tipo_linha")) && fact.path("codigo").asText().equals(row.get("artigo_codigo")))) return false;
            }
            return true;
        } catch (Exception ex) { return false; }
    }

    private static boolean same(JsonNode node,Object actual) {
        if (node.isMissingNode() || node.isNull()) return actual==null;
        if (actual==null) return false;
        if (node.isNumber() && actual instanceof Number) return node.decimalValue().compareTo(new BigDecimal(actual.toString()))==0;
        return node.asText().equals(actual.toString());
    }

    @Override public SaftFtExport.LineFacts line(long invoice, long line) {
        try {
            var facts=verifiedEnvelope(invoice).path("lineFacts");
            var rows=db.queryForList("select artigo_codigo,tipo_taxa_iva_codigo from linha_documento_comercial where id=? and id_documento_comercial=?",line,invoice);
            if (rows.size()!=1) throw new SaftFtExport.Refusal("Linha em falta: " + line);
            for (var fact:facts) if (fact.path("id").asLong()==line) {
                var row=rows.get(0);
                if (!fact.path("codigo").asText().equals(row.get("artigo_codigo")) || !fact.path("codigo_identificacao").isNull())
                    throw new SaftFtExport.Refusal("Identificação histórica divergente: " + line);
                String type=switch(fact.path("tipo_artigo").asText()) {case "ARTIGO" -> "P"; case "SERVICO" -> "S"; default -> throw new SaftFtExport.Refusal("Tipo de artigo desconhecido: " + line);};
                String tax=switch(String.valueOf(row.get("tipo_taxa_iva_codigo"))) {case "NORMAL" -> "NOR"; case "INTERMEDIA" -> "INT"; case "REDUZIDA" -> "RED"; default -> throw new SaftFtExport.Refusal("IVA fora da cobertura: " + line);};
                return new SaftFtExport.LineFacts(tax,type,fact.path("codigo").asText(),BigDecimal.ZERO);
            }
            throw new SaftFtExport.Refusal("Classificação histórica em falta: " + line);
        } catch (SaftFtExport.Refusal ex) { throw ex; }
        catch (Exception ex) { throw new SaftFtExport.Refusal("Prova de artigo inválida: " + line); }
    }

    @Override public boolean isOwnNormalVatInvoice(long invoice) {
        try {
            var envelope=verifiedEnvelope(invoice);
            return "OWN_BILLING".equals(envelope.path("provenance").asText())
                    && "NORMAL_NO_CASH_VAT".equals(envelope.path("vatRegime").asText());
        } catch (SaftFtExport.Refusal ex) { throw ex; }
        catch (Exception ex) { throw new SaftFtExport.Refusal("Prova de proveniência ou assinatura demo inválida: " + invoice); }
    }
}
