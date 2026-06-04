package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.MIsencaoCreateDto;
import com.ar2lda.fac.controller.dto.MIsencaoDto;
import com.ar2lda.fac.controller.dto.MIsencaoUpdateDto;
import com.ar2lda.fac.model.MIsencao;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MIsencaoMapper {
    default MIsencaoDto toDTO(MIsencao entity) {
        if (entity == null) {
            return null;
        }
        return new MIsencaoDto(entity.getId(), entity.getNome(),
                entity.getIvaSaft() != null ? entity.getIvaSaft().getId() : null);
    }

    default MIsencao fromCreateDTO(MIsencaoCreateDto dto) {
        if (dto == null) {
            return null;
        }
        MIsencao entity = new MIsencao();
        entity.setId(dto.id());
        entity.setNome(dto.nome());
        return entity;
    }

    default void applyUpdate(MIsencaoUpdateDto dto, @MappingTarget MIsencao entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
    }
}
