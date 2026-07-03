package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.TipoDescontoLinha;
import com.ar2lda.fac.model.TipoLinhaDocumento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LinhaDocumentoComercialCreateDto(
        TipoLinhaDocumento tipoLinha,
        String artigoId,
        @Size(max = 80, message = "Descricao deve ter no maximo 80 caracteres")
        String descricao,
        @DecimalMin(value = "0.000001", message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade,
        @DecimalMin(value = "0.000000", message = "Preco unitario nao pode ser negativo")
        BigDecimal precoUnitario,
        TipoDescontoLinha tipoDesconto,
        @DecimalMin(value = "0.000000", message = "Desconto nao pode ser negativo")
        BigDecimal desconto,
        String tipoTaxaIvaId,
        BigDecimal peso
) {
}
