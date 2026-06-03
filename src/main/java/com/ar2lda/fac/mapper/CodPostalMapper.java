package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.CodPostalCreateDto;
import com.ar2lda.fac.controller.dto.CodPostalDto;
import com.ar2lda.fac.controller.dto.CodPostalUpdateDto;
import com.ar2lda.fac.model.CodPostal;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CodPostalMapper {
    default CodPostalDto toDTO(CodPostal entity) {
        if (entity == null) {
            return null;
        }
        return new CodPostalDto(entity.getId(), entity.getNome());
    }

    default CodPostal fromCreateDTO(CodPostalCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new CodPostal(dto.id(), dto.nome());
    }

    default void applyUpdate(CodPostalUpdateDto dto, @MappingTarget CodPostal entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
    }
}
