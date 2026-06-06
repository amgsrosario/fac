package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EmpresaCreateDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,
        @NotBlank(message = "NIF é obrigatório")
        @Size(max = 20, message = "NIF deve ter no máximo 20 caracteres")
        String nif,
        @NotBlank(message = "Morada é obrigatória")
        @Size(max = 60, message = "Morada deve ter no máximo 60 caracteres")
        String morada,
        @Size(max = 60, message = "Morada complementar deve ter no máximo 60 caracteres")
        String morada1,
        @NotBlank(message = "Código postal é obrigatório")
        @Size(max = 20, message = "Código postal deve ter no máximo 20 caracteres")
        String codPostalId,
        @NotBlank(message = "Localidade é obrigatória")
        @Size(max = 50, message = "Localidade deve ter no máximo 50 caracteres")
        String localidade,
        @NotBlank(message = "País é obrigatório")
        @Size(max = 3, message = "País deve ter no máximo 3 caracteres")
        String paisId,
        @Size(max = 6, message = "Freguesia deve ter no máximo 6 caracteres")
        String freguesiaId,
        @NotNull(message = "Capital social é obrigatório")
        @DecimalMin(value = "0.00", message = "Capital social não pode ser negativo")
        BigDecimal capitalSocial,
        @NotBlank(message = "Matrícula do registo comercial é obrigatória")
        @Size(max = 100, message = "Matrícula do registo comercial deve ter no máximo 100 caracteres")
        String matriculaRegistoComercial,
        @NotBlank(message = "CAE é obrigatório")
        @Size(max = 10, message = "CAE deve ter no máximo 10 caracteres")
        String cae,
        @NotBlank(message = "Descrição do CAE é obrigatória")
        @Size(max = 100, message = "Descrição do CAE deve ter no máximo 100 caracteres")
        String descricaoCae,
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        @Size(max = 120, message = "Email deve ter no máximo 120 caracteres")
        String email,
        @Size(max = 120, message = "Web deve ter no máximo 120 caracteres")
        String web
) {
}
