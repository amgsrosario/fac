package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.PPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.PPagamentoDto;
import com.ar2lda.fac.controller.dto.PPagamentoUpdateDto;
import com.ar2lda.fac.model.PPagamento;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PPagamentoMapper {

    PPagamentoDto toDTO(PPagamento entity);

    PPagamento fromCreate(PPagamentoCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(PPagamentoUpdateDto dto, @MappingTarget PPagamento entity);
}
