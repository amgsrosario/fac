package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ExtratoClienteMovimentoDto(
        Long id,
        String origem,
        LocalDate data,
        OffsetDateTime momento,
        String tipoDocumentoId,
        String serie,
        Long numeroDocumento,
        String descricao,
        LocalDate dataVencimento,
        BigDecimal debito,
        BigDecimal credito,
        BigDecimal saldoAcumulado
) {
}
