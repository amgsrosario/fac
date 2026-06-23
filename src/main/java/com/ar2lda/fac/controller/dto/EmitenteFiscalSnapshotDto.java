package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record EmitenteFiscalSnapshotDto(
        String nome,
        String nif,
        String morada,
        String morada1,
        String codPostal,
        String localidade,
        String pais,
        String email,
        String web,
        BigDecimal capitalSocial,
        String matriculaRegistoComercial,
        String cae,
        String descricaoCae
) {
}
