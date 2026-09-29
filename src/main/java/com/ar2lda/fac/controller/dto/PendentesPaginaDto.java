package com.ar2lda.fac.controller.dto;

import java.util.List;

public record PendentesPaginaDto(
        List<PendenteListagemDto> linhas,
        PendenteListagemTotaisDto totais,
        long totalElements,
        long totalPages
) {
}
