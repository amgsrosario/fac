package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TipoDocumentoUpdateDto(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 50, message = "Descrição deve ter no máximo 50 caracteres")
        String descricao,
        @Size(min = 2, max = 2, message = "Código fiscal deve ter 2 caracteres")
        String codigoFiscal,
        @Size(max = 25, message = "Modelo de emissão 1 deve ter no máximo 25 caracteres")
        String modeloEmissao1,
        @Size(max = 25, message = "Modelo de emissão 2 deve ter no máximo 25 caracteres")
        String modeloEmissao2,
        @Size(max = 25, message = "Modelo de emissão 3 deve ter no máximo 25 caracteres")
        String modeloEmissao3,
        @Size(max = 25, message = "Modelo de emissão 4 deve ter no máximo 25 caracteres")
        String modeloEmissao4,
        @NotNull(message = "Área de gestão é obrigatória")
        @Min(value = 1, message = "Área de gestão deve ser >= 1")
        @Max(value = 99, message = "Área de gestão deve ser <= 99")
        Integer areaGestao,
        @NotNull(message = "Entidade é obrigatória")
        @Min(value = 1, message = "Entidade deve ser >= 1")
        @Max(value = 9, message = "Entidade deve ser <= 9")
        Integer entidade,
        @NotNull(message = "Sinal contabilístico é obrigatório")
        @Min(value = 1, message = "Sinal contabilístico deve ser 1 ou 2")
        @Max(value = 2, message = "Sinal contabilístico deve ser 1 ou 2")
        Integer sinalContabilistico,
        boolean liquidacaoImediata
) {
}
