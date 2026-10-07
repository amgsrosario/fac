package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.TipoArtigo;

import java.math.BigDecimal;

public record ArtigoDto(
        String codigo,
        String abreviatura,
        String codigoIdentificacao,
        String descricao,
        TipoArtigo tipoArtigo,
        String unidade,
        Long familiaId,
        BigDecimal peso,
        String ivaCompraId,
        String ivaVendaId,
        BigDecimal pvp,
        boolean inativo,
        boolean retencao,
        String observacoes,
        String mIsencaoId, String fundamentoFiscal
) {
    public ArtigoDto(String codigo, String abreviatura, String codigoIdentificacao, String descricao, TipoArtigo tipoArtigo, String unidade, Long familiaId, BigDecimal peso, String ivaCompraId, String ivaVendaId, BigDecimal pvp, boolean inativo, boolean retencao, String observacoes) {
        this(codigo, abreviatura, codigoIdentificacao, descricao, tipoArtigo, unidade, familiaId, peso, ivaCompraId, ivaVendaId, pvp, inativo, retencao, observacoes, null, null);
    }

}
