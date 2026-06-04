package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SerieCreateDto(
        @NotBlank(message = "Série é obrigatória")
        @Size(max = 10, message = "Série deve ter no máximo 10 caracteres")
        String serie,
        @NotBlank(message = "Tipo de documento é obrigatório")
        @Size(min = 3, max = 3, message = "Tipo de documento deve ter 3 caracteres")
        String tipoDocumentoId,
        @NotBlank(message = "Nome da série é obrigatório")
        @Size(max = 50, message = "Nome da série deve ter no máximo 50 caracteres")
        String nome,
        @Size(max = 100, message = "Código AT deve ter no máximo 100 caracteres")
        String codigoAt,
        LocalDate dataCodigoAt
) {
}
