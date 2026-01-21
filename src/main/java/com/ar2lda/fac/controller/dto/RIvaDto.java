package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.RIva;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link RIva}
 */
public record RIvaDto (
    String id,
    String nome,
    BigDecimal isenta,
    BigDecimal reduzida,
    BigDecimal intermedia,
    BigDecimal normal
){
}