package com.ar2lda.fac.controller.dto;

import java.util.List;

public record ExtratoClienteMoedaDto(
        String moedaId,
        ExtratoClienteTotaisDto anterior,
        List<ExtratoClienteMovimentoDto> movimentos,
        ExtratoClienteTotaisDto totalPeriodo,
        ExtratoClienteTotaisDto totalFinal
) {
}
