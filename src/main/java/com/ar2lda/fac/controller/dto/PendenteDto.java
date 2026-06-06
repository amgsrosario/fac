package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PendenteDto(
        Long id,
        Long documentoComercialId,
        Long clienteId,
        String tipoDocumentoId,
        Long numeroDocumento,
        String serieDocumento,
        BigDecimal valorDocumento,
        BigDecimal valorPendente,
        LocalDate dataDocumento,
        LocalDate dataVencimento,
        String moedaId
) {
}
