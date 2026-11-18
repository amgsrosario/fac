package com.ar2lda.fac.controller.dto;
/** Persisted governed identity, shared by QR/PDF and future SAF-T; not a public editable input. */
public record FiscalProjectionDto(String externalType,String catalogCode,Long catalogRevision,String documentLabel) { }
