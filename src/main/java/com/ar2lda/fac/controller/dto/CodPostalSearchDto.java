package com.ar2lda.fac.controller.dto;

public record CodPostalSearchDto(
        String codigoPostal,
        String nome,
        String concelho,
        String distrito
) {}
