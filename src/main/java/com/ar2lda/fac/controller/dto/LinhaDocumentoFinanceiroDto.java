package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LinhaDocumentoFinanceiroDto(
        Long id,
        Integer numeroLinha,
        Long pendenteId,
        LocalDate dataDocumento,
        LocalDate dataVencimento,
        String tipoDocumentoId,
        Long numeroDocumento,
        String serieDocumento,
        BigDecimal valorDocumento,
        BigDecimal valorPendenteAntes,
        BigDecimal valorALiquidar,
        BigDecimal descontoPercentual,
        BigDecimal descontoValor,
        BigDecimal valorPagamentoLiquido,
        BigDecimal novoValorPendente,
        String moedaId
) {
}
