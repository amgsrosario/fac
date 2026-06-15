package com.ar2lda.fac.reporting.extrato;

import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteDto;

public record ExtratoClienteReportData(
        EmpresaDto empresa,
        ExtratoClienteDto extrato
) {
}
