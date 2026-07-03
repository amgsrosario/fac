package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReordenarLinhasDocumentoDto(
        @NotNull(message = "Lista de linhas e obrigatoria")
        List<@NotNull(message = "Identificador de linha e obrigatorio") Long> linhaIds
) {
}
