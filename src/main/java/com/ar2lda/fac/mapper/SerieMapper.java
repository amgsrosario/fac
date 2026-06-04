package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.SerieCreateDto;
import com.ar2lda.fac.controller.dto.SerieDto;
import com.ar2lda.fac.controller.dto.SerieUpdateDto;
import com.ar2lda.fac.model.Serie;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SerieMapper {

    default SerieDto toDTO(Serie entity) {
        if (entity == null) {
            return null;
        }
        return new SerieDto(
                entity.getSerie(),
                entity.getTipoDocumento() != null ? entity.getTipoDocumento().getId() : null,
                entity.getNome(),
                entity.getCodigoAt(),
                entity.getDataCodigoAt()
        );
    }

    default Serie fromCreateDTO(SerieCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Serie(dto.serie(), dto.nome(), dto.codigoAt(), dto.dataCodigoAt());
    }

    default void applyUpdate(SerieUpdateDto dto, @MappingTarget Serie entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
        entity.setCodigoAt(dto.codigoAt());
        entity.setDataCodigoAt(dto.dataCodigoAt());
    }
}
