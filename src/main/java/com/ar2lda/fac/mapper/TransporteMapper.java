package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.TransporteDto;
import com.ar2lda.fac.model.Transporte;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface TransporteMapper {

    //@Mapping(target = "id", ignore = true)
    //@Mapping(target = "nome", ignore = true)
    Transporte toEntity(TransporteDto dto);
    TransporteDto toDTO(Transporte transporte);


    @ObjectFactory
    default Transporte create(TransporteDto dto){
        return new Transporte(dto.nome());
    }
}
