package com.ar2lda.fac.controller.dto;

public record ParametrosDocumentoComercialDto(
        Long id,
        String tipoDocumentoId,
        String serie,
        Long armazemCargaId
) {
}
