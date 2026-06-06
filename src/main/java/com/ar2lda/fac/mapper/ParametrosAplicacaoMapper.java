package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.ParametrosAplicacaoCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosAplicacaoDto;
import com.ar2lda.fac.controller.dto.ParametrosAplicacaoUpdateDto;
import com.ar2lda.fac.model.ParametrosAplicacao;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ParametrosAplicacaoMapper {

    default ParametrosAplicacaoDto toDTO(ParametrosAplicacao entity) {
        if (entity == null) {
            return null;
        }
        return new ParametrosAplicacaoDto(
                entity.getId(),
                entity.getAtrasoCargaMinutos(),
                entity.getDecimaisQuantidade(),
                entity.getDecimaisValor()
        );
    }

    default ParametrosAplicacao fromCreateDTO(ParametrosAplicacaoCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new ParametrosAplicacao(dto.atrasoCargaMinutos(), dto.decimaisQuantidade(), dto.decimaisValor());
    }

    default void applyUpdate(ParametrosAplicacaoUpdateDto dto, @MappingTarget ParametrosAplicacao entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setAtrasoCargaMinutos(dto.atrasoCargaMinutos());
        entity.setDecimaisQuantidade(dto.decimaisQuantidade());
        entity.setDecimaisValor(dto.decimaisValor());
    }
}
