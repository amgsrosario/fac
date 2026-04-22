package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaisCreateDto(
        @NotBlank(message = "Código é obrigatório")
        @Size(max = 3, message = "Código deve ter no máximo 2 caracteres")
        String id,
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
        String nome
) {}
