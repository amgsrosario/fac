package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank(message = "Utilizador ou email e obrigatorio")
        @Size(max = 100, message = "Utilizador ou email deve ter no maximo 100 caracteres")
        String username,
        @NotBlank(message = "Password e obrigatoria")
        @Size(max = 72, message = "Password deve ter no maximo 72 caracteres")
        String password
) {
}
