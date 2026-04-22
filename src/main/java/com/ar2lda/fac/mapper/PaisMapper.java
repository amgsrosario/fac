package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.CodPostalCreateDto;
import com.ar2lda.fac.controller.dto.CodPostalDto;
import com.ar2lda.fac.controller.dto.PaisCreateDto;
import com.ar2lda.fac.controller.dto.PaisDto;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Pais;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaisMapper {
    PaisDto toDTO(Pais entity);
    Pais fromCreate(PaisCreateDto dto);
}