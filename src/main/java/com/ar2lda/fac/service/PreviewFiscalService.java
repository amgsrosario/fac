package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.PreviewFiscalDto;
import com.ar2lda.fac.controller.dto.PreviewFiscalRequest;
import com.ar2lda.fac.controller.dto.ResultadoFiscalLinhaDto;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.TipoLinhaDocumento;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.repository.ArtigoRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PreviewFiscalService {

    private final MotorFiscalService motor;
    private final RIvaRepository regimes;
    private final ArtigoRepository artigos;
    private final TipoTaxaIvaRepository categorias;

    @Transactional(readOnly = true)
    public PreviewFiscalDto preview(PreviewFiscalRequest request) {
        RIva r = regimes.findById(request.rivaId())
                .orElseThrow(() -> new NotFoundException("Regime não encontrado"));
        var linhas = new ArrayList<ResultadoFiscalLinhaDto>();
        BigDecimal bruto = new BigDecimal("0.00"), desconto = bruto, iva = bruto, total = bruto;
        for (var l : request.linhas()) {
            if (l.tipoLinha() == TipoLinhaDocumento.TEXTO) {
                linhas.add(null);
                continue;
            }
            Artigo a = artigos.findById(l.artigoId())
                    .orElseThrow(() -> new NotFoundException("Artigo não encontrado"));
            TipoTaxaIva c = l.tipoTaxaIvaId() == null ? a.getIvaVenda()
                    : categorias.findById(l.tipoTaxaIvaId())
                            .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
            var v = motor.calcular(r, a, c, request.dataEmissao(), l.quantidade(), l.precoUnitario(),
                    l.tipoDesconto(), l.desconto()).valores();
            linhas.add(v);
            bruto = bruto.add(v.valorBruto());
            desconto = desconto.add(v.valorDesconto());
            iva = iva.add(v.ivaLiquidado());
            total = total.add(v.totalLinha());
        }
        return new PreviewFiscalDto(linhas, new PreviewFiscalDto.Totais(bruto, desconto, iva, total));
    }
}
