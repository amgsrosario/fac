package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record EmpresaDto(
        Long id,
        String nome,
        String nif,
        String morada,
        String morada1,
        String codPostalId,
        String localidade,
        String paisId,
        String freguesiaId,
        String codigoDistrito,
        String codigoConcelho,
        String concelho,
        String freguesiaNome,
        BigDecimal capitalSocial,
        String matriculaRegistoComercial,
        String cae,
        String descricaoCae,
        String email,
        String web
) {
}
