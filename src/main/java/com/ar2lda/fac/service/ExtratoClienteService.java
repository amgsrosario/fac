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
        List<Long> ids = resolveClienteIds(clienteIds);
        return ids.stream()
                .map(clienteId -> getExtrato(clienteId, dataInicial, dataFinal))
                .toList();
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
        movimentos.sort(MOVIMENTO_COMPARATOR);

        TreeSet<String> moedas = new TreeSet<>();
        moedas.add(cliente.getMoeda().getId());
        moedas.addAll(anteriores.keySet());
        movimentos.forEach(movimento -> moedas.add(movimento.projection().getMoedaId()));

        List<ExtratoClienteMoedaDto> blocos = moedas.stream()
                .map(moedaId -> buildMoeda(moedaId, anteriores.getOrDefault(moedaId, new TotaisMutaveis()), movimentos))
                .toList();

        return new ExtratoClienteDto(
                cliente.getId(),
                cliente.getNome(),
                cliente.getNif(),
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

    private List<Long> resolveClienteIds(List<Long> clienteIds) {
        if (clienteIds == null || clienteIds.isEmpty()) {
            return clienteRepository.findAll().stream()
                    .sorted(Comparator.comparing(Cliente::getNome, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(Cliente::getId))
                    .map(Cliente::getId)
                    .toList();
        }
        return List.copyOf(new LinkedHashSet<>(clienteIds));
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
