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
        String descricaoCae,
        String nomeComercial,
        String telefone,
        String iban,
        String bicSwift,
        String observacoesLegais,
        String textoRodape,
        byte[] logo,
        String logoMediaType
) {
    public EmitenteFiscalSnapshotDto(String nome, String nif, String morada, String morada1, String codPostal,
                                     String localidade, String pais, String email, String web,
                                     BigDecimal capitalSocial, String matriculaRegistoComercial,
                                     String cae, String descricaoCae) {
        this(nome, nif, morada, morada1, codPostal, localidade, pais, email, web, capitalSocial,
                matriculaRegistoComercial, cae, descricaoCae, null, null, null, null, null, null, null, null);
    }
}
