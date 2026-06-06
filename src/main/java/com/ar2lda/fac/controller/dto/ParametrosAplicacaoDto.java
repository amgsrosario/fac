package com.ar2lda.fac.controller.dto;

public record ParametrosAplicacaoDto(
        Long id,
        Integer atrasoCargaMinutos,
        Integer decimaisQuantidade,
        Integer decimaisValor
) {
}
