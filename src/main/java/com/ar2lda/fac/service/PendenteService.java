package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ContaCorrenteClienteDiagnosticoDto;
import com.ar2lda.fac.controller.dto.ContaCorrenteDocumentoDto;
import com.ar2lda.fac.controller.dto.ContaCorrenteMoedaResumoDto;
import com.ar2lda.fac.controller.dto.ContaCorrenteMovimentoDto;
import com.ar2lda.fac.controller.dto.PendenteDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.PendenteMapper;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.LinhaDocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.PendenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PendenteService {

    private final PendenteRepository repository;
    private final LinhaDocumentoFinanceiroRepository linhaDocumentoFinanceiroRepository;
    private final ClienteRepository clienteRepository;
    private final PendenteMapper mapper;

    public Page<PendenteDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public PendenteDto getById(Long id) {
        return mapper.toDTO(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pendente nao encontrado: " + id)));
    }

    @Transactional(readOnly = true)
    public ContaCorrenteClienteDiagnosticoDto diagnosticoContaCorrenteCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado: " + clienteId));
        List<Pendente> pendentes = repository.findByClienteIdOrderByDataDocumentoAscNumeroDocumentoAsc(clienteId);
        Map<Long, List<LinhaDocumentoFinanceiro>> movimentosPorPendente = movimentosPorPendente(pendentes);
        List<ContaCorrenteDocumentoDto> documentos = pendentes.stream()
                .map(pendente -> buildDocumento(pendente, movimentosPorPendente.getOrDefault(pendente.getId(), List.of())))
                .toList();

        return new ContaCorrenteClienteDiagnosticoDto(
                cliente.getId(),
                cliente.getNome(),
                buildTotais(documentos),
                documentos,
                buildAlertas(documentos)
        );
    }

    public Pendente criarDeDocumento(DocumentoComercial documento) {
        if (documento.getTipoDocumento().isLiquidacaoImediata()) {
            return null;
        }
        if (documento.getEstado() != EstadoDocumentoComercial.EMITIDO || documento.getNumeroDocumento() == null) {
            throw new BadRequestException("Pendente so pode ser criado para documento emitido");
        }
        return repository.findByDocumentoComercialId(documento.getId())
                .orElseGet(() -> repository.save(novoPendente(documento)));
    }

    private Pendente novoPendente(DocumentoComercial documento) {
        BigDecimal valorTotal = documento.getValorTotal();
        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Documento sem valor nao gera pendente");
        }

        Pendente pendente = new Pendente();
        pendente.setDocumentoComercial(documento);
        pendente.setCliente(documento.getCliente());
        pendente.setTipoDocumento(documento.getTipoDocumento());
        pendente.setNumeroDocumento(documento.getNumeroDocumento());
        pendente.setSerieDocumento(documento.getSerie());
        pendente.setValorDocumento(valorTotal);
        pendente.setValorPendente(valorTotal);
        pendente.setDataDocumento(documento.getDataEmissao());
        pendente.setDataVencimento(documento.getDataVencimento());
        pendente.setMoeda(documento.getMoeda());
        return pendente;
    }

    private Map<Long, List<LinhaDocumentoFinanceiro>> movimentosPorPendente(List<Pendente> pendentes) {
        List<Long> ids = pendentes.stream().map(Pendente::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<LinhaDocumentoFinanceiro>> result = new LinkedHashMap<>();
        linhaDocumentoFinanceiroRepository
                .findByPendenteIdInOrderByDocumentoFinanceiroDataEmissaoAscDocumentoFinanceiroNumeroDocumentoAscNumeroLinhaAsc(ids)
                .forEach(linha -> result.computeIfAbsent(linha.getPendente().getId(), key -> new ArrayList<>()).add(linha));
        return result;
    }

    private ContaCorrenteDocumentoDto buildDocumento(Pendente pendente, List<LinhaDocumentoFinanceiro> movimentos) {
        BigDecimal recebidoAtivo = movimentos.stream()
                .filter(linha -> !linha.getDocumentoFinanceiro().isAnulado())
                .map(LinhaDocumentoFinanceiro::getValorALiquidar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal recebidoAnulado = movimentos.stream()
                .filter(linha -> linha.getDocumentoFinanceiro().isAnulado())
                .map(LinhaDocumentoFinanceiro::getValorALiquidar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ContaCorrenteDocumentoDto(
                pendente.getId(),
                pendente.getDocumentoComercial().getId(),
                pendente.getTipoDocumento().getId(),
                pendente.getSerieDocumento(),
                pendente.getNumeroDocumento(),
                pendente.getDataDocumento(),
                pendente.getDataVencimento(),
                estadoPendente(pendente),
                pendente.getMoeda().getId(),
                pendente.getValorDocumento(),
                recebidoAtivo,
                recebidoAnulado,
                pendente.getValorPendente(),
                movimentos.stream().map(this::buildMovimento).toList()
        );
    }

    private ContaCorrenteMovimentoDto buildMovimento(LinhaDocumentoFinanceiro linha) {
        return new ContaCorrenteMovimentoDto(
                linha.getDocumentoFinanceiro().getId(),
                linha.getDocumentoFinanceiro().getTipoDocumento().getId(),
                linha.getDocumentoFinanceiro().getSerie(),
                linha.getDocumentoFinanceiro().getNumeroDocumento(),
                linha.getDocumentoFinanceiro().getDataEmissao(),
                linha.getDocumentoFinanceiro().isAnulado(),
                linha.getValorALiquidar(),
                linha.getDescontoValor(),
                linha.getValorPagamentoLiquido(),
                linha.getNovoValorPendente()
        );
    }

    private String estadoPendente(Pendente pendente) {
        if (pendente.getDocumentoComercial().isAnulado()) {
            return "ANULADO";
        }
        if (pendente.getValorPendente().compareTo(BigDecimal.ZERO) <= 0) {
            return "LIQUIDADO";
        }
        if (pendente.getDataVencimento().isBefore(LocalDate.now())) {
            return "VENCIDO";
        }
        if (pendente.getValorPendente().compareTo(pendente.getValorDocumento()) < 0) {
            return "PARCIAL";
        }
        return "ABERTO";
    }

    private List<ContaCorrenteMoedaResumoDto> buildTotais(List<ContaCorrenteDocumentoDto> documentos) {
        Map<String, List<ContaCorrenteDocumentoDto>> porMoeda = new LinkedHashMap<>();
        documentos.forEach(documento -> porMoeda.computeIfAbsent(documento.moedaId(), key -> new ArrayList<>()).add(documento));

        return porMoeda.entrySet().stream()
                .map(entry -> buildTotalMoeda(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ContaCorrenteMoedaResumoDto::moedaId))
                .toList();
    }

    private ContaCorrenteMoedaResumoDto buildTotalMoeda(String moedaId, List<ContaCorrenteDocumentoDto> documentos) {
        return new ContaCorrenteMoedaResumoDto(
                moedaId,
                documentos.size(),
                documentos.stream().filter(documento -> "VENCIDO".equals(documento.estado())).count(),
                sum(documentos.stream().map(ContaCorrenteDocumentoDto::valorDocumento).toList()),
                sum(documentos.stream().map(ContaCorrenteDocumentoDto::valorRecebidoAtivo).toList()),
                sum(documentos.stream().map(ContaCorrenteDocumentoDto::valorRecebidoAnulado).toList()),
                sum(documentos.stream().map(ContaCorrenteDocumentoDto::valorPendente).toList())
        );
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<String> buildAlertas(List<ContaCorrenteDocumentoDto> documentos) {
        List<String> alertas = new ArrayList<>();
        long moedas = documentos.stream().map(ContaCorrenteDocumentoDto::moedaId).distinct().count();
        if (moedas > 1) {
            alertas.add("Cliente tem movimentos em mais do que uma moeda; totais devem ser lidos por moeda.");
        }
        long vencidos = documentos.stream().filter(documento -> "VENCIDO".equals(documento.estado())).count();
        if (vencidos > 0) {
            alertas.add("Cliente tem documentos vencidos.");
        }
        return alertas;
    }
}
