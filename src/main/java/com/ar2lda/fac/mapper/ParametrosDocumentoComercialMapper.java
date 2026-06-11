package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialUpdateDto;
import com.ar2lda.fac.model.ParametrosDocumentoComercial;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ParametrosDocumentoComercialMapper {

    default ParametrosDocumentoComercialDto toDTO(ParametrosDocumentoComercial entity) {
        if (entity == null) return null;
        return new ParametrosDocumentoComercialDto(
                entity.getId(),
                entity.getTipoDocumento() != null ? entity.getTipoDocumento().getId() : null,
                entity.getSerie(),
                entity.getArmazemCarga() != null ? entity.getArmazemCarga().getId() : null
        );
    }

    default ParametrosDocumentoComercial fromCreateDTO(ParametrosDocumentoComercialCreateDto dto) {
        return dto == null ? null : new ParametrosDocumentoComercial();
    }

    default void applyUpdate(ParametrosDocumentoComercialUpdateDto dto,
                             @MappingTarget ParametrosDocumentoComercial entity) {
        // As relações são validadas e aplicadas pelo Service.
    }
}
