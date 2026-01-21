package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MoedaCreateDto(
        @NotBlank(message = "Código é obrigatório")
        @Size(min = 3, max = 3, message = "Código deve ter 3 letras (ISO)")
        String id,
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 30, message = "Nome deve ter no máximo 30 caracteres")
        String nome,
        BigDecimal vcompra,
        BigDecimal vvenda,
        @NotBlank(message = "Símbolo é obrigatório")
        @Size(max = 5, message = "Símbolo deve ter no máximo 5 caracteres")
        String simbolo,
        @NotNull(message = "Número de decimais é obrigatório")
        @Min(value = 0, message = "Número de decimais deve ser >= 0")
        @Max(value = 6, message = "Número de decimais deve ser <= 6")
        Integer ndecimais,
        @Size(max = 10, message = "Código ISO adicional deve ter no máximo 10 caracteres")
        String ciso
) {}
