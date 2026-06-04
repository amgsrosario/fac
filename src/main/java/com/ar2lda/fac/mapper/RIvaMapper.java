package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.RIvaCreateDto;
import com.ar2lda.fac.controller.dto.RIvaDto;
import com.ar2lda.fac.controller.dto.RIvaTaxaDto;
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
        return new RIvaDto(
                entity.getId(),
                entity.getNome(),
                entity.getTaxas().stream()
                        .map(taxa -> new RIvaTaxaDto(taxa.getTipoTaxaIva().getId(), taxa.getValor()))
                        .toList()
        );
    }

    default RIva fromCreateDTO(RIvaCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new RIva(dto.id(), dto.nome());
    }

    default void applyUpdate(RIvaUpdateDto dto, @MappingTarget RIva entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
    }
}
