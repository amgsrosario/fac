package com.ar2lda.fac.reporting.pendentes;

import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.EmpresaMapper;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.EmpresaRepository;
import com.ar2lda.fac.service.ListagensService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PendentesReportDataService {

    private final ListagensService listagensService;
    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public PendentesReportData pendentes(List<Long> clienteIds) {
        return data(
                "Todos os pendentes",
                "Inclui documentos vencidos e nao vencidos com valor pendente.",
                null,
                clienteIds,
                listagensService.pendentes(clienteIds),
                false
        );
    }

    @Transactional(readOnly = true)
    public PendentesReportData pendentesAData(LocalDate dataReferencia, List<Long> clienteIds) {
        return data(
                "Valores pendentes numa data",
                "Consulta os valores que se encontravam pendentes em " + dataReferencia
                        + ", incluindo documentos vencidos e nao vencidos.",
                dataReferencia,
                clienteIds,
                listagensService.pendentesAData(dataReferencia, clienteIds),
                true
        );
    }

    private PendentesReportData data(
            String titulo,
            String regra,
            LocalDate dataReferencia,
            List<Long> clienteIds,
            com.ar2lda.fac.controller.dto.PendentesListagemDto pendentes,
            boolean pendentesAData
    ) {
        var empresa = empresaRepository.findById(Empresa.EMPRESA_ID)
                .orElseThrow(() -> new NotFoundException("Ficha da empresa nao encontrada"));
        return new PendentesReportData(
                empresaMapper.toDTO(empresa),
                pendentes,
                titulo,
                regra + " Clientes: " + clientes(clienteIds),
                dataReferencia,
                OffsetDateTime.now(),
                pendentesAData
        );
    }

    private String clientes(List<Long> clienteIds) {
        List<Long> filtro = clienteIds == null ? List.of() : clienteIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (filtro.isEmpty()) {
            return "Todos os clientes";
        }
        List<Cliente> clientes = clienteRepository.findAllById(filtro);
        clientes.sort(Comparator.comparing(Cliente::getNome, Comparator.nullsLast(String::compareToIgnoreCase)));
        return clientes.stream()
                .map(cliente -> "%d - %s".formatted(cliente.getId(), cliente.getNome()))
                .toList()
                .toString();
    }
}
