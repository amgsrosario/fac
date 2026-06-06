package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.PendenteDto;
import com.ar2lda.fac.model.Pendente;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PendenteMapper {

    default PendenteDto toDTO(Pendente entity) {
        if (entity == null) {
            return null;
        }
        return new PendenteDto(
                entity.getId(),
                entity.getDocumentoComercial() != null ? entity.getDocumentoComercial().getId() : null,
                entity.getCliente() != null ? entity.getCliente().getId() : null,
                entity.getTipoDocumento() != null ? entity.getTipoDocumento().getId() : null,
                entity.getNumeroDocumento(),
                entity.getSerieDocumento(),
                entity.getValorDocumento(),
                entity.getValorPendente(),
                entity.getDataDocumento(),
                entity.getDataVencimento(),
                entity.getMoeda() != null ? entity.getMoeda().getId() : null
        );
    }
}
