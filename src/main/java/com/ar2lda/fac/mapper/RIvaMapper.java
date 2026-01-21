package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.RIvaDto;
import com.ar2lda.fac.controller.dto.TransporteDto;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Transporte;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RIvaMapper {

    RIvaDto toDto(RIva entity);
    RIva toEntity(RIvaDto dto);




    @ObjectFactory
    default RIva create(RIvaDto dto){
        return new RIva(dto.id(),
                dto.nome(),
                dto.isenta(),
                dto.reduzida(),
                dto.intermedia(),
                dto.normal()
        );
    }
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    //@Mapping(target = "id", ignore = true) // não permitir trocar PK num update
    void updateEntityFromDto(RIvaDto dto, @MappingTarget RIva entity);

}
