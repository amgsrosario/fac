package com.ar2lda.fac.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PreviewFiscalRequest(
        @NotBlank String rivaId,
        @NotNull LocalDate dataEmissao,
        @NotNull @Size(max = 500) List<@Valid LinhaDocumentoComercialCreateDto> linhas
) {}
