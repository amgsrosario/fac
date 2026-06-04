package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.UtilizadorCreateDto;
import com.ar2lda.fac.controller.dto.UtilizadorDto;
import com.ar2lda.fac.controller.dto.UtilizadorUpdateDto;
import com.ar2lda.fac.model.Utilizador;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UtilizadorMapper {

    default UtilizadorDto toDTO(Utilizador entity) {
        if (entity == null) {
            return null;
        }
        return new UtilizadorDto(entity.getCodigo(), entity.getNome(), entity.getEmail(), entity.isInativo());
    }

    default Utilizador fromCreateDTO(UtilizadorCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Utilizador(dto.codigo(), dto.nome(), dto.email(), null, dto.inativo());
    }

    default void applyUpdate(UtilizadorUpdateDto dto, @MappingTarget Utilizador entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
        entity.setEmail(dto.email());
        entity.setInativo(dto.inativo());
    }
}
