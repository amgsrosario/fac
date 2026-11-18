package com.ar2lda.fac.controller.dto;
/** Persisted governed identity, shared by QR/PDF and future SAF-T; not a public editable input. */
public record FiscalProjectionDto(String externalType,String catalogCode,Long catalogRevision,String documentLabel) {
    public FiscalProjectionDto {
        if(!"FR".equals(externalType)||!"FT".equals(catalogCode)||catalogRevision==null||catalogRevision<=0||!"Fatura-recibo".equals(documentLabel))
            throw new IllegalArgumentException("Invalid persisted FR projection identity");
    }
}
