package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record MoedaDto(
        String id,
        String nome,
        BigDecimal vcompra,
        BigDecimal vvenda,
        String simbolo,
        Integer ndecimais,
        String ciso
) {}
