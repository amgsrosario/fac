package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LinhaDocumentoFinanceiroCreateDto(
        @NotNull(message = "Pendente e obrigatorio")
        Long pendenteId,

        @NotNull(message = "Valor a liquidar e obrigatorio")
        @DecimalMin(value = "0.000001", message = "Valor a liquidar deve ser positivo")
        BigDecimal valorALiquidar,

        @DecimalMin(value = "0.000000", message = "Desconto percentual nao pode ser negativo")
        BigDecimal descontoPercentual,

        @DecimalMin(value = "0.000000", message = "Desconto valor nao pode ser negativo")
        BigDecimal descontoValor
) {
}
