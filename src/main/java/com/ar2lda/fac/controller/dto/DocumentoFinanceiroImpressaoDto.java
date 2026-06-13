package com.ar2lda.fac.controller.dto;

public record DocumentoFinanceiroImpressaoDto(
        EmpresaDto empresa,
        ClienteDto cliente,
        DocumentoFinanceiroDto documento
) {
}
