package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.util.List;

public record PreviewFiscalDto(List<ResultadoFiscalLinhaDto> linhas, Totais totais) {

    public record Totais(
            BigDecimal valorBruto,
            BigDecimal valorDesconto,
            BigDecimal valorIvaTotal,
            BigDecimal valorTotal
    ) {}
}
