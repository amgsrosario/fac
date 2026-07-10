package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TransporteCreateDto(
        @NotBlank(message = "Codigo e obrigatorio")
        @Size(min = 3, max = 3, message = "Codigo deve ter 3 caracteres")
        String id,
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 30, message = "Nome deve ter no maximo 30 caracteres")
        String nome
) {}
