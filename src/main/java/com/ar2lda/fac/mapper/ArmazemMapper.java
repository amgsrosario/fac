package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.ArmazemCreateDto;
import com.ar2lda.fac.controller.dto.ArmazemDto;
import com.ar2lda.fac.controller.dto.ArmazemUpdateDto;
import com.ar2lda.fac.model.Armazem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ArmazemMapper {

    default ArmazemDto toDTO(Armazem entity) {
        if (entity == null) {
            return null;
        }
        return new ArmazemDto(
                entity.getId(),
                entity.getNome(),
                entity.getMorada(),
                entity.getMorada1(),
                entity.getCodPostal() != null ? entity.getCodPostal().getId() : null,
                entity.getLocalidade(),
                entity.getPais() != null ? entity.getPais().getId() : null,
                entity.getFreguesia() != null ? entity.getFreguesia().getCodigo() : null
        );
    }

    default Armazem fromCreateDTO(ArmazemCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Armazem(dto.nome(), dto.morada(), dto.morada1(), dto.localidade());
    }

    default void applyUpdate(ArmazemUpdateDto dto, @MappingTarget Armazem entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
        entity.setMorada(dto.morada());
        entity.setMorada1(dto.morada1());
        entity.setLocalidade(dto.localidade());
    }
}
