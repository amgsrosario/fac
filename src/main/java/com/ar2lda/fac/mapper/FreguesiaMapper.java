package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.FreguesiaCreateDto;
import com.ar2lda.fac.controller.dto.FreguesiaDto;
import com.ar2lda.fac.controller.dto.FreguesiaUpdateDto;
import com.ar2lda.fac.model.Freguesia;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FreguesiaMapper {

    default FreguesiaDto toDTO(Freguesia entity) {
        if (entity == null) {
            return null;
        }
        return new FreguesiaDto(
                entity.getCodigo(),
                entity.getCodigoDistrito(),
                entity.getCodigoConcelho(),
                entity.getCodigoFreguesia(),
                entity.getConcelho(),
                entity.getNome(),
                entity.isExtinta()
        );
    }

    default Freguesia fromCreateDTO(FreguesiaCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Freguesia(
                dto.codigo(),
                dto.codigoDistrito(),
                dto.codigoConcelho(),
                dto.codigoFreguesia(),
                dto.concelho(),
                dto.nome(),
                dto.extinta()
        );
    }

    default void applyUpdate(FreguesiaUpdateDto dto, @MappingTarget Freguesia entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setConcelho(dto.concelho());
        entity.setNome(dto.nome());
        entity.setExtinta(dto.extinta());
    }
}
