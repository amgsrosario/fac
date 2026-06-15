package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoFinanceiroDto;
import com.ar2lda.fac.model.DocumentoFinanceiro;
import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentoFinanceiroMapper {

    default DocumentoFinanceiroDto toDTO(DocumentoFinanceiro entity, List<LinhaDocumentoFinanceiro> linhas) {
        if (entity == null) {
            return null;
        }
        return new DocumentoFinanceiroDto(
                entity.getId(),
                entity.getCliente() != null ? entity.getCliente().getId() : null,
                entity.getTipoDocumento() != null ? entity.getTipoDocumento().getId() : null,
                entity.getSerie(),
                entity.getNumeroDocumento(),
                entity.getAtcud(),
                entity.getDataEmissao(),
                entity.getMoeda() != null ? entity.getMoeda().getId() : null,
                entity.getValorPagamentoBruto(),
                entity.getValorDescontoFinanceiro(),
                entity.getValorPagamentoLiquido(),
                entity.getMPagamento() != null ? entity.getMPagamento().getId() : null,
                entity.getDataHoraOperacao(),
                entity.getEmissor() != null ? entity.getEmissor().getCodigo() : null,
                entity.getMomentoEmissao(),
                entity.getObservacoes(),
                entity.isAnulado(),
                entity.isImpresso(),
                linhas == null ? List.of() : linhas.stream().map(this::toLinhaDTO).toList()
        );
    }

    default LinhaDocumentoFinanceiroDto toLinhaDTO(LinhaDocumentoFinanceiro linha) {
        if (linha == null) {
            return null;
        }
        return new LinhaDocumentoFinanceiroDto(
                linha.getId(),
                linha.getNumeroLinha(),
                linha.getPendente() != null ? linha.getPendente().getId() : null,
                linha.getDataDocumento(),
                linha.getDataVencimento(),
                linha.getTipoDocumento() != null ? linha.getTipoDocumento().getId() : null,
                linha.getNumeroDocumento(),
                linha.getSerieDocumento(),
                linha.getValorDocumento(),
                linha.getValorPendenteAntes(),
                linha.getValorALiquidar(),
                linha.getDescontoPercentual(),
                linha.getDescontoValor(),
                linha.getValorPagamentoLiquido(),
                linha.getNovoValorPendente(),
                linha.getMoeda() != null ? linha.getMoeda().getId() : null
        );
    }
}
