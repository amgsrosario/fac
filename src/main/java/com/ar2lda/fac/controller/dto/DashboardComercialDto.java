package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardComercialDto(
        Periodo periodo,
        String moedaId,
        BigDecimal vendas,
        BigDecimal recebimentos,
        BigDecimal valorEmAberto,
        DocumentosVencidos documentosVencidos,
        List<Evolucao> evolucao,
        List<ClienteSaldo> clientesComMaiorSaldo
) {
    public record Periodo(LocalDate dataInicio, LocalDate dataFim) {}

    public record DocumentosVencidos(long quantidade, BigDecimal valor) {}

    public record Evolucao(String periodo, BigDecimal vendas, BigDecimal recebimentos) {}

    public record ClienteSaldo(
            Long clienteId,
            String clienteNome,
            BigDecimal saldo,
            long documentosPendentes,
            LocalDate vencimentoMaisAntigo
    ) {}
}
