package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record ExtratoClienteTotaisDto(
        BigDecimal debito,
        BigDecimal credito,
        BigDecimal saldo
) {
}
