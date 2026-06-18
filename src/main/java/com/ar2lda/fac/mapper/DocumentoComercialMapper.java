package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.model.DocumentoComercial;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentoComercialMapper {

    default DocumentoComercialDto toDTO(DocumentoComercial e) {
        if (e == null) {
            return null;
        }
        return new DocumentoComercialDto(
                e.getId(),
                e.getTipoDocumento() != null ? e.getTipoDocumento().getId() : null,
                e.getSerie(),
                e.getNumeroDocumento(),
                e.getAtcud(),
                e.temQrFiscal(),
                e.getEstado(),
                e.getDataEmissao(),
                e.getDataVencimento(),
                e.getCliente() != null ? e.getCliente().getId() : null,
                e.getMoradaEnvio() != null ? e.getMoradaEnvio().getId() : null,
                e.getArmazemCarga() != null ? e.getArmazemCarga().getId() : null,
                e.getMoeda() != null ? e.getMoeda().getId() : null,
                e.getRiva() != null ? e.getRiva().getId() : null,
                e.getMPagamento() != null ? e.getMPagamento().getId() : null,
                e.getPPagamento() != null ? e.getPPagamento().getId() : null,
                e.getTransporte() != null ? e.getTransporte().getId() : null,
                e.getClienteNome(),
                e.getClienteNif(),
                e.getClienteMorada(),
                e.getClienteMorada1(),
                e.getClienteCodPostal(),
                e.getClienteLocalidade(),
                e.getClientePais(),
                e.getEnvioNome(),
                e.getEnvioMorada(),
                e.getEnvioMorada1(),
                e.getEnvioCodPostal(),
                e.getEnvioLocalidade(),
                e.getEnvioPais(),
                e.getDataCarga(),
                e.getHoraCarga(),
                e.getMatricula(),
                e.getCargaNome(),
                e.getCargaMorada(),
                e.getCargaMorada1(),
                e.getCargaCodPostal(),
                e.getCargaLocalidade(),
                e.getCargaPais(),
                e.getDataDescarga(),
                e.getHoraDescarga(),
                e.getDescargaMorada(),
                e.getDescargaMorada1(),
                e.getDescargaCodPostal(),
                e.getDescargaLocalidade(),
                e.getDescargaPais(),
                e.getValorBruto(),
                e.getValorDesconto(),
                e.getValorIsento(),
                e.getValorSujeitoReduzida(),
                e.getValorSujeitoIntermedia(),
                e.getValorSujeitoNormal(),
                e.getValorIvaReduzida(),
                e.getValorIvaIntermedia(),
                e.getValorIvaNormal(),
                e.getValorRetencao(),
                e.getValorIvaTotal(),
                e.getValorTotal(),
                e.getPeso(),
                e.getObservacoes(),
                e.getMomentoEmissao(),
                e.getEmissor() != null ? e.getEmissor().getCodigo() : null,
                e.isAnulado(),
                e.isImpresso(),
                e.isLiquidado()
        );
    }
}
