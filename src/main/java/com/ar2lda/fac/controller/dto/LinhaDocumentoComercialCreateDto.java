package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.TipoDescontoLinha;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LinhaDocumentoComercialCreateDto(
        @NotBlank(message = "Artigo é obrigatório")
        String artigoId,
        @Size(max = 80, message = "Descrição deve ter no máximo 80 caracteres")
        String descricao,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.000001", message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade,
        @NotNull(message = "Preço unitário é obrigatório")
        @DecimalMin(value = "0.000000", message = "Preço unitário não pode ser negativo")
        BigDecimal precoUnitario,
        TipoDescontoLinha tipoDesconto,
        @DecimalMin(value = "0.000000", message = "Desconto não pode ser negativo")
        BigDecimal desconto,
        String tipoTaxaIvaId,
        BigDecimal peso
) {
}
