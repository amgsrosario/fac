package com.ar2lda.fac.controller.dto;

import java.time.LocalDate;

public record SerieDto(
        String serie,
        String tipoDocumentoId,
        String nome,
        String codigoAt,
        LocalDate dataCodigoAt,
        Long numerador
) {
}
