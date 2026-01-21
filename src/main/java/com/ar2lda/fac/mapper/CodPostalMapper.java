package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.CodPostalCreateDto;
import com.ar2lda.fac.controller.dto.CodPostalDto;
import com.ar2lda.fac.model.CodPostal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CodPostalMapper {
    CodPostalDto toDTO(CodPostal entity);
    CodPostal fromCreate(CodPostalCreateDto dto);
}