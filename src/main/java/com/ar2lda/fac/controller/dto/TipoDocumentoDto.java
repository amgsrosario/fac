package com.ar2lda.fac.controller.dto;

public record TipoDocumentoDto(
        String id,
        String descricao,
        String codigoFiscal,
        String modeloEmissao1,
        String modeloEmissao2,
        String modeloEmissao3,
        String modeloEmissao4,
        Integer areaGestao,
        Integer entidade,
        Integer sinalContabilistico,
        boolean liquidacaoImediata
) {
}
