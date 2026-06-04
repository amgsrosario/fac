package com.ar2lda.fac.controller.dto;

import java.util.List;

public record RIvaDto (
    String id,
    String nome,
    List<RIvaTaxaDto> taxas
){
}
