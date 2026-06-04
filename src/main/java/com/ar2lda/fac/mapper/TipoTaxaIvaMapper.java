package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.TipoTaxaIvaCreateDto;
import com.ar2lda.fac.controller.dto.TipoTaxaIvaDto;
import com.ar2lda.fac.controller.dto.TipoTaxaIvaUpdateDto;
import com.ar2lda.fac.model.TipoTaxaIva;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TipoTaxaIvaMapper {

    default TipoTaxaIvaDto toDTO(TipoTaxaIva entity) {
        if (entity == null) {
            return null;
        }
        return new TipoTaxaIvaDto(entity.getId(), entity.getDescricao(), entity.isInativo());
    }

    default TipoTaxaIva fromCreateDTO(TipoTaxaIvaCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new TipoTaxaIva(dto.id(), dto.descricao(), dto.inativo());
    }

    default void applyUpdate(TipoTaxaIvaUpdateDto dto, @MappingTarget TipoTaxaIva entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setDescricao(dto.descricao());
        entity.setInativo(dto.inativo());
    }
}
