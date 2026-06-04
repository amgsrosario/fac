package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UtilizadorCreateDto(
        @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "^[A-Za-z0-9]{1,20}$", message = "Código deve ter apenas caracteres alfanuméricos")
        String codigo,
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
        String email,
        @NotBlank(message = "Password é obrigatória")
        @Size(min = 8, max = 72, message = "Password deve ter entre 8 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S+$",
                message = "Password deve incluir maiúscula, minúscula, número e símbolo, sem espaços"
        )
        String password,
        boolean inativo
) {
}
