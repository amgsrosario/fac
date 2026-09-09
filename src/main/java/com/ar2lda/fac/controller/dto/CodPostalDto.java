package com.ar2lda.fac.controller.dto;

public record CodPostalDto(
        String id,
        String nome,
        String distrito,
        String concelho,
        String freguesia
) {
    public CodPostalDto(String id, String nome) {
        this(id, nome, null, null, null);
    }
}
