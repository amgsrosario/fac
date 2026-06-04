package com.ar2lda.fac.controller.dto;

public record UtilizadorDto(
        String codigo,
        String nome,
        String email,
        boolean inativo
) {
}
