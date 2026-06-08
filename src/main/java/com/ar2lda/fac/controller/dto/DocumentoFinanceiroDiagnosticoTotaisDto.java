package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record DocumentoFinanceiroDiagnosticoTotaisDto(
        BigDecimal cabecalhoValorPagamentoBruto,
        BigDecimal linhasValorALiquidar,
        BigDecimal cabecalhoValorDescontoFinanceiro,
        BigDecimal linhasValorDesconto,
        BigDecimal cabecalhoValorPagamentoLiquido,
        BigDecimal linhasValorPagamentoLiquido,
        boolean coerente
) {
}
