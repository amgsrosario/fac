package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record DocumentoComercialDiagnosticoTotaisDto(
        BigDecimal cabecalhoValorBruto,
        BigDecimal linhasValorBruto,
        BigDecimal cabecalhoValorDesconto,
        BigDecimal linhasValorDesconto,
        BigDecimal cabecalhoValorLinha,
        BigDecimal linhasValorLinha,
        BigDecimal cabecalhoValorIvaTotal,
        BigDecimal linhasValorIvaTotal,
        BigDecimal cabecalhoValorTotal,
        BigDecimal linhasValorTotal,
        boolean coerente
) {
}
