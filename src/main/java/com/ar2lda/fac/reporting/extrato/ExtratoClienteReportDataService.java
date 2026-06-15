package com.ar2lda.fac.reporting.extrato;

import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.EmpresaMapper;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.repository.EmpresaRepository;
import com.ar2lda.fac.service.ExtratoClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExtratoClienteReportDataService {

    private final ExtratoClienteService extratoClienteService;
    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    @Transactional(readOnly = true)
    public ExtratoClienteReportData getData(Long clienteId, LocalDate dataInicial, LocalDate dataFinal) {
        var extrato = extratoClienteService.getExtrato(clienteId, dataInicial, dataFinal);
        var empresa = empresaRepository.findById(Empresa.EMPRESA_ID)
                .orElseThrow(() -> new NotFoundException("Ficha da empresa nao encontrada"));
        return new ExtratoClienteReportData(empresaMapper.toDTO(empresa), extrato);
    }
}
