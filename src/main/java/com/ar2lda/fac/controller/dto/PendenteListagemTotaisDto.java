package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record PendenteListagemTotaisDto(
        BigDecimal total,
        BigDecimal recebido,
        BigDecimal pendente
) {
}
