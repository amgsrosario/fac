package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PPagamentoCreateDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 30, message = "Nome deve ter no máximo 30 caracteres")
        String nome,
        @NotNull(message = "Dias é obrigatório")
        @Min(value = 0, message = "Dias deve ser >= 0")
        Integer dias
) {}
