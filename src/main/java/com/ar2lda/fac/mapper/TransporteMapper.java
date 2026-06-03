package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.TransporteCreateDto;
import com.ar2lda.fac.controller.dto.TransporteDto;
import com.ar2lda.fac.controller.dto.TransporteUpdateDto;
import com.ar2lda.fac.model.Transporte;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransporteMapper {
    default TransporteDto toDTO(Transporte entity) {
        if (entity == null) {
            return null;
        }
        return new TransporteDto(entity.getId(), entity.getNome());
    }

    default Transporte fromCreateDTO(TransporteCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Transporte(dto.nome());
    }

    default void applyUpdate(TransporteUpdateDto dto, @MappingTarget Transporte entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
    }
}
