package com.ar2lda.fac.controller.dto;

import java.util.List;

public record DocumentoComercialImpressaoDto(
        EmitenteFiscalSnapshotDto empresa,
        DocumentoComercialDto documento,
        List<LinhaDocumentoComercialDto> linhas
) {
}
