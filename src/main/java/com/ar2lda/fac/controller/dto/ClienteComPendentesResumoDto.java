package com.ar2lda.fac.controller.dto;

public record ClienteComPendentesResumoDto(
        Long id,
        Long codigo,
        String nome,
        String nif,
        String moedaId,
        String mPagamentoId,
        long quantidadePendentes
) {
}
