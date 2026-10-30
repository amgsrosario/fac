package com.ar2lda.fac.testinfra;

import com.ar2lda.fac.service.FiscalDocumentSignatureSource;
import com.ar2lda.fac.controller.dto.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Synthetic signature/certificate fixture. Not an AT signer or production source. */
@TestConfiguration
public class FiscalSignatureTestConfiguration {
    @Bean public TestSource fiscalTestSignatureSource() { return new TestSource(); }
    public static class TestSource implements FiscalDocumentSignatureSource {
        private final KeyPair keys;
        public boolean invalidSignature;
        public boolean unavailable;
        public TestSource() {
            try {var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);keys=generator.generateKeyPair();}
            catch(GeneralSecurityException e){throw new IllegalStateException(e);}
        }
        public Optional<Signature> commercial(DocumentoComercialImpressaoDto d) {
            var doc=d.documento();return sign(doc.id(),doc.numeroDocumentoCompleto(),doc.atcud(),d.toString());
        }
        public Optional<Signature> financial(DocumentoFinanceiroImpressaoDto d) {
            var doc=d.documento();return sign(doc.id(),doc.tipoDocumentoId()+" "+doc.serie()+"/"+doc.numeroDocumento(),doc.atcud(),d.toString());
        }
        private Optional<Signature> sign(Long id,String number,String atcud,String snapshot) {
            if(unavailable)return Optional.empty();
            try {
                var signer=java.security.Signature.getInstance("SHA256withRSA");signer.initSign(keys.getPrivate());signer.update(snapshot.getBytes(StandardCharsets.UTF_8));
                String recorded=invalidSignature?"BAD":Base64.getEncoder().encodeToString(signer.sign());
                return Optional.of(new Signature(id,number,atcud,recorded,"1234"));
            } catch(GeneralSecurityException e){throw new IllegalStateException(e);}
        }
    }
}
