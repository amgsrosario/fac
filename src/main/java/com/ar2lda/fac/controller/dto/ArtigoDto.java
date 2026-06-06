package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record ArtigoDto(
        String codigo,
        String abreviatura,
        String codigoIdentificacao,
        String descricao,
        String unidade,
        Long familiaId,
        BigDecimal peso,
        String ivaCompraId,
        String ivaVendaId,
        BigDecimal pvp,
        boolean inativo,
        boolean retencao,
        String observacoes
) {
}
