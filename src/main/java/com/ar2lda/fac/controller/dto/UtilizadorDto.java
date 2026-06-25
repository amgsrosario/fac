package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.PapelUtilizador;

import java.time.OffsetDateTime;

public record UtilizadorDto(
        String codigo,
        String nome,
        String email,
        PapelUtilizador papel,
        boolean ativo,
        boolean inativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        OffsetDateTime ultimoLoginEm,
        String criadoPor,
        String atualizadoPor
) {
}
