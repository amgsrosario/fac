package com.ar2lda.fac.controller.dto;

import java.math.BigDecimal;

public record RIvaDto (
    String id,
    String nome,
    BigDecimal isenta,
    BigDecimal reduzida,
    BigDecimal intermedia,
    BigDecimal normal
){
}
