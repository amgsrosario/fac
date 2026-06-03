package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.IvaSaftCreateDto;
import com.ar2lda.fac.controller.dto.IvaSaftDto;
import com.ar2lda.fac.controller.dto.IvaSaftUpdateDto;
import com.ar2lda.fac.model.IvaSaft;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IvaSaftMapper {
    default IvaSaftDto toDTO(IvaSaft entity) {
        if (entity == null) {
            return null;
        }
        return new IvaSaftDto(entity.getId(), entity.getNome());
    }

    default IvaSaft fromCreateDTO(IvaSaftCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new IvaSaft(dto.id(), dto.nome());
    }

    default void applyUpdate(IvaSaftUpdateDto dto, @MappingTarget IvaSaft entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
    }
}
