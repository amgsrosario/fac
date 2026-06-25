package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotNull;

public record UtilizadorEstadoDto(
        @NotNull(message = "Estado ativo é obrigatório") Boolean ativo
) {
}
