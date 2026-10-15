package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroCreateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import java.time.LocalDate;

/** E1/E2: debt is born on emission; due date never determines receipt value date. */
public final class ReceiptValueDate {
    private ReceiptValueDate() {}
    public static LocalDate effective(DocumentoFinanceiroCreateDto dto) {
        return dto.dataValor() == null ? dto.dataEmissao() : dto.dataValor();
    }
    public static void validate(DocumentoFinanceiroCreateDto dto, Pendente pending) {
        var document = pending.getDocumentoComercial();
        var value = effective(dto);
        if (document.getEstado() != EstadoDocumentoComercial.EMITIDO || document.isAnulado()
                || !Integer.valueOf(1).equals(document.getTipoDocumento().getSinalContabilistico()))
            throw new BadRequestException("Documento credor ou não emitido não é dívida elegível para recebimento");
        if (value == null || dto.dataEmissao() == null || value.isAfter(dto.dataEmissao()))
            throw new BadRequestException("Data Valor não pode ser posterior à emissão do recibo");
        if (value.isBefore(document.getDataEmissao()))
            throw new BadRequestException("Data Valor não pode ser anterior à emissão de qualquer documento liquidado");
    }
}
