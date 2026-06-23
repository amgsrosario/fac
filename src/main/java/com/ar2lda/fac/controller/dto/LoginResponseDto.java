package com.ar2lda.fac.controller.dto;

public record LoginResponseDto(
        String token,
        String type,
        long expiresIn,
        String codigo,
        String nome,
        String papel,
        java.util.Set<String> permissoes
) {
}
