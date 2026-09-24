package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record ContaCorrentePendenteResumoMoedaDto(
        String moedaId,
        long quantidade,
        long quantidadeAbertos,
        BigDecimal valorDocumento,
        BigDecimal valorPendente
) {
}
