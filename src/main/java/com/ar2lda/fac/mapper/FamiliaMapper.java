package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.FamiliaCreateDto;
import com.ar2lda.fac.controller.dto.FamiliaDto;
import com.ar2lda.fac.controller.dto.FamiliaUpdateDto;
import com.ar2lda.fac.model.Familia;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FamiliaMapper {

    default FamiliaDto toDTO(Familia entity) {
        if (entity == null) {
            return null;
        }
        return new FamiliaDto(entity.getId(), entity.getDescricao());
    }

    default Familia fromCreateDTO(FamiliaCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Familia(dto.descricao());
    }

    default void applyUpdate(FamiliaUpdateDto dto, @MappingTarget Familia entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setDescricao(dto.descricao());
    }
}
