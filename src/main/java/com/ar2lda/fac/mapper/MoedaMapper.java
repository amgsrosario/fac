package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.MoedaDto;
import com.ar2lda.fac.model.Moeda;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MoedaMapper {
    MoedaDto toDTO(Moeda entity);
}