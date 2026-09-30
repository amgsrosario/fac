package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ExtratoClienteDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteMoedaDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteMovimentoDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteTotaisDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.projection.ExtratoAnteriorProjection;
import com.ar2lda.fac.repository.projection.ExtratoMovimentoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import com.ar2lda.fac.repository.projection.ExtratoClienteProjection;
import com.ar2lda.fac.repository.projection.ExtratosAnteriorProjection;
import com.ar2lda.fac.repository.projection.ExtratosMovimentoProjection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class ExtratoClienteService {

    static final int CLIENTES_POR_BLOCO = 100;

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

    private final ClienteRepository clienteRepository;
    private final DocumentoComercialRepository documentoComercialRepository;
    private final DocumentoFinanceiroRepository documentoFinanceiroRepository;

    @Transactional(readOnly = true)
    public List<ExtratoClienteDto> getExtratos(
            List<Long> clienteIds,
            LocalDate dataInicial,
            LocalDate dataFinal
    ) {
        validatePeriodo(dataInicial, dataFinal);
        List<ExtratoClienteDto> result = new ArrayList<>();
        if (clienteIds == null || clienteIds.isEmpty()) {
            Long aposId = null;
            while (true) {
                var clientes = clienteRepository.findClientesExtratoApos(aposId, PageRequest.of(0, CLIENTES_POR_BLOCO));
                if (clientes.isEmpty()) break;
                addBloco(result, clientes, dataInicial, dataFinal);
                aposId = clientes.getLast().getId();
                if (clientes.size() < CLIENTES_POR_BLOCO) break;
            }
            // Sort only the required response DTOs, preserving Java's original Unicode/case ordering.
            result.sort(Comparator.comparing(ExtratoClienteDto::clienteNome, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ExtratoClienteDto::clienteId));
        } else {
            List<Long> ids = List.copyOf(new LinkedHashSet<>(clienteIds));
            for (int inicio = 0; inicio < ids.size(); inicio += CLIENTES_POR_BLOCO) {
                var blocoIds = ids.subList(inicio, Math.min(inicio + CLIENTES_POR_BLOCO, ids.size()));
                var porId = clienteRepository.findClientesExtratoPorIds(blocoIds).stream()
                        .collect(Collectors.toMap(ExtratoClienteProjection::getId, Function.identity()));
                var clientes = blocoIds.stream().map(id -> {
                    var cliente = porId.get(id);
                    if (cliente == null) throw new NotFoundException("Cliente nao encontrado: " + id);
                    return cliente;
                }).toList();
                addBloco(result, clientes, dataInicial, dataFinal);
            }
        }
        // The existing JSON/export contract requires the complete output, but no entity graph or
        // universe-wide intermediate collection of clients, ids or source movements is retained.
        return Collections.unmodifiableList(result);
    }

    private void addBloco(List<ExtratoClienteDto> result, List<ExtratoClienteProjection> clientes,
                          LocalDate dataInicial, LocalDate dataFinal) {
        List<Long> ids = clientes.stream().map(ExtratoClienteProjection::getId).toList();
        Map<Long, Map<String, TotaisMutaveis>> anteriores = new LinkedHashMap<>();
        addAnterioresBloco(anteriores, documentoComercialRepository.findExtratosAnterior(ids, dataInicial));
        addAnterioresBloco(anteriores, documentoFinanceiroRepository.findExtratosAnterior(ids, dataInicial));
        Map<Long, List<MovimentoFonte>> movimentos = new LinkedHashMap<>();
        addMovimentosBloco(movimentos, "COMERCIAL", documentoComercialRepository.findExtratosMovimentos(ids, dataInicial, dataFinal));
        addMovimentosBloco(movimentos, "FINANCEIRO", documentoFinanceiroRepository.findExtratosMovimentos(ids, dataInicial, dataFinal));
        for (var cliente : clientes) {
            result.add(buildExtrato(cliente.getId(), cliente.getNome(), cliente.getNif(), cliente.getMoedaId(),
                    dataInicial, dataFinal, anteriores.getOrDefault(cliente.getId(), Map.of()),
                    movimentos.getOrDefault(cliente.getId(), new ArrayList<>())));
        }
    }

    private void addAnterioresBloco(Map<Long, Map<String, TotaisMutaveis>> target,
                                    List<ExtratosAnteriorProjection> projections) {
        for (var projection : projections) {
            target.computeIfAbsent(projection.getClienteId(), id -> new LinkedHashMap<>())
                    .computeIfAbsent(projection.getMoedaId(), id -> new TotaisMutaveis())
                    .add(projection.getDebito(), projection.getCredito());
        }
    }

    private void addMovimentosBloco(Map<Long, List<MovimentoFonte>> target, String origem,
                                    List<ExtratosMovimentoProjection> projections) {
        for (var projection : projections) {
            target.computeIfAbsent(projection.getClienteId(), id -> new ArrayList<>())
                    .add(new MovimentoFonte(origem, "COMERCIAL".equals(origem), projection));
        }
    }

    @Transactional(readOnly = true)
    public ExtratoClienteDto getExtrato(Long clienteId, LocalDate dataInicial, LocalDate dataFinal) {
        validatePeriodo(dataInicial, dataFinal);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado: " + clienteId));

        Map<String, TotaisMutaveis> anteriores = new LinkedHashMap<>();
        addAnteriores(anteriores, documentoComercialRepository.findExtratoAnterior(clienteId, dataInicial));
        addAnteriores(anteriores, documentoFinanceiroRepository.findExtratoAnterior(clienteId, dataInicial));

        List<MovimentoFonte> movimentos = new ArrayList<>();
        addMovimentos(movimentos, "COMERCIAL",
                documentoComercialRepository.findExtratoMovimentos(clienteId, dataInicial, dataFinal));
        addMovimentos(movimentos, "FINANCEIRO",
                documentoFinanceiroRepository.findExtratoMovimentos(clienteId, dataInicial, dataFinal));
        return buildExtrato(cliente.getId(), cliente.getNome(), cliente.getNif(), cliente.getMoeda().getId(),
                dataInicial, dataFinal, anteriores, movimentos);
    }

    private ExtratoClienteDto buildExtrato(Long clienteId, String nome, String nif, String moedaBase,
                                           LocalDate dataInicial, LocalDate dataFinal,
                                           Map<String, TotaisMutaveis> anteriores, List<MovimentoFonte> movimentos) {
        movimentos.sort(MOVIMENTO_COMPARATOR);

        TreeSet<String> moedas = new TreeSet<>();
        moedas.add(moedaBase);
        moedas.addAll(anteriores.keySet());
        movimentos.forEach(movimento -> moedas.add(movimento.projection().getMoedaId()));

        List<ExtratoClienteMoedaDto> blocos = moedas.stream()
                .map(moedaId -> buildMoeda(moedaId, anteriores.getOrDefault(moedaId, new TotaisMutaveis()), movimentos))
                .toList();

        return new ExtratoClienteDto(
                clienteId,
                nome,
                nif,
                dataInicial,
                dataFinal,
                OffsetDateTime.now(),
                blocos
        );
    }

    private ExtratoClienteMoedaDto buildMoeda(
            String moedaId,
            TotaisMutaveis anterior,
            List<MovimentoFonte> todosMovimentos
    ) {
        BigDecimal saldo = anterior.saldo();
        TotaisMutaveis periodo = new TotaisMutaveis();
        List<ExtratoClienteMovimentoDto> movimentos = new ArrayList<>();

        for (MovimentoFonte fonte : todosMovimentos) {
            ExtratoMovimentoProjection projection = fonte.projection();
            if (!moedaId.equals(projection.getMoedaId())) {
                continue;
            }
            saldo = addMovimento(movimentos, periodo, fonte, projection, saldo);
            if (fonte.comercial() && Boolean.TRUE.equals(projection.getLiquidacaoImediata())
                    && projection.getSinalContabilistico() == 1) {
                saldo = addMovimento(
                        movimentos,
                        periodo,
                        fonte,
                        new RecebimentoImediatoProjection(projection),
                        saldo
                );
            }
        }

        TotaisMutaveis totalFinal = anterior.plus(periodo);
        return new ExtratoClienteMoedaDto(
                moedaId,
                anterior.toDto(),
                List.copyOf(movimentos),
                periodo.toDto(),
                totalFinal.toDto()
        );
    }

    private void addAnteriores(Map<String, TotaisMutaveis> target, List<ExtratoAnteriorProjection> projections) {
        for (ExtratoAnteriorProjection projection : projections) {
            target.computeIfAbsent(projection.getMoedaId(), key -> new TotaisMutaveis())
                    .add(projection.getDebito(), projection.getCredito());
        }
    }

    private void addMovimentos(
            List<MovimentoFonte> target,
            String origem,
            List<ExtratoMovimentoProjection> projections
    ) {
        boolean comercial = "COMERCIAL".equals(origem);
        projections.forEach(projection -> target.add(new MovimentoFonte(origem, comercial, projection)));
    }

    private BigDecimal addMovimento(
            List<ExtratoClienteMovimentoDto> movimentos,
            TotaisMutaveis periodo,
            MovimentoFonte fonte,
            ExtratoMovimentoProjection projection,
            BigDecimal saldoAtual
    ) {
        BigDecimal valor = scale(projection.getValor());
        BigDecimal debito = projection.getSinalContabilistico() == 1 ? valor : ZERO;
        BigDecimal credito = projection.getSinalContabilistico() == 2 ? valor : ZERO;
        periodo.add(debito, credito);
        BigDecimal novoSaldo = scale(saldoAtual.add(debito).subtract(credito));
        movimentos.add(new ExtratoClienteMovimentoDto(
                projection.getId(),
                fonte.origem(),
                projection.getData(),
                projection.getMomento(),
                projection.getTipoDocumentoId(),
                projection.getSerie(),
                projection.getNumeroDocumento(),
                projection.getDescricao(),
                projection.getDataVencimento(),
                debito,
                credito,
                novoSaldo
        ));
        return novoSaldo;
    }

    private void validatePeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial == null || dataFinal == null) {
            throw new BadRequestException("Data inicial e data final sao obrigatorias");
        }
        if (dataInicial.isAfter(dataFinal)) {
            throw new BadRequestException("Data inicial nao pode ser posterior a data final");
        }
    }

    private static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(6, RoundingMode.HALF_UP);
    }

    private static final Comparator<MovimentoFonte> MOVIMENTO_COMPARATOR = Comparator
            .comparing((MovimentoFonte movimento) -> movimento.projection().getData())
            .thenComparing(movimento -> movimento.projection().getMomento(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(MovimentoFonte::origem)
            .thenComparing(movimento -> movimento.projection().getId());

    private record MovimentoFonte(String origem, boolean comercial, ExtratoMovimentoProjection projection) {
    }

    private record RecebimentoImediatoProjection(ExtratoMovimentoProjection base) implements ExtratoMovimentoProjection {
        @Override
        public Long getId() {
            return base.getId();
        }

        @Override
        public LocalDate getData() {
            return base.getData();
        }

        @Override
        public OffsetDateTime getMomento() {
            return base.getMomento();
        }

        @Override
        public String getTipoDocumentoId() {
            return base.getTipoDocumentoId();
        }

        @Override
        public String getSerie() {
            return base.getSerie();
        }

        @Override
        public Long getNumeroDocumento() {
            return base.getNumeroDocumento();
        }

        @Override
        public String getDescricao() {
            return "Recebimento imediato - " + base.getDescricao();
        }

        @Override
        public LocalDate getDataVencimento() {
            return null;
        }

        @Override
        public String getMoedaId() {
            return base.getMoedaId();
        }

        @Override
        public Integer getSinalContabilistico() {
            return 2;
        }

        @Override
        public Boolean getLiquidacaoImediata() {
            return false;
        }

        @Override
        public BigDecimal getValor() {
            return base.getValor();
        }
    }

    private static class TotaisMutaveis {
        private BigDecimal debito = ZERO;
        private BigDecimal credito = ZERO;

        void add(BigDecimal valorDebito, BigDecimal valorCredito) {
            debito = scale(debito.add(scale(valorDebito)));
            credito = scale(credito.add(scale(valorCredito)));
        }

        BigDecimal saldo() {
            return scale(debito.subtract(credito));
        }

        TotaisMutaveis plus(TotaisMutaveis other) {
            TotaisMutaveis result = new TotaisMutaveis();
            result.add(debito.add(other.debito), credito.add(other.credito));
            return result;
        }

        ExtratoClienteTotaisDto toDto() {
            return new ExtratoClienteTotaisDto(debito, credito, saldo());
        }
    }
}
