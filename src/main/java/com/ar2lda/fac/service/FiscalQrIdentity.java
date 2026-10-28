package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import java.util.Base64;

/** Field extraction from a trusted source. Does not assert cryptographic validity. */
public final class FiscalQrIdentity {
    private final String hashCharacters;
    private final String certificateNumber;
    private FiscalQrIdentity(String hashCharacters, String certificateNumber) {
        this.hashCharacters=hashCharacters; this.certificateNumber=certificateNumber;
    }
    public String hashCharacters() { return hashCharacters; }
    public String certificateNumber() { return certificateNumber; }

    public static FiscalQrIdentity derive(String fullSignature, String certificateNumber) {
        if (fullSignature==null || fullSignature.length()<31)
            throw invalid();
        try {
            byte[] decoded=Base64.getDecoder().decode(fullSignature);
            if (!Base64.getEncoder().encodeToString(decoded).equals(fullSignature)) throw invalid();
        } catch (IllegalArgumentException error) { throw invalid(); }
        if (certificateNumber==null || !certificateNumber.matches("[0-9]{1,4}") || Integer.parseInt(certificateNumber)==0)
            throw invalid();
        // One-based positions 1, 11, 21, 31 of the full recorded signature.
        String q=""+fullSignature.charAt(0)+fullSignature.charAt(10)+fullSignature.charAt(20)+fullSignature.charAt(30);
        return new FiscalQrIdentity(q, certificateNumber);
    }
    private static BadRequestException invalid() {
        return new BadRequestException("Assinatura fiscal ou certificado aplicável inválido para gerar QR");
    }
}
