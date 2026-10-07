package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ResultadoFiscalLinhaDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.TaxaIvaTerritorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MotorFiscalService {

    public static final int VERSION = 1;

    private final TaxaIvaTerritorialRepository taxasRepository;

    public record Resultado(ResultadoFiscalLinhaDto valores, ProjecaoFiscalQr projecao) {}

    public Resultado calcular(RIva riva, Artigo artigo, TipoTaxaIva categoria, LocalDate data,
                              BigDecimal quantidade, BigDecimal preco, TipoDescontoLinha tipo, BigDecimal desconto) {
        if (riva == null || !"PT".equals(riva.getJurisdicao())) {
            throw new BadRequestException("Jurisdição fiscal não suportada");
        }
        if (!Set.of("ISENTA", "REDUZIDA", "INTERMEDIA", "NORMAL").contains(categoria.getId())) {
            throw new BadRequestException("Categoria fiscal não suportada");
        }
        BigDecimal taxa = riva.getTaxa(categoria.getId());
        // Existing matrices remain explicit applicable-rate overrides; they no longer encode non-liquidation.
        // Territorial defaults are separate from operation market/treatment and currency.
        if (riva.getTerritorioFiscal() != TerritorioFiscal.CONTINENTE) {
            var taxas = taxasRepository.aplicaveis(riva.getJurisdicao(), riva.getTerritorioFiscal(), categoria.getId(), data);
            if (taxas.size() != 1) {
                throw new BadRequestException("Taxa territorial ausente ou ambígua");
            }
            taxa = taxas.getFirst().getValor();
        }
        if (taxa == null) {
            throw new BadRequestException("Regime sem taxa aplicável para a categoria");
        }
        boolean inerente = "ISENTA".equals(categoria.getId());
        if (taxa.signum() < 0 || (inerente && taxa.signum() != 0) || (!inerente && taxa.signum() == 0)) {
            throw new BadRequestException("Taxa aplicável incompatível com categoria; não liquidação não transforma a taxa em zero");
        }
        MIsencao motivo = null;
        String fundamento = null;
        ProjecaoFiscalQr projecao = ProjecaoFiscalQr.TRIBUTADA;
        TratamentoLiquidacao tratamento = riva.getTratamentoLiquidacao();
        if (inerente) {
            if (tratamento == TratamentoLiquidacao.NAO_LIQUIDAR) {
                throw new BadRequestException("Combinação de isenção própria e não liquidação da operação exige validação normativa específica");
            }
            motivo = artigo.getMIsencao();
            fundamento = artigo.getFundamentoFiscal();
            if (motivo == null || !"M07".equals(motivo.getId())) {
                throw new BadRequestException("Isenção própria exige fundamento M07 validado; outras combinações não estão habilitadas");
            }
            projecao = ProjecaoFiscalQr.ISENTA;
        } else if (tratamento == TratamentoLiquidacao.NAO_LIQUIDAR) {
            motivo = riva.getMIsencao();
            fundamento = riva.getFundamentoFiscal();
            if (motivo == null || !Set.of("M10", "M16").contains(motivo.getId())) {
                throw new BadRequestException("Projecção normativa desta não liquidação ainda não habilitada");
            }
            if (("M16".equals(motivo.getId()) && riva.getMercado() != MercadoFiscal.INTRACOMUNITARIO)
                    || ("M10".equals(motivo.getId()) && riva.getMercado() != MercadoFiscal.NACIONAL)) {
                throw new BadRequestException("Motivo fiscal incompatível com enquadramento");
            }
            if ("M16".equals(motivo.getId()) && artigo.getTipoArtigo() != TipoArtigo.ARTIGO) {
                throw new BadRequestException("M16 exige transmissão de bens; regra para serviços não habilitada");
            }
            projecao = ProjecaoFiscalQr.ISENTA;
        }
        if (motivo != null && (fundamento == null || fundamento.isBlank())) {
            throw new BadRequestException("Fundamento fiscal obrigatório");
        }
        if (tratamento == TratamentoLiquidacao.NORMAL && riva.getMIsencao() != null) {
            throw new BadRequestException("Tributação normal com motivo da operação incompatível");
        }
        var v = MotorMonetario.calcular(quantidade, preco, tipo, desconto, taxa);
        BigDecimal liquidado = tratamento == TratamentoLiquidacao.NAO_LIQUIDAR
                ? new BigDecimal("0.00") : v.ivaCalculado();
        return new Resultado(new ResultadoFiscalLinhaDto(v.bruto(), v.desconto(), v.base(), taxa,
                v.ivaCalculado(), liquidado, v.base().add(liquidado), motivo == null ? null : motivo.getId(),
                fundamento, tratamento.name()), projecao);
    }

    public void aplicar(LinhaDocumentoComercial linha, DocumentoComercial documento) {
        var r = calcular(documento.getRiva(), linha.getArtigo(), linha.getTipoTaxaIva(), documento.getDataEmissao(),
                linha.getQuantidade(), linha.getPrecoUnitario(), linha.getTipoDesconto(), linha.getDesconto());
        var v = r.valores();
        linha.setValorBruto(v.valorBruto());
        linha.setValorDesconto(v.valorDesconto());
        linha.setValorLinha(v.baseTributavel());
        linha.setPercentagemIva(v.taxaAplicavel());
        linha.setIvaCalculado(v.ivaCalculado());
        linha.setIvaLiquidado(v.ivaLiquidado());
        linha.setFiscalMIsencaoCodigo(v.mIsencaoCodigo());
        linha.setFiscalFundamento(v.fundamentoFiscal());
        linha.setFiscalTratamentoLiquidacao(TratamentoLiquidacao.valueOf(v.tratamentoLiquidacao()));
        linha.setFiscalProjecaoQr(r.projecao());
        linha.consolidarSnapshotFiscal(v.baseTributavel(), v.ivaLiquidado(), v.totalLinha());
    }

    public void enquadrar(DocumentoComercial d) {
        RIva r = d.getRiva();
        d.setFiscalMotorVersion(VERSION);
        d.setFiscalMercado(r.getMercado().name());
        d.setFiscalTratamentoLiquidacao(r.getTratamentoLiquidacao().name());
        d.setFiscalFundamento(r.getFundamentoFiscal());
        d.setFiscalMIsencaoCodigo(r.getMIsencao() == null ? null : r.getMIsencao().getId());
        d.setFiscalJurisdicao(r.getJurisdicao());
        d.setFiscalTerritorioFiscal(r.getTerritorioFiscal().name());
    }
}
