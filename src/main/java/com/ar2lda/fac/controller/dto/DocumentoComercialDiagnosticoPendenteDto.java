package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record DocumentoComercialDiagnosticoPendenteDto(
        boolean existe,
        Long id,
        BigDecimal valorDocumento,
        BigDecimal valorPendente
) {
}
