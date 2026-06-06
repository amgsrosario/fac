package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FreguesiaCreateDto(
        @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "Código deve ter 6 dígitos")
        String codigo,
        @NotBlank(message = "Código de distrito é obrigatório")
        @Pattern(regexp = "^\\d{2}$", message = "Código de distrito deve ter 2 dígitos")
        String codigoDistrito,
        @NotBlank(message = "Código de concelho é obrigatório")
        @Pattern(regexp = "^\\d{2}$", message = "Código de concelho deve ter 2 dígitos")
        String codigoConcelho,
        @NotBlank(message = "Código de freguesia é obrigatório")
        @Pattern(regexp = "^\\d{2}$", message = "Código de freguesia deve ter 2 dígitos")
        String codigoFreguesia,
        @NotBlank(message = "Concelho é obrigatório")
        @Size(max = 50, message = "Concelho deve ter no máximo 50 caracteres")
        String concelho,
        @NotBlank(message = "Freguesia é obrigatória")
        @Size(max = 80, message = "Freguesia deve ter no máximo 80 caracteres")
        String nome,
        boolean extinta
) {
    @AssertTrue(message = "Código deve corresponder a distrito + concelho + freguesia")
    public boolean isCodigoCoerente() {
        return codigo != null
                && codigoDistrito != null
                && codigoConcelho != null
                && codigoFreguesia != null
                && codigo.equals(codigoDistrito + codigoConcelho + codigoFreguesia);
    }
}
