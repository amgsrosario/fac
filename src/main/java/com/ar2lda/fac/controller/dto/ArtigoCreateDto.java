package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ArtigoCreateDto(
        @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "^[A-Z0-9]{1,50}$", message = "Código deve conter apenas letras maiúsculas e números")
        String codigo,
        @Size(max = 30, message = "Abreviatura deve ter no máximo 30 caracteres")
        String abreviatura,
        @Size(max = 100, message = "Código de identificação deve ter no máximo 100 caracteres")
        String codigoIdentificacao,
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 80, message = "Descrição deve ter no máximo 80 caracteres")
        String descricao,
        @NotBlank(message = "Unidade é obrigatória")
        @Size(max = 3, message = "Unidade deve ter no máximo 3 caracteres")
        String unidade,
        @NotNull(message = "Família é obrigatória")
        Long familiaId,
        @DecimalMin(value = "0.000", message = "Peso não pode ser negativo")
        @Digits(integer = 12, fraction = 3, message = "Peso deve respeitar o formato com 3 casas decimais")
        BigDecimal peso,
        @NotBlank(message = "IVA na compra é obrigatório")
        String ivaCompraId,
        @NotBlank(message = "IVA na venda é obrigatório")
        String ivaVendaId,
        @NotNull(message = "PVP é obrigatório")
        @DecimalMin(value = "0.000000", message = "PVP não pode ser negativo")
        @Digits(integer = 13, fraction = 6, message = "PVP deve respeitar o formato com 6 casas decimais")
        BigDecimal pvp,
        boolean inativo,
        boolean retencao,
        @Size(max = 250, message = "Observações deve ter no máximo 250 caracteres")
        String observacoes
) {
}
