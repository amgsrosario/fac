package com.ar2lda.fac.controller.dto;

import java.util.List;

public record ContaCorrenteClienteDiagnosticoDto(
        Long clienteId,
        String clienteNome,
        List<ContaCorrenteMoedaResumoDto> totais,
        List<ContaCorrenteDocumentoDto> documentos,
        List<String> alertas
) {
}
