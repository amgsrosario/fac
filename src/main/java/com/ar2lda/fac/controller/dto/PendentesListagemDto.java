package com.ar2lda.fac.controller.dto;

import java.util.List;

public record PendentesListagemDto(
        List<PendenteListagemDto> linhas,
        PendenteListagemTotaisDto totais
) {
}
