package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.TipoDadosMestres;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ImportacaoValidacaoDto(
        UUID id,
        TipoDadosMestres tipo,
        String nomeFicheiro,
        String formato,
        OffsetDateTime expiraEm,
        ImportacaoResumoDto resumo,
        List<ImportacaoErroDto> erros,
        List<ImportacaoErroDto> avisos,
        List<Map<String, String>> amostra
) {
}
