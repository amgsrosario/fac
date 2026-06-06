package com.ar2lda.fac.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record DocumentoFinanceiroCreateDto(
        @NotBlank(message = "Tipo de documento Ã© obrigatÃ³rio")
        @Size(max = 3, message = "Tipo de documento deve ter no mÃ¡ximo 3 caracteres")
        String tipoDocumentoId,

        @NotBlank(message = "SÃ©rie Ã© obrigatÃ³ria")
        @Size(max = 10, message = "SÃ©rie deve ter no mÃ¡ximo 10 caracteres")
        String serie,

        @NotNull(message = "Data de emissÃ£o Ã© obrigatÃ³ria")
        LocalDate dataEmissao,

        @NotNull(message = "Cliente Ã© obrigatÃ³rio")
        Long clienteId,

        @NotBlank(message = "Moeda Ã© obrigatÃ³ria")
        @Size(max = 3, message = "Moeda deve ter no mÃ¡ximo 3 caracteres")
        String moedaId,

        @NotNull(message = "Modo de pagamento Ã© obrigatÃ³rio")
        Integer mPagamentoId,

        OffsetDateTime dataHoraOperacao,

        @NotBlank(message = "Emissor Ã© obrigatÃ³rio enquanto a seguranÃ§a estiver desativada")
        @Size(max = 20, message = "Emissor deve ter no mÃ¡ximo 20 caracteres")
        String emissorId,

        @Size(max = 250, message = "ObservaÃ§Ãµes devem ter no mÃ¡ximo 250 caracteres")
        String observacoes,

        @NotEmpty(message = "Documento financeiro deve ter pelo menos uma linha")
        List<@Valid LinhaDocumentoFinanceiroCreateDto> linhas
) {
}
