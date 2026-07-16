package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record ListagemDocumentoComercialDto(
        DocumentoComercialDto documento,
        BigDecimal valorLiquido
) {
}
