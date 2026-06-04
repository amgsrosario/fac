package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.TipoDocumentoCreateDto;
import com.ar2lda.fac.controller.dto.TipoDocumentoDto;
import com.ar2lda.fac.controller.dto.TipoDocumentoUpdateDto;
import com.ar2lda.fac.model.TipoDocumento;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TipoDocumentoMapper {

    default TipoDocumentoDto toDTO(TipoDocumento entity) {
        if (entity == null) {
            return null;
        }
        return new TipoDocumentoDto(
                entity.getId(),
                entity.getDescricao(),
                entity.getModeloEmissao1(),
                entity.getModeloEmissao2(),
                entity.getModeloEmissao3(),
                entity.getModeloEmissao4(),
                entity.getAreaGestao(),
                entity.getEntidade(),
                entity.getSinalContabilistico(),
                entity.isLiquidacaoImediata()
        );
    }

    default TipoDocumento fromCreateDTO(TipoDocumentoCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new TipoDocumento(
                dto.id(),
                dto.descricao(),
                dto.modeloEmissao1(),
                dto.modeloEmissao2(),
                dto.modeloEmissao3(),
                dto.modeloEmissao4(),
                dto.areaGestao(),
                dto.entidade(),
                dto.sinalContabilistico(),
                dto.liquidacaoImediata()
        );
    }

    default void applyUpdate(TipoDocumentoUpdateDto dto, @MappingTarget TipoDocumento entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setDescricao(dto.descricao());
        entity.setModeloEmissao1(dto.modeloEmissao1());
        entity.setModeloEmissao2(dto.modeloEmissao2());
        entity.setModeloEmissao3(dto.modeloEmissao3());
        entity.setModeloEmissao4(dto.modeloEmissao4());
        entity.setAreaGestao(dto.areaGestao());
        entity.setEntidade(dto.entidade());
        entity.setSinalContabilistico(dto.sinalContabilistico());
        entity.setLiquidacaoImediata(dto.liquidacaoImediata());
    }
}
