package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RIvaTaxaDto(
        @NotBlank(message = "Tipo de taxa de IVA é obrigatório")
        @Size(max = 20, message = "Tipo de taxa de IVA deve ter no máximo 20 caracteres")
        String tipoTaxaIvaId,
        @NotNull(message = "Valor da taxa é obrigatório")
        @Digits(integer = 2, fraction = 2, message = "Valor da taxa deve respeitar o formato 99.99")
        BigDecimal valor
) {
}
