package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.Size;

public record ParametrosDocumentoComercialUpdateDto(
        @Size(max = 3, message = "Tipo de documento deve ter no máximo 3 caracteres")
        String tipoDocumentoId,
        @Size(max = 10, message = "Série deve ter no máximo 10 caracteres")
        String serie,
        Long armazemCargaId
) {
}
