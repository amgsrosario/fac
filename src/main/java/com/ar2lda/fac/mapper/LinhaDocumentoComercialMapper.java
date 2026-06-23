package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialDto;
import com.ar2lda.fac.model.LinhaDocumentoComercial;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LinhaDocumentoComercialMapper {

    default LinhaDocumentoComercialDto toDTO(LinhaDocumentoComercial e) {
        return toDTO(e, e != null && e.getDocumentoComercial() != null
                && e.getDocumentoComercial().getFiscalSnapshotVersion() != null);
    }

    default LinhaDocumentoComercialDto toDTO(LinhaDocumentoComercial e, boolean consolidado) {
        if (e == null) {
            return null;
        }
        return new LinhaDocumentoComercialDto(
                e.getId(),
                e.getDocumentoComercial() != null ? e.getDocumentoComercial().getId() : null,
                e.getNumeroLinha(),
                consolidado ? e.getArtigoCodigo() : e.getArtigo() != null ? e.getArtigo().getCodigo() : null,
                e.getDescricao(),
                e.getQuantidade(),
                e.getPrecoUnitario(),
                e.getValorBruto(),
                e.getTipoDesconto(),
                e.getDesconto(),
                e.getValorDesconto(),
                e.getValorLinha(),
                consolidado ? e.getTipoTaxaIvaCodigo() : e.getTipoTaxaIva() != null ? e.getTipoTaxaIva().getId() : null,
                e.getPercentagemIva(),
                e.getPeso(),
                consolidado ? e.getUnidade() : e.getArtigo() != null ? e.getArtigo().getUnidade() : null,
                e.getBaseTributavel(),
                e.getValorImposto(),
                e.getTotalLinha()
        );
    }
}
