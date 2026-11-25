package com.ar2lda.fac.demo;

import com.ar2lda.fac.fiscal.saft.SaftFtExport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.env.MockEnvironment;

import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DemoSaftEvidenceTests {
    @Test void missingOriginalEvidenceReportsTheCause() {
        var db=mock(JdbcTemplate.class);
        when(db.queryForList(anyString(),eq(42L))).thenReturn(List.of());
        var env=new MockEnvironment().withProperty("fac.demo.saft.public-key-sha256","pin")
                .withProperty("fac.demo.saft.manifest-sha256","manifest");
        var evidence=new DemoSaftEvidence(db,new ObjectMapper(),env);
        assertThatThrownBy(()->evidence.isOwnNormalVatInvoice(42L))
                .isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("falta a prova original da assinatura demo");
    }

    @Test void signedEnvelopeIsBoundToTheTrustedKeyDocumentAndManifest() throws Exception {
        var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(1024);var pair=generator.generateKeyPair();
        byte[] originalSignature={1,2,3};
        var env=new MockEnvironment().withProperty("fac.demo.saft.public-key-sha256",
                HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pair.getPublic().getEncoded())))
                .withProperty("fac.demo.saft.manifest-sha256","manifest-test");
        var envelope=new LinkedHashMap<String,Object>();
        envelope.put("scope","SYNTHETIC_ONLY");envelope.put("documentId",42);envelope.put("documentNumber","FT DEMO26/42");
        envelope.put("documentSignature",Base64.getEncoder().encodeToString(originalSignature));envelope.put("keyVersion","1");
        envelope.put("manifestSha256","manifest-test");envelope.put("provenance","OWN_BILLING");envelope.put("vatRegime","NORMAL_NO_CASH_VAT");
        byte[] bytes=new ObjectMapper().writeValueAsBytes(envelope);
        var signer=Signature.getInstance("SHA256withRSA");signer.initSign(pair.getPrivate());signer.update(bytes);
        Map<String,Object> record=new LinkedHashMap<>();record.put("key_version","1");record.put("numero_documento","FT DEMO26/42");
        record.put("public_key",pair.getPublic().getEncoded());record.put("signature",originalSignature);
        record.put("envelope",bytes);record.put("envelope_signature",signer.sign());
        var db=mock(JdbcTemplate.class);when(db.queryForList(anyString(),eq(42L))).thenReturn(List.of(record));
        var evidence=new DemoSaftEvidence(db,new ObjectMapper(),env);
        assertThat(evidence.isOwnNormalVatInvoice(42L)).isTrue();
        byte[] altered=bytes.clone();altered[altered.length-2]^=1;record.put("envelope",altered);
        assertThatThrownBy(()->evidence.isOwnNormalVatInvoice(42L)).isInstanceOf(SaftFtExport.Refusal.class)
                .hasMessageContaining("Prova adicional demo inválida");
        record.put("envelope",bytes);env.setProperty("fac.demo.saft.manifest-sha256","other");
        var wrongManifest=new DemoSaftEvidence(db,new ObjectMapper(),env);
        assertThatThrownBy(()->wrongManifest.isOwnNormalVatInvoice(42L)).isInstanceOf(SaftFtExport.Refusal.class)
                .hasMessageContaining("Vínculo da prova demo divergente");
    }
}
