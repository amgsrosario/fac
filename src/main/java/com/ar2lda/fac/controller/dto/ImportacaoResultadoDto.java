package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.EstadoImportacaoDadosMestres;
import com.ar2lda.fac.model.TipoDadosMestres;

import java.util.List;
import java.util.UUID;

public record ImportacaoResultadoDto(
        UUID id,
        TipoDadosMestres tipo,
        EstadoImportacaoDadosMestres estado,
        ImportacaoResumoDto resumo,
        int criados,
        int rejeitados,
        int ignorados,
        List<ImportacaoErroDto> erros,
        List<ImportacaoErroDto> avisos
) {
}
