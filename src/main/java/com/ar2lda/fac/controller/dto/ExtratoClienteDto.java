package com.ar2lda.fac.controller.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ExtratoClienteDto(
        Long clienteId,
        String clienteNome,
        String clienteNif,
        LocalDate dataInicial,
        LocalDate dataFinal,
        OffsetDateTime geradoEm,
        List<ExtratoClienteMoedaDto> moedas
) {
}
