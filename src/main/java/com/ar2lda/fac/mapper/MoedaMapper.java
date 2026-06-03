package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.MoedaCreateDto;
import com.ar2lda.fac.controller.dto.MoedaDto;
import com.ar2lda.fac.controller.dto.MoedaUpdateDto;
import com.ar2lda.fac.model.Moeda;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MoedaMapper {
    default MoedaDto toDTO(Moeda entity) {
        if (entity == null) {
            return null;
        }
        return new MoedaDto(entity.getId(), entity.getNome(), entity.getVcompra(), entity.getVvenda(),
                entity.getSimbolo(), entity.getNdecimais(), entity.getCiso());
    }

    default Moeda fromCreateDTO(MoedaCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Moeda(dto.id(), dto.nome(), dto.vcompra(), dto.vvenda(), dto.simbolo(), dto.ndecimais(), dto.ciso());
    }

    default void applyUpdate(MoedaUpdateDto dto, @MappingTarget Moeda entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
        entity.setVcompra(dto.vcompra());
        entity.setVvenda(dto.vvenda());
        entity.setSimbolo(dto.simbolo());
        entity.setNdecimais(dto.ndecimais());
        entity.setCiso(dto.ciso());
    }
}
