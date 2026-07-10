package com.ar2lda.fac.controller.dto;

public record ParametrosClienteDto(
        Long id,
        String paisId,
        String moedaId,
        String rivaId,
        String mPagamentoId,
        String pPagamentoId,
        String transporteId,
        Boolean retencao
) {
}
