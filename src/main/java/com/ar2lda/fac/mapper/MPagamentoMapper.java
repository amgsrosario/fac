package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.MPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.MPagamentoDto;
import com.ar2lda.fac.controller.dto.MPagamentoUpdateDto;
import com.ar2lda.fac.model.MPagamento;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MPagamentoMapper {

    MPagamentoDto toDTO(MPagamento entity);

    MPagamento fromCreate(MPagamentoCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(MPagamentoUpdateDto dto, @MappingTarget MPagamento entity);
}
