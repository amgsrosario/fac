package com.ar2lda.fac.controller.dto;

import java.util.List;

public record ContaCorrentePendentePageDto(
        List<PendenteDto> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        List<ContaCorrentePendenteResumoMoedaDto> totais
) {
}
