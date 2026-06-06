package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialDto;
import com.ar2lda.fac.model.LinhaDocumentoComercial;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LinhaDocumentoComercialMapper {

    default LinhaDocumentoComercialDto toDTO(LinhaDocumentoComercial e) {
        if (e == null) {
            return null;
        }
        return new LinhaDocumentoComercialDto(
                e.getId(),
                e.getDocumentoComercial() != null ? e.getDocumentoComercial().getId() : null,
                e.getNumeroLinha(),
                e.getArtigo() != null ? e.getArtigo().getCodigo() : null,
                e.getDescricao(),
                e.getQuantidade(),
                e.getPrecoUnitario(),
                e.getValorBruto(),
                e.getTipoDesconto(),
                e.getDesconto(),
                e.getValorDesconto(),
                e.getValorLinha(),
                e.getTipoTaxaIva() != null ? e.getTipoTaxaIva().getId() : null,
                e.getPercentagemIva(),
                e.getPeso()
        );
    }
}
