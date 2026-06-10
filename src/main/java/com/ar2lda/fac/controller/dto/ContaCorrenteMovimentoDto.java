package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaCorrenteMovimentoDto(
        Long documentoFinanceiroId,
        String tipoDocumentoId,
        String serie,
        Long numeroDocumento,
        LocalDate dataEmissao,
        boolean anulado,
        BigDecimal valorALiquidar,
        BigDecimal descontoValor,
        BigDecimal valorLiquido,
        BigDecimal novoValorPendente
) {
}
