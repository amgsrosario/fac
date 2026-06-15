package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record DocumentoFinanceiroDto(
        Long id,
        Long clienteId,
        String tipoDocumentoId,
        String serie,
        Long numeroDocumento,
        String atcud,
        LocalDate dataEmissao,
        String moedaId,
        BigDecimal valorPagamentoBruto,
        BigDecimal valorDescontoFinanceiro,
        BigDecimal valorPagamentoLiquido,
        Integer mPagamentoId,
        OffsetDateTime dataHoraOperacao,
        String emissorId,
        OffsetDateTime momentoEmissao,
        String observacoes,
        boolean anulado,
        boolean impresso,
        List<LinhaDocumentoFinanceiroDto> linhas
) {
}
