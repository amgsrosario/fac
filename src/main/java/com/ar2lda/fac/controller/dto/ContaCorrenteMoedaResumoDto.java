package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record ContaCorrenteMoedaResumoDto(
        String moedaId,
        long documentos,
        long vencidos,
        BigDecimal valorDocumento,
        BigDecimal valorRecebidoAtivo,
        BigDecimal valorRecebidoAnulado,
        BigDecimal valorPendente
) {
}
