package com.ar2lda.fac.controller.dto;

public record ArmazemDto(
        Long id,
        String nome,
        String morada,
        String morada1,
        String codPostalId,
        String localidade,
        String paisId,
        String freguesiaId
) {
}
