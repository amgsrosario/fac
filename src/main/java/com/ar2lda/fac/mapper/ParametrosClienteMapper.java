package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.ParametrosClienteCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosClienteDto;
import com.ar2lda.fac.controller.dto.ParametrosClienteUpdateDto;
import com.ar2lda.fac.model.ParametrosCliente;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ParametrosClienteMapper {

    default ParametrosClienteDto toDTO(ParametrosCliente entity) {
        if (entity == null) {
            return null;
        }
        return new ParametrosClienteDto(
                entity.getId(),
                entity.getPais() != null ? entity.getPais().getId() : null,
                entity.getMoeda() != null ? entity.getMoeda().getId() : null,
                entity.getRiva() != null ? entity.getRiva().getId() : null,
                entity.getMPagamento() != null ? entity.getMPagamento().getId() : null,
                entity.getPPagamento() != null ? entity.getPPagamento().getId() : null,
                entity.getTransporte() != null ? entity.getTransporte().getId() : null,
                entity.getRetencao()
        );
    }

    default ParametrosCliente fromCreateDTO(ParametrosClienteCreateDto dto) {
        if (dto == null) {
            return null;
        }
        ParametrosCliente entity = new ParametrosCliente();
        entity.setRetencao(dto.retencao());
        return entity;
    }

    default void applyUpdate(ParametrosClienteUpdateDto dto, @MappingTarget ParametrosCliente entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setRetencao(dto.retencao());
    }
}
