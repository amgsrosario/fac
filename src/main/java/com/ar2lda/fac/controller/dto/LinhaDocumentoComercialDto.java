package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.TipoDescontoLinha;

import java.math.BigDecimal;

public record LinhaDocumentoComercialDto(
        Long id,
        Long documentoComercialId,
        Integer numeroLinha,
        String artigoId,
        String descricao,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal valorBruto,
        TipoDescontoLinha tipoDesconto,
        BigDecimal desconto,
        BigDecimal valorDesconto,
        BigDecimal valorLinha,
        String tipoTaxaIvaId,
        BigDecimal percentagemIva,
        BigDecimal peso
) {
}
