package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.EstadoDocumentoComercial;

import java.util.List;

public record DocumentoComercialDiagnosticoDto(
        Long documentoId,
        String referencia,
        EstadoDocumentoComercial estado,
        boolean anulado,
        boolean impresso,
        boolean liquidado,
        boolean temLinhas,
        boolean podeEmitir,
        boolean podeAnular,
        DocumentoComercialDiagnosticoPendenteDto pendente,
        DocumentoComercialDiagnosticoTotaisDto totais,
        List<String> alertas,
        List<String> bloqueios
) {
}
