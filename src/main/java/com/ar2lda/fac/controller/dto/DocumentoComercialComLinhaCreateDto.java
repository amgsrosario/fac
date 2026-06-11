package com.ar2lda.fac.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DocumentoComercialComLinhaCreateDto(
        @NotNull(message = "Cabecalho do documento e obrigatorio")
        @Valid
        DocumentoComercialCreateDto documento,

        @NotNull(message = "Primeira linha do documento e obrigatoria")
        @Valid
        LinhaDocumentoComercialCreateDto linha
) {
}
