package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DocumentoFinanceiroResumoDto(
        Long id,
        Long clienteId,
        String tipoDocumentoId,
        String serie,
        Long numeroDocumento,
        LocalDate dataEmissao,
        String moedaId,
        BigDecimal valorPagamentoLiquido,
        String mPagamentoId,
        String emissorId,
        boolean anulado
) {
}
