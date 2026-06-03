package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.MPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.MPagamentoDto;
import com.ar2lda.fac.controller.dto.MPagamentoUpdateDto;
import com.ar2lda.fac.model.MPagamento;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MPagamentoMapper {
    default MPagamentoDto toDTO(MPagamento entity) {
        if (entity == null) {
            return null;
        }
        return new MPagamentoDto(entity.getId(), entity.getNome());
    }

    default MPagamento fromCreateDTO(MPagamentoCreateDto dto) {
        if (dto == null) {
            return null;
        }
        MPagamento entity = new MPagamento();
        entity.setNome(dto.nome());
        return entity;
    }

    default void applyUpdate(MPagamentoUpdateDto dto, @MappingTarget MPagamento entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
    }
}
