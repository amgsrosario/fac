package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentoComercialEmitirDto(
        @NotBlank(message = "Emissor e obrigatorio enquanto a seguranca estiver desativada")
        @Size(max = 20, message = "Emissor deve ter no maximo 20 caracteres")
        String emissorId
) {
}
