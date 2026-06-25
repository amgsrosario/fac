package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UtilizadorPasswordResetDto(
        @NotBlank(message = "Nova password é obrigatória")
        @Size(min = 10, max = 72, message = "Password deve ter entre 10 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S+$",
                message = "Password deve incluir maiúscula, minúscula, número e símbolo, sem espaços"
        )
        String novaPassword
) {
}
