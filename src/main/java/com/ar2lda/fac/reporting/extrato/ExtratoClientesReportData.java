package com.ar2lda.fac.reporting.extrato;

import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteDto;

import java.time.LocalDate;
import java.util.List;

public record ExtratoClientesReportData(
        EmpresaDto empresa,
        List<ExtratoClienteDto> extratos,
        LocalDate dataInicial,
        LocalDate dataFinal
) {
}
