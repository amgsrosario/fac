package com.ar2lda.fac.controller.dto;

public record ImportacaoErroDto(
        int linha,
        String coluna,
        String valor,
        String codigo,
        String mensagem
) {
}
