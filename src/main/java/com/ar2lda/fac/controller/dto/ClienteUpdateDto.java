package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.*;

public record ClienteUpdateDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 80, message = "Nome deve ter no máximo 80 caracteres")
        String nome,
        @NotBlank(message = "Morada é obrigatória")
        @Size(max = 60, message = "Morada deve ter no máximo 60 caracteres")
        String morada,
        @Size(max = 60, message = "Morada1 deve ter no máximo 60 caracteres")
        String morada1,
        @Size(max = 50, message = "Localidade deve ter no máximo 50 caracteres")
        String localidade,
        @NotBlank(message = "NIF é obrigatório")
        @Size(min = 9, max = 9, message = "NIF deve ter 9 caracteres")
        String nif,
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String tel,
        @Size(max = 20, message = "Telemóvel deve ter no máximo 20 caracteres")
        String tm,
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 120, message = "Email deve ter no máximo 120 caracteres")
        String email,
        @Email(message = "Email inválido")
        @Size(max = 120, message = "Email1 deve ter no máximo 120 caracteres")
        String email1,
        @Size(max = 20, message = "TSPIVA deve ter no máximo 20 caracteres")
        String tspiva,
        @Size(max = 34, message = "IBAN deve ter no máximo 34 caracteres")
        String iban,
        boolean retencao,
        boolean inativo,
        @Size(max = 300, message = "Observações deve ter no máximo 300 caracteres")
        String observacoes,
        @NotBlank(message = "Código Postal é obrigatório")
        String codPostalId,
        @NotBlank(message = "Moeda é obrigatória")
        String moedaId,
        Integer mPagamentoId,
        Integer pPagamentoId,
        String rivaId,
        Integer transporteId
) {}
