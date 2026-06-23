package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnularDocumentoRequest(
        @NotBlank(message = "Motivo da anulacao e obrigatorio")
        @Size(min = 5, max = 500, message = "Motivo da anulacao deve ter entre 5 e 500 caracteres")
        String motivo
) {
}
