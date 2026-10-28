package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import java.util.Optional;

/**
 * Trusted fiscal signing/persistence boundary, not a public input contract.
 * Implementations must return the actual signature and software certificate bound
 * to the numbered snapshot, durably recorded in the emission transaction.
 * QR extraction does not replace signing, signature verification or certification.
 * This release supplies no production signer, keys, certificate or fake fallback.
 * The separately guarded DemoSignatureSource emits a different, explicitly non-fiscal
 * format; it never returns a synthetic Signature through this production method.
 */
public interface FiscalDocumentSignatureSource {
    record Signature(Long documentId, String documentNumber, String atcud,
                     String fullSignature, String softwareCertificateNumber) { }

    Optional<Signature> commercial(DocumentoComercialImpressaoDto document);

    /** Receipts are outside the signing implementation of this mission. */
    default Optional<Signature> financial(DocumentoFinanceiroImpressaoDto document) { return Optional.empty(); }
}
