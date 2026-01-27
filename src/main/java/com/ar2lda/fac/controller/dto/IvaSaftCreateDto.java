package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IvaSaftCreateDto(
        @NotBlank(message = "Id é obrigatório")
        @Size(max = 3, message = "Id deve ter no máximo 3 caracteres")
        String id,
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
        String nome
) {}
