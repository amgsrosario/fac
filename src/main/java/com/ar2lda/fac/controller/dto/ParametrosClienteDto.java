package com.ar2lda.fac.controller.dto;

public record ParametrosClienteDto(
        Long id,
        String paisId,
        String moedaId,
        String rivaId,
        Integer mPagamentoId,
        String pPagamentoId,
        Integer transporteId,
        Boolean retencao
) {
}
