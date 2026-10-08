package com.ar2lda.fac.controller.dto;

import java.util.List;

public record RIvaDto (
    String id,
    String nome,
    List<RIvaTaxaDto> taxas,
        com.ar2lda.fac.model.MercadoFiscal mercado, com.ar2lda.fac.model.TratamentoLiquidacao tratamentoLiquidacao, String fundamentoFiscal, String mIsencaoId, String jurisdicao, com.ar2lda.fac.model.TerritorioFiscal territorioFiscal
){
    public RIvaDto(String id, String nome, List<RIvaTaxaDto> taxas) {
        this(id, nome, taxas, null, null, null, null, null, null);
    }

}
