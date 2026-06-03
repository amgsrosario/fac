package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.PPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.PPagamentoDto;
import com.ar2lda.fac.controller.dto.PPagamentoUpdateDto;
import com.ar2lda.fac.model.PPagamento;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PPagamentoMapper {
    default PPagamentoDto toDTO(PPagamento entity) {
        if (entity == null) {
            return null;
        }
        return new PPagamentoDto(entity.getId(), entity.getNome(), entity.getDias());
    }

    default PPagamento fromCreateDTO(PPagamentoCreateDto dto) {
        if (dto == null) {
            return null;
        }
        PPagamento entity = new PPagamento();
        entity.setId(dto.id());
        entity.setNome(dto.nome());
        entity.setDias(dto.dias());
        return entity;
    }

    default void applyUpdate(PPagamentoUpdateDto dto, @MappingTarget PPagamento entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
        entity.setDias(dto.dias());
    }
}
