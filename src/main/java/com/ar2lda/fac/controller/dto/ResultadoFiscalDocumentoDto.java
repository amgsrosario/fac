package com.ar2lda.fac.controller.dto;

public record ResultadoFiscalDocumentoDto(
        Integer motorVersion,
        String mercado,
        String tratamentoLiquidacao,
        String fundamentoFiscal,
        String mIsencaoCodigo,
        String jurisdicao,
        String territorioFiscal,
        boolean recalculoAviso
) {}
