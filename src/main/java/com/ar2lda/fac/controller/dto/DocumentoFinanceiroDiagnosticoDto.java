package com.ar2lda.fac.controller.dto;

import java.util.List;

public record DocumentoFinanceiroDiagnosticoDto(
        Long documentoId,
        String referencia,
        boolean anulado,
        boolean impresso,
        boolean temLinhas,
        boolean podeAnular,
        DocumentoFinanceiroDiagnosticoTotaisDto totais,
        List<String> alertas,
        List<String> bloqueios
) {
}
