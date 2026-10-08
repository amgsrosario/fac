package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.TaxaIvaTerritorialRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MotorFiscalServiceTests {
    private final TaxaIvaTerritorialRepository repository = mock(TaxaIvaTerritorialRepository.class);
    private final MotorFiscalService motor = new MotorFiscalService(repository);
    private final LocalDate date = LocalDate.of(2026, 1, 2);
    private final Artigo artigo = new Artigo("TESTEFISCAL");
    { artigo.setTipoArtigo(TipoArtigo.ARTIGO); }
    private final TipoTaxaIva normal = new TipoTaxaIva("NORMAL", "Normal", false);
    private final TipoTaxaIva isenta = new TipoTaxaIva("ISENTA", "Isenta", false);

    @Test
    void naoLiquidacaoPreservaNaturezaTaxaEImpostoCalculado() {
        RIva tributado = regime(normal, "6");
        var comIva = calcular(tributado, normal);
        assertThat(comIva.valores().baseTributavel()).isEqualByComparingTo("50.00");
        assertThat(comIva.valores().taxaAplicavel()).isEqualByComparingTo("6");
        assertThat(comIva.valores().ivaCalculado()).isEqualByComparingTo("3.00");
        assertThat(comIva.valores().ivaLiquidado()).isEqualByComparingTo("3.00");
        assertThat(comIva.valores().totalLinha()).isEqualByComparingTo("53.00");
        assertThat(comIva.projecao()).isEqualTo(ProjecaoFiscalQr.TRIBUTADA);

        RIva semLiquidacao = regime(normal, "6");
        semLiquidacao.setTratamentoLiquidacao(TratamentoLiquidacao.NAO_LIQUIDAR);
        semLiquidacao.setMercado(MercadoFiscal.INTRACOMUNITARIO);
        semLiquidacao.setMIsencao(motivo("M16"));
        semLiquidacao.setFundamentoFiscal("Isento artigo 14.º do RITI");
        var semIva = calcular(semLiquidacao, normal);
        assertThat(semIva.valores().taxaAplicavel()).isEqualByComparingTo("6");
        assertThat(semIva.valores().ivaCalculado()).isEqualByComparingTo("3.00");
        assertThat(semIva.valores().ivaLiquidado()).isEqualByComparingTo("0.00");
        assertThat(semIva.valores().totalLinha()).isEqualByComparingTo("50.00");
        assertThat(semIva.valores().mIsencaoCodigo()).isEqualTo("M16");
        assertThat(semIva.projecao()).isEqualTo(ProjecaoFiscalQr.ISENTA);
        assertThat(normal.getId()).isEqualTo("NORMAL");
    }

    @Test
    void isencaoInerentePreservaFundamentoDoArtigo() {
        artigo.setMIsencao(motivo("M07"));
        artigo.setFundamentoFiscal("Isento artigo 9.º do CIVA");
        var r = calcular(regime(isenta, "0"), isenta);
        assertThat(r.valores().mIsencaoCodigo()).isEqualTo("M07");
        assertThat(r.valores().fundamentoFiscal()).isEqualTo("Isento artigo 9.º do CIVA");
        assertThat(r.valores().ivaCalculado()).isEqualByComparingTo("0.00");
        assertThat(r.valores().ivaLiquidado()).isEqualByComparingTo("0.00");
        assertThat(r.valores().totalLinha()).isEqualByComparingTo("50.00");
    }

    @Test
    void rejeitaIsencaoInerenteComNaoLiquidacaoSemRegraNormativa() {
        artigo.setMIsencao(motivo("M07"));
        artigo.setFundamentoFiscal("Isento artigo 9.º do CIVA");
        RIva riva = regime(isenta, "0");
        riva.setTratamentoLiquidacao(TratamentoLiquidacao.NAO_LIQUIDAR);
        assertThatThrownBy(() -> calcular(riva, isenta)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejeitaTaxaNegativaZeroTributadoEMatrizAusente() {
        assertThatThrownBy(() -> calcular(regime(normal, "-1"), normal)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> calcular(regime(normal, "0"), normal)).isInstanceOf(BadRequestException.class);
        when(repository.aplicaveis("PT", TerritorioFiscal.CONTINENTE, "NORMAL", date)).thenReturn(List.of());
        assertThatThrownBy(() -> calcular(new RIva("SEM", "Sem taxas"), normal))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("Regime sem taxa aplicável");
    }

    @Test
    void exigeFundamentoEEnquadramentoCompativelComMotivo() {
        RIva riva = regime(normal, "23");
        riva.setTratamentoLiquidacao(TratamentoLiquidacao.NAO_LIQUIDAR);
        riva.setMIsencao(motivo("M16"));
        riva.setMercado(MercadoFiscal.NACIONAL);
        assertThatThrownBy(() -> calcular(riva, normal)).isInstanceOf(BadRequestException.class);
        riva.setMercado(MercadoFiscal.INTRACOMUNITARIO);
        assertThatThrownBy(() -> calcular(riva, normal)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Fundamento fiscal obrigatório");
        riva.setMIsencao(motivo("M99"));
        riva.setFundamentoFiscal("Não habilitado");
        assertThatThrownBy(() -> calcular(riva, normal)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejeitaM16ParaServicosSemInferirRegraDeLocalizacao() {
        RIva riva = regime(normal, "6");
        riva.setTratamentoLiquidacao(TratamentoLiquidacao.NAO_LIQUIDAR);
        riva.setMercado(MercadoFiscal.INTRACOMUNITARIO);
        riva.setMIsencao(motivo("M16"));
        riva.setFundamentoFiscal("Isento artigo 14.º do RITI");
        artigo.setTipoArtigo(TipoArtigo.SERVICO);
        assertThatThrownBy(() -> calcular(riva, normal)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("transmissão de bens");
    }

    private RIva regime(TipoTaxaIva categoria, String taxa) {
        RIva riva = new RIva("TST", "Regime teste");
        riva.substituirTaxas(List.of(new RIvaTaxa(riva, categoria, new BigDecimal(taxa))));
        return riva;
    }

    private MIsencao motivo(String codigo) {
        MIsencao motivo = new MIsencao();
        motivo.setId(codigo);
        return motivo;
    }

    private MotorFiscalService.Resultado calcular(RIva riva, TipoTaxaIva categoria) {
        return motor.calcular(riva, artigo, categoria, date, new BigDecimal("1"), new BigDecimal("50"),
                TipoDescontoLinha.VALOR, BigDecimal.ZERO);
    }
}
