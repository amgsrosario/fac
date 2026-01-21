package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.ClienteDto;
import com.ar2lda.fac.model.Cliente;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class ClienteMapper {

    public ClienteDto toDTO(Cliente e) {
        if (e == null) return null;
        return new ClienteDto(
                e.getId(),
                e.getNome(),
                e.getMorada(),
                e.getMorada1(),
                e.getLocalidade(),
                e.getNif(),
                e.getTel(),
                e.getTm(),
                e.getEmail(),
                e.getEmail1(),
                e.getTspiva(),
                e.getIban(),
                e.isRetencao(),
                e.isInativo(),
                e.getObservacoes(),
                e.getCodPostal() != null ? e.getCodPostal().getId() : null,
                e.getMoeda() != null ? e.getMoeda().getId() : null,
                e.getMPagamento() != null ? e.getMPagamento().getId() : null,
                e.getPPagamento() != null ? e.getPPagamento().getId() : null,
                e.getRiva() != null ? e.getRiva().getId() : null,
                e.getTransporte() != null ? e.getTransporte().getId() : null
        );
    }
}
