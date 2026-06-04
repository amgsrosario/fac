package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TipoTaxaIvaCreateDto(
        @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "^[A-Z0-9_]{1,20}$", message = "Código deve conter apenas maiúsculas, números ou underscore")
        String id,
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 50, message = "Descrição deve ter no máximo 50 caracteres")
        String descricao,
        boolean inativo
) {
}
