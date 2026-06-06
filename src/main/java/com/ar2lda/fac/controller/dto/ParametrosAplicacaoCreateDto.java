package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ParametrosAplicacaoCreateDto(
        @NotNull(message = "Atraso de carga é obrigatório")
        @Min(value = 0, message = "Atraso de carga não pode ser negativo")
        @Max(value = 120, message = "Atraso de carga deve ter no máximo 120 minutos")
        Integer atrasoCargaMinutos,
        @NotNull(message = "Decimais da quantidade são obrigatórios")
        @Min(value = 0, message = "Decimais da quantidade não podem ser negativos")
        @Max(value = 6, message = "Decimais da quantidade devem ter no máximo 6")
        Integer decimaisQuantidade,
        @NotNull(message = "Decimais do valor são obrigatórios")
        @Min(value = 0, message = "Decimais do valor não podem ser negativos")
        @Max(value = 6, message = "Decimais do valor devem ter no máximo 6")
        Integer decimaisValor
) {
}
