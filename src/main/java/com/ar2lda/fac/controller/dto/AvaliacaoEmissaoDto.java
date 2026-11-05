package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AvaliacaoEmissaoDto(
        Instant avaliadoEm, List<Motivo> impedimentos, List<Motivo> avisos,
        boolean confirmacaoRecalculoNecessaria, BigDecimal totalApresentado, BigDecimal totalProjectado) {
    public record Motivo(String codigo, String mensagem) {}
    public boolean elegivel() { return impedimentos.isEmpty(); }
}
