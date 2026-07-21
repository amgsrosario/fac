package com.ar2lda.fac.reporting.pendentes;

import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.PendentesListagemDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PendentesReportData(
        EmpresaDto empresa,
        PendentesListagemDto pendentes,
        String titulo,
        String filtros,
        LocalDate dataReferencia,
        OffsetDateTime geradoEm,
        boolean pendentesAData
) {
}
