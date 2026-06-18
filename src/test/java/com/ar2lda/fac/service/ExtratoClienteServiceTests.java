package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ExtratoClienteDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteMoedaDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.projection.ExtratoAnteriorProjection;
import com.ar2lda.fac.repository.projection.ExtratoMovimentoProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExtratoClienteServiceTests {

    private static final Long CLIENTE_ID = 1001L;
    private static final LocalDate INICIAL = LocalDate.of(2026, 6, 1);
    private static final LocalDate FINAL = LocalDate.of(2026, 6, 30);

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private DocumentoComercialRepository documentoComercialRepository;
    @Mock
    private DocumentoFinanceiroRepository documentoFinanceiroRepository;

    private ExtratoClienteService service;

    @BeforeEach
    void setup() {
        service = new ExtratoClienteService(
                clienteRepository,
                documentoComercialRepository,
                documentoFinanceiroRepository
        );
        Cliente cliente = mock(Cliente.class);
        Moeda moeda = mock(Moeda.class);
        when(cliente.getId()).thenReturn(CLIENTE_ID);
        when(cliente.getNome()).thenReturn("Cliente Teste");
        when(cliente.getNif()).thenReturn("500000001");
        when(cliente.getMoeda()).thenReturn(moeda);
        when(moeda.getId()).thenReturn("EUR");
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(documentoComercialRepository.findExtratoAnterior(CLIENTE_ID, INICIAL)).thenReturn(List.of());
        when(documentoFinanceiroRepository.findExtratoAnterior(CLIENTE_ID, INICIAL)).thenReturn(List.of());
        when(documentoComercialRepository.findExtratoMovimentos(CLIENTE_ID, INICIAL, FINAL)).thenReturn(List.of());
        when(documentoFinanceiroRepository.findExtratoMovimentos(CLIENTE_ID, INICIAL, FINAL)).thenReturn(List.of());
    }

    @Test
    void calculaAnteriorPeriodoFinalESaldoAcumulado() {
        ExtratoAnteriorProjection anteriorComercial = anterior("EUR", "150.25", "20.10");
        ExtratoAnteriorProjection anteriorFinanceiro = anterior("EUR", "0", "30.15");
        ExtratoMovimentoProjection fatura = movimento(1L, "2026-06-01T09:00:00Z", "FT", 1, "100.125");
        ExtratoMovimentoProjection recibo = movimento(2L, "2026-06-30T18:00:00Z", "RC", 2, "40.025");
        when(documentoComercialRepository.findExtratoAnterior(CLIENTE_ID, INICIAL))
                .thenReturn(List.of(anteriorComercial));
        when(documentoFinanceiroRepository.findExtratoAnterior(CLIENTE_ID, INICIAL))
                .thenReturn(List.of(anteriorFinanceiro));
        when(documentoComercialRepository.findExtratoMovimentos(CLIENTE_ID, INICIAL, FINAL))
                .thenReturn(List.of(fatura));
        when(documentoFinanceiroRepository.findExtratoMovimentos(CLIENTE_ID, INICIAL, FINAL))
                .thenReturn(List.of(recibo));

        ExtratoClienteMoedaDto eur = service.getExtrato(CLIENTE_ID, INICIAL, FINAL).moedas().getFirst();

        assertThat(eur.anterior().debito()).isEqualByComparingTo("150.250000");
        assertThat(eur.anterior().credito()).isEqualByComparingTo("50.250000");
        assertThat(eur.anterior().saldo()).isEqualByComparingTo("100.000000");
        assertThat(eur.movimentos()).hasSize(2);
        assertThat(eur.movimentos().get(0).saldoAcumulado()).isEqualByComparingTo("200.125000");
        assertThat(eur.movimentos().get(1).saldoAcumulado()).isEqualByComparingTo("160.100000");
        assertThat(eur.totalPeriodo().debito()).isEqualByComparingTo("100.125000");
        assertThat(eur.totalPeriodo().credito()).isEqualByComparingTo("40.025000");
        assertThat(eur.totalPeriodo().saldo()).isEqualByComparingTo("60.100000");
        assertThat(eur.totalFinal().saldo()).isEqualByComparingTo("160.100000");
    }

    @Test
    void mantemOrdenacaoEstavelEntreOrigensNaMesmaData() {
        OffsetDateTime momento = OffsetDateTime.parse("2026-06-15T10:00:00Z");
        ExtratoMovimentoProjection fatura = movimento(20L, momento, "FT", 1, "20");
        ExtratoMovimentoProjection recibo = movimento(10L, momento, "RC", 2, "5");
        when(documentoComercialRepository.findExtratoMovimentos(CLIENTE_ID, INICIAL, FINAL))
                .thenReturn(List.of(fatura));
        when(documentoFinanceiroRepository.findExtratoMovimentos(CLIENTE_ID, INICIAL, FINAL))
                .thenReturn(List.of(recibo));

        ExtratoClienteMoedaDto eur = service.getExtrato(CLIENTE_ID, INICIAL, FINAL).moedas().getFirst();

        assertThat(eur.movimentos()).extracting(movimento -> movimento.origem())
                .containsExactly("COMERCIAL", "FINANCEIRO");
        assertThat(eur.movimentos().getLast().saldoAcumulado()).isEqualByComparingTo("15.000000");
    }

    @Test
    void documentoComercialDeLiquidacaoImediataGeraRecebimentoNoExtrato() {
        ExtratoMovimentoProjection frc = movimento(30L, "2026-06-12T11:00:00Z", "FRC", 1, "123.45", "EUR", true);
        when(documentoComercialRepository.findExtratoMovimentos(CLIENTE_ID, INICIAL, FINAL))
                .thenReturn(List.of(frc));

        ExtratoClienteMoedaDto eur = service.getExtrato(CLIENTE_ID, INICIAL, FINAL).moedas().getFirst();

        assertThat(eur.movimentos()).hasSize(2);
        assertThat(eur.movimentos().get(0).descricao()).isEqualTo("Recibo");
        assertThat(eur.movimentos().get(0).debito()).isEqualByComparingTo("123.450000");
        assertThat(eur.movimentos().get(0).credito()).isEqualByComparingTo("0.000000");
        assertThat(eur.movimentos().get(0).saldoAcumulado()).isEqualByComparingTo("123.450000");
        assertThat(eur.movimentos().get(1).descricao()).isEqualTo("Recebimento imediato - Recibo");
        assertThat(eur.movimentos().get(1).debito()).isEqualByComparingTo("0.000000");
        assertThat(eur.movimentos().get(1).credito()).isEqualByComparingTo("123.450000");
        assertThat(eur.movimentos().get(1).saldoAcumulado()).isEqualByComparingTo("0.000000");
        assertThat(eur.totalPeriodo().debito()).isEqualByComparingTo("123.450000");
        assertThat(eur.totalPeriodo().credito()).isEqualByComparingTo("123.450000");
        assertThat(eur.totalFinal().saldo()).isEqualByComparingTo("0.000000");
    }

    @Test
    void apresentaAnteriorQuandoPeriodoNaoTemMovimentos() {
        ExtratoAnteriorProjection anterior = anterior("EUR", "75", "25");
        when(documentoComercialRepository.findExtratoAnterior(CLIENTE_ID, INICIAL))
                .thenReturn(List.of(anterior));

        ExtratoClienteMoedaDto eur = service.getExtrato(CLIENTE_ID, INICIAL, FINAL).moedas().getFirst();

        assertThat(eur.movimentos()).isEmpty();
        assertThat(eur.totalPeriodo().saldo()).isEqualByComparingTo("0.000000");
        assertThat(eur.totalFinal()).isEqualTo(eur.anterior());
    }

    @Test
    void apresentaMoedaBaseComTotaisZeroParaClienteSemMovimentos() {
        ExtratoClienteDto extrato = service.getExtrato(CLIENTE_ID, INICIAL, FINAL);

        assertThat(extrato.moedas()).hasSize(1);
        assertThat(extrato.moedas().getFirst().moedaId()).isEqualTo("EUR");
        assertThat(extrato.moedas().getFirst().totalFinal().saldo()).isEqualByComparingTo("0.000000");
    }

    @Test
    void separaTotaisPorMoeda() {
        ExtratoAnteriorProjection anteriorUsd = anterior("USD", "50", "0");
        ExtratoMovimentoProjection faturaEur = movimento(1L, "2026-06-10T10:00:00Z", "FT", 1, "20", "EUR");
        when(documentoComercialRepository.findExtratoAnterior(CLIENTE_ID, INICIAL))
                .thenReturn(List.of(anteriorUsd));
        when(documentoComercialRepository.findExtratoMovimentos(CLIENTE_ID, INICIAL, FINAL))
                .thenReturn(List.of(faturaEur));

        ExtratoClienteDto extrato = service.getExtrato(CLIENTE_ID, INICIAL, FINAL);

        assertThat(extrato.moedas()).extracting(ExtratoClienteMoedaDto::moedaId)
                .containsExactly("EUR", "USD");
        assertThat(extrato.moedas().get(0).totalFinal().saldo()).isEqualByComparingTo("20.000000");
        assertThat(extrato.moedas().get(1).totalFinal().saldo()).isEqualByComparingTo("50.000000");
    }

    @Test
    void rejeitaPeriodoInvertido() {
        assertThatThrownBy(() -> service.getExtrato(CLIENTE_ID, FINAL, INICIAL))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data inicial nao pode ser posterior a data final");
    }

    private ExtratoAnteriorProjection anterior(String moedaId, String debito, String credito) {
        ExtratoAnteriorProjection projection = mock(ExtratoAnteriorProjection.class);
        when(projection.getMoedaId()).thenReturn(moedaId);
        when(projection.getDebito()).thenReturn(new BigDecimal(debito));
        when(projection.getCredito()).thenReturn(new BigDecimal(credito));
        return projection;
    }

    private ExtratoMovimentoProjection movimento(
            Long id,
            String momento,
            String tipo,
            int sinal,
            String valor
    ) {
        return movimento(id, OffsetDateTime.parse(momento), tipo, sinal, valor, "EUR");
    }

    private ExtratoMovimentoProjection movimento(
            Long id,
            OffsetDateTime momento,
            String tipo,
            int sinal,
            String valor
    ) {
        return movimento(id, momento, tipo, sinal, valor, "EUR");
    }

    private ExtratoMovimentoProjection movimento(
            Long id,
            String momento,
            String tipo,
            int sinal,
            String valor,
            String moedaId
    ) {
        return movimento(id, OffsetDateTime.parse(momento), tipo, sinal, valor, moedaId);
    }

    private ExtratoMovimentoProjection movimento(
            Long id,
            String momento,
            String tipo,
            int sinal,
            String valor,
            String moedaId,
            boolean liquidacaoImediata
    ) {
        ExtratoMovimentoProjection projection = movimento(id, OffsetDateTime.parse(momento), tipo, sinal, valor, moedaId);
        when(projection.getLiquidacaoImediata()).thenReturn(liquidacaoImediata);
        return projection;
    }

    private ExtratoMovimentoProjection movimento(
            Long id,
            OffsetDateTime momento,
            String tipo,
            int sinal,
            String valor,
            String moedaId
    ) {
        ExtratoMovimentoProjection projection = mock(ExtratoMovimentoProjection.class);
        when(projection.getId()).thenReturn(id);
        when(projection.getData()).thenReturn(LocalDate.of(2026, 6, 15));
        when(projection.getMomento()).thenReturn(momento);
        when(projection.getTipoDocumentoId()).thenReturn(tipo);
        when(projection.getSerie()).thenReturn("2026");
        when(projection.getNumeroDocumento()).thenReturn(id);
        when(projection.getDescricao()).thenReturn(tipo.equals("FT") ? "Fatura" : "Recibo");
        when(projection.getMoedaId()).thenReturn(moedaId);
        when(projection.getSinalContabilistico()).thenReturn(sinal);
        when(projection.getLiquidacaoImediata()).thenReturn(false);
        when(projection.getValor()).thenReturn(new BigDecimal(valor));
        return projection;
    }
}
