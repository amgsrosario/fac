package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.EstadoDocumentoComercial;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DocumentoComercialResumoDto(
        Long id,
        String tipoDocumentoId,
        String tipoDocumentoDescricao,
        String serie,
        String serieDescricao,
        Long numeroDocumento,
        String numeroDocumentoCompleto,
        String atcud,
        EstadoDocumentoComercial estado,
        LocalDate dataEmissao,
        LocalDate dataVencimento,
        Long clienteId,
        String clienteNome,
        String clienteNif,
        String moedaId,
        String moedaCodigo,
        String moedaSimbolo,
        Integer moedaCasasDecimais,
        BigDecimal valorBruto,
        BigDecimal valorDesconto,
        BigDecimal valorIvaTotal,
        BigDecimal valorRetencao,
        BigDecimal valorTotal,
        boolean anulado,
        String motivoAnulacao,
        boolean impresso,
        boolean liquidado
) {}
