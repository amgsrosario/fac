package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.ArtigoCreateDto;
import com.ar2lda.fac.controller.dto.ArtigoDto;
import com.ar2lda.fac.controller.dto.ArtigoUpdateDto;
import com.ar2lda.fac.model.Artigo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ArtigoMapper {

    default ArtigoDto toDTO(Artigo entity) {
        if (entity == null) {
            return null;
        }
        return new ArtigoDto(
                entity.getCodigo(),
                entity.getAbreviatura(),
                entity.getCodigoIdentificacao(),
                entity.getDescricao(),
                entity.getUnidade(),
                entity.getFamilia().getId(),
                entity.getPeso(),
                entity.getIvaCompra().getId(),
                entity.getIvaVenda().getId(),
                entity.getPvp(),
                entity.isInativo(),
                entity.isRetencao(),
                entity.getObservacoes()
        );
    }

    default Artigo fromCreateDTO(ArtigoCreateDto dto) {
        if (dto == null) {
            return null;
        }
        Artigo entity = new Artigo(dto.codigo());
        applyScalars(
                dto.abreviatura(), dto.codigoIdentificacao(), dto.descricao(), dto.unidade(), dto.peso(),
                dto.pvp(), dto.inativo(), dto.retencao(), dto.observacoes(), entity
        );
        return entity;
    }

    default void applyUpdate(ArtigoUpdateDto dto, @MappingTarget Artigo entity) {
        if (dto == null || entity == null) {
            return;
        }
        applyScalars(
                dto.abreviatura(), dto.codigoIdentificacao(), dto.descricao(), dto.unidade(), dto.peso(),
                dto.pvp(), dto.inativo(), dto.retencao(), dto.observacoes(), entity
        );
    }

    private void applyScalars(String abreviatura, String codigoIdentificacao, String descricao, String unidade,
                              java.math.BigDecimal peso, java.math.BigDecimal pvp, boolean inativo,
                              boolean retencao, String observacoes, Artigo entity) {
        entity.setAbreviatura(abreviatura);
        entity.setCodigoIdentificacao(codigoIdentificacao);
        entity.setDescricao(descricao);
        entity.setUnidade(unidade);
        entity.setPeso(peso);
        entity.setPvp(pvp);
        entity.setInativo(inativo);
        entity.setRetencao(retencao);
        entity.setObservacoes(observacoes);
    }
}
