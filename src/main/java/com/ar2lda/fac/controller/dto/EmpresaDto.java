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
        String web,
        String nomeComercial,
        String telefone,
        String iban,
        String bicSwift,
        String observacoesLegais,
        String textoRodape,
        String observacoesComerciaisDefault,
        boolean temLogotipo,
        String logotipoMediaType,
        java.time.OffsetDateTime atualizadoEm,
        String atualizadoPor
) {
    public EmpresaDto(Long id, String nome, String nif, String morada, String morada1, String codPostalId,
                      String localidade, String paisId, String freguesiaId, String codigoDistrito,
                      String codigoConcelho, String concelho, String freguesiaNome, BigDecimal capitalSocial,
                      String matriculaRegistoComercial, String cae, String descricaoCae, String email, String web) {
        this(id, nome, nif, morada, morada1, codPostalId, localidade, paisId, freguesiaId, codigoDistrito,
                codigoConcelho, concelho, freguesiaNome, capitalSocial, matriculaRegistoComercial, cae,
                descricaoCae, email, web, null, null, null, null, null, null, null, false, null, null, null);
    }
}
