package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FamiliaCreateDto(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 30, message = "Descrição deve ter no máximo 30 caracteres")
        String descricao
) {
}
