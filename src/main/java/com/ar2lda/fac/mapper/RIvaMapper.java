package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.RIvaCreateDto;
import com.ar2lda.fac.controller.dto.RIvaDto;
import com.ar2lda.fac.controller.dto.RIvaUpdateDto;
import com.ar2lda.fac.model.RIva;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RIvaMapper {
    default RIvaDto toDTO(RIva entity) {
        if (entity == null) {
            return null;
        }
        return new RIvaDto(entity.getId(), entity.getNome(), entity.getIsenta(), entity.getReduzida(),
                entity.getIntermedia(), entity.getNormal());
    }

    default RIva fromCreateDTO(RIvaCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new RIva(dto.id(), dto.nome(), dto.isenta(), dto.reduzida(), dto.intermedia(), dto.normal());
    }

    default void applyUpdate(RIvaUpdateDto dto, @MappingTarget RIva entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
        entity.setIsenta(dto.isenta());
        entity.setReduzida(dto.reduzida());
        entity.setIntermedia(dto.intermedia());
        entity.setNormal(dto.normal());
    }
}
