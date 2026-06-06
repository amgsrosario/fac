package com.ar2lda.fac.controller.dto;

public record FreguesiaDto(
        String codigo,
        String codigoDistrito,
        String codigoConcelho,
        String codigoFreguesia,
        String concelho,
        String nome,
        boolean extinta
) {
}
