package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FreguesiaUpdateDto(
        @NotBlank(message = "Concelho é obrigatório")
        @Size(max = 50, message = "Concelho deve ter no máximo 50 caracteres")
        String concelho,
        @NotBlank(message = "Freguesia é obrigatória")
        @Size(max = 80, message = "Freguesia deve ter no máximo 80 caracteres")
        String nome,
        boolean extinta
) {
}
