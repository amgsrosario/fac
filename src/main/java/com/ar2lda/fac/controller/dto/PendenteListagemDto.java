package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PendenteListagemDto(
        Long documentoId,
        String documento,
        LocalDate data,
        LocalDate vencimento,
        Long clienteId,
        String clienteCodigo,
        String clienteNome,
        String moedaId,
        BigDecimal total,
        BigDecimal recebido,
        BigDecimal pendente
) {
}
