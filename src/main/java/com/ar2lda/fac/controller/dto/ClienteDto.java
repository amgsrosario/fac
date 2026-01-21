package com.ar2lda.fac.controller.dto;

public record ClienteDto(
        Long id,
        String nome,
        String morada,
        String morada1,
        String localidade,
        String nif,
        String tel,
        String tm,
        String email,
        String email1,
        String tspiva,
        String iban,
        boolean retencao,
        boolean inativo,
        String observacoes,
        String codPostalId,
        String moedaId,
        Integer mPagamentoId,
        Integer pPagamentoId,
        String rivaId,
        Integer transporteId
) {}
