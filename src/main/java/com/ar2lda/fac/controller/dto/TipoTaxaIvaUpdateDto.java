package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoTaxaIvaUpdateDto(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 50, message = "Descrição deve ter no máximo 50 caracteres")
        String descricao,
        boolean inativo
) {
}
