package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArmazemUpdateDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,
        @NotBlank(message = "Morada é obrigatória")
        @Size(max = 60, message = "Morada deve ter no máximo 60 caracteres")
        String morada,
        @Size(max = 60, message = "Morada complementar deve ter no máximo 60 caracteres")
        String morada1,
        @NotBlank(message = "Código postal é obrigatório")
        @Size(max = 20, message = "Código postal deve ter no máximo 20 caracteres")
        String codPostalId,
        @NotBlank(message = "Localidade é obrigatória")
        @Size(max = 50, message = "Localidade deve ter no máximo 50 caracteres")
        String localidade,
        @NotBlank(message = "País é obrigatório")
        @Size(max = 3, message = "País deve ter no máximo 3 caracteres")
        String paisId,
        @Size(max = 6, message = "Freguesia deve ter no máximo 6 caracteres")
        String freguesiaId
) {
}
