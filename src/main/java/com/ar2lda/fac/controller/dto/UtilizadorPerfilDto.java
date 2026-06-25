package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.PapelUtilizador;
import jakarta.validation.constraints.NotNull;

public record UtilizadorPerfilDto(
        @NotNull(message = "Perfil é obrigatório") PapelUtilizador papel
) {
}
