package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record ResultadoFiscalLinhaDto(
        BigDecimal valorBruto,
        BigDecimal valorDesconto,
        BigDecimal baseTributavel,
        BigDecimal taxaAplicavel,
        BigDecimal ivaCalculado,
        BigDecimal ivaLiquidado,
        BigDecimal totalLinha,
        String mIsencaoCodigo,
        String fundamentoFiscal,
        String tratamentoLiquidacao
) {}
