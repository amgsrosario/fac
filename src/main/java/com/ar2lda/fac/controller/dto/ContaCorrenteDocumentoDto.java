package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContaCorrenteDocumentoDto(
        Long pendenteId,
        Long documentoComercialId,
        String tipoDocumentoId,
        String serie,
        Long numeroDocumento,
        LocalDate dataDocumento,
        LocalDate dataVencimento,
        String estado,
        String moedaId,
        BigDecimal valorDocumento,
        BigDecimal valorRecebidoAtivo,
        BigDecimal valorRecebidoAnulado,
        BigDecimal valorPendente,
        List<ContaCorrenteMovimentoDto> movimentos
) {
}
