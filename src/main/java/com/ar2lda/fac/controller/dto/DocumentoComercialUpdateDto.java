package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record DocumentoComercialUpdateDto(
        @NotNull(message = "Data de emissão é obrigatória")
        LocalDate dataEmissao,
        Long moradaEnvioId,
        @NotNull(message = "Armazém de carga é obrigatório")
        Long armazemCargaId,
        String moedaId,
        String rivaId,
        Integer mPagamentoId,
        String pPagamentoId,
        Integer transporteId,
        LocalDate dataCarga,
        LocalTime horaCarga,
        @Size(max = 100, message = "Matrícula deve ter no máximo 100 caracteres")
        String matricula,
        LocalDate dataDescarga,
        LocalTime horaDescarga,
        BigDecimal peso,
        @Size(max = 250, message = "Observações devem ter no máximo 250 caracteres")
        String observacoes
) {
}
