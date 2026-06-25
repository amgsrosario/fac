package com.ar2lda.fac.controller.dto;

public record ImportacaoResumoDto(
        int totalLinhas,
        int linhasValidas,
        int linhasComErro,
        int linhasComAviso,
        int registosNovos,
        int duplicados,
        int linhasIgnoradas
) {
}
