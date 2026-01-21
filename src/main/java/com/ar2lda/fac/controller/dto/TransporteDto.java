package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.Transporte;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Transporte}
 */

public record TransporteDto (
    Integer id,
    String nome){
}