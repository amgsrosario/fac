package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.IvaSaftDto;
import com.ar2lda.fac.model.IvaSaft;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IvaSaftMapper {
    IvaSaftDto toDTO(IvaSaft entity);
}
