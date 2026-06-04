package com.ar2lda.fac.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RIvaCreateDto(
        @NotBlank(message = "Código é obrigatório")
        @Size(max = 3, message = "Código deve ter no máximo 3 caracteres")
        String id,
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 30, message = "Nome deve ter no máximo 30 caracteres")
        String nome,
        @NotEmpty(message = "Regime de IVA deve ter pelo menos uma taxa")
        List<@Valid RIvaTaxaDto> taxas
) {}
