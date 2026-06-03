package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.PaisCreateDto;
import com.ar2lda.fac.controller.dto.PaisDto;
import com.ar2lda.fac.controller.dto.PaisUpdateDto;
import com.ar2lda.fac.model.Pais;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaisMapper {
    default PaisDto toDTO(Pais entity) {
        if (entity == null) {
            return null;
        }
        return new PaisDto(entity.getId(), entity.getNome());
    }

    default Pais fromCreateDTO(PaisCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Pais(dto.id(), dto.nome());
    }

    default void applyUpdate(PaisUpdateDto dto, @MappingTarget Pais entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
    }
}
