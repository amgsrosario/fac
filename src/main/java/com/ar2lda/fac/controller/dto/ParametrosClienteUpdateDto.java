package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.Size;

public record ParametrosClienteUpdateDto(
        @Size(max = 3, message = "País deve ter no máximo 3 caracteres")
        String paisId,
        @Size(max = 3, message = "Moeda deve ter no máximo 3 caracteres")
        String moedaId,
        @Size(max = 3, message = "Regime de IVA deve ter no máximo 3 caracteres")
        String rivaId,
        String mPagamentoId,
        @Size(max = 3, message = "Prazo de pagamento deve ter no máximo 3 caracteres")
        String pPagamentoId,
        String transporteId,
        Boolean retencao
) {
}
