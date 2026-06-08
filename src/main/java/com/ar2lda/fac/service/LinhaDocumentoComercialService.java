package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.LinhaDocumentoComercialMapper;
import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.LinhaDocumentoComercial;
import com.ar2lda.fac.model.TipoDescontoLinha;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.repository.ArtigoRepository;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.LinhaDocumentoComercialRepository;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LinhaDocumentoComercialService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

    private final LinhaDocumentoComercialRepository linhaRepository;
    private final DocumentoComercialRepository documentoRepository;
    private final ArtigoRepository artigoRepository;
    private final TipoTaxaIvaRepository tipoTaxaIvaRepository;
    private final LinhaDocumentoComercialMapper mapper;

    @Transactional
    public LinhaDocumentoComercialDto create(Long documentoId, LinhaDocumentoComercialCreateDto dto) {
        DocumentoComercial documento = findDocumento(documentoId);
        validateRascunho(documento);
        Artigo artigo = findArtigo(dto.artigoId());
        TipoTaxaIva tipoTaxaIva = findTipoTaxaIvaOrDefault(dto.tipoTaxaIvaId(), artigo.getIvaVenda());

        LinhaDocumentoComercial linha = new LinhaDocumentoComercial();
        linha.setDocumentoComercial(documento);
        linha.setNumeroLinha(linhaRepository.findMaxNumeroLinha(documentoId) + 1);
        applyValues(linha, documento, artigo, tipoTaxaIva, dto.descricao(), dto.quantidade(), dto.precoUnitario(),
                dto.tipoDesconto(), dto.desconto(), dto.peso());

        LinhaDocumentoComercial saved = linhaRepository.save(linha);
        recalcularTotais(documento);
        return mapper.toDTO(saved);
    }

    public List<LinhaDocumentoComercialDto> list(Long documentoId) {
        return linhaRepository.findByDocumentoComercialIdOrderByNumeroLinha(documentoId).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public LinhaDocumentoComercialDto getById(Long documentoId, Long linhaId) {
        return mapper.toDTO(findLinha(documentoId, linhaId));
    }

    @Transactional
    public LinhaDocumentoComercialDto update(Long documentoId, Long linhaId, LinhaDocumentoComercialUpdateDto dto) {
        DocumentoComercial documento = findDocumento(documentoId);
        validateRascunho(documento);
        LinhaDocumentoComercial linha = findLinha(documentoId, linhaId);
        Artigo artigo = findArtigo(dto.artigoId());
        TipoTaxaIva tipoTaxaIva = findTipoTaxaIvaOrDefault(dto.tipoTaxaIvaId(), artigo.getIvaVenda());
        applyValues(linha, documento, artigo, tipoTaxaIva, dto.descricao(), dto.quantidade(), dto.precoUnitario(),
                dto.tipoDesconto(), dto.desconto(), dto.peso());
        LinhaDocumentoComercial saved = linhaRepository.save(linha);
        recalcularTotais(documento);
        return mapper.toDTO(saved);
    }

    @Transactional
    public void delete(Long documentoId, Long linhaId) {
        DocumentoComercial documento = findDocumento(documentoId);
        validateRascunho(documento);
        LinhaDocumentoComercial linha = findLinha(documentoId, linhaId);
        linhaRepository.delete(linha);
        recalcularTotais(documento);
    }

    private void applyValues(LinhaDocumentoComercial linha, DocumentoComercial documento, Artigo artigo, TipoTaxaIva tipoTaxaIva,
                             String descricao, BigDecimal quantidade, BigDecimal precoUnitario, TipoDescontoLinha tipoDesconto,
                             BigDecimal desconto, BigDecimal peso) {
        BigDecimal quantidade6 = scale6(quantidade);
        BigDecimal preco6 = scale6(precoUnitario);
        TipoDescontoLinha tipo = tipoDesconto != null ? tipoDesconto : TipoDescontoLinha.VALOR;
        BigDecimal desconto6 = desconto != null ? scale6(desconto) : ZERO;
        BigDecimal valorBruto = quantidade6.multiply(preco6).setScale(6, RoundingMode.HALF_UP);
        BigDecimal valorDesconto = calcularValorDesconto(valorBruto, tipo, desconto6);

        if (valorDesconto.compareTo(valorBruto) > 0) {
            throw new BadRequestException("Valor do desconto não pode ser superior ao valor bruto da linha");
        }

        linha.setArtigo(artigo);
        linha.setDescricao(descricao == null || descricao.isBlank() ? artigo.getDescricao() : descricao);
        linha.setQuantidade(quantidade6);
        linha.setPrecoUnitario(preco6);
        linha.setValorBruto(valorBruto);
        linha.setTipoDesconto(tipo);
        linha.setDesconto(desconto6);
        linha.setValorDesconto(valorDesconto);
        linha.setValorLinha(valorBruto.subtract(valorDesconto).setScale(6, RoundingMode.HALF_UP));
        linha.setTipoTaxaIva(tipoTaxaIva);
        linha.setPercentagemIva(findPercentagemIva(documento, tipoTaxaIva));
        linha.setPeso(peso != null ? peso.setScale(3, RoundingMode.HALF_UP) : calcularPeso(artigo, quantidade6));
    }

    private BigDecimal calcularValorDesconto(BigDecimal valorBruto, TipoDescontoLinha tipo, BigDecimal desconto) {
        if (tipo == TipoDescontoLinha.PERCENTAGEM) {
            if (desconto.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Desconto percentual não pode ser superior a 100");
            }
            return valorBruto.multiply(desconto)
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        }
        return desconto;
    }

    private BigDecimal findPercentagemIva(DocumentoComercial documento, TipoTaxaIva tipoTaxaIva) {
        BigDecimal taxa = documento.getRiva().getTaxa(tipoTaxaIva.getId());
        if (taxa == null) {
            throw new BadRequestException("Regime de IVA do documento não tem taxa para " + tipoTaxaIva.getId());
        }
        return taxa;
    }

    private BigDecimal calcularPeso(Artigo artigo, BigDecimal quantidade) {
        if (artigo.getPeso() == null) {
            return null;
        }
        return artigo.getPeso().multiply(quantidade).setScale(3, RoundingMode.HALF_UP);
    }

    private void recalcularTotais(DocumentoComercial documento) {
        List<LinhaDocumentoComercial> linhas = linhaRepository.findByDocumentoComercialIdOrderByNumeroLinha(documento.getId());

        BigDecimal valorBruto = ZERO;
        BigDecimal valorDesconto = ZERO;
        BigDecimal valorIsento = ZERO;
        BigDecimal valorSujeitoReduzida = ZERO;
        BigDecimal valorSujeitoIntermedia = ZERO;
        BigDecimal valorSujeitoNormal = ZERO;
        BigDecimal valorIvaReduzida = ZERO;
        BigDecimal valorIvaIntermedia = ZERO;
        BigDecimal valorIvaNormal = ZERO;
        BigDecimal peso = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

        for (LinhaDocumentoComercial linha : linhas) {
            valorBruto = valorBruto.add(linha.getValorBruto());
            valorDesconto = valorDesconto.add(linha.getValorDesconto());
            if (linha.getPeso() != null) {
                peso = peso.add(linha.getPeso());
            }

            String tipoTaxa = linha.getTipoTaxaIva().getId();
            BigDecimal valorLinha = linha.getValorLinha();
            BigDecimal valorIva = valorLinha.multiply(linha.getPercentagemIva())
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

            switch (tipoTaxa) {
                case "ISENTA" -> valorIsento = valorIsento.add(valorLinha);
                case "REDUZIDA" -> {
                    valorSujeitoReduzida = valorSujeitoReduzida.add(valorLinha);
                    valorIvaReduzida = valorIvaReduzida.add(valorIva);
                }
                case "INTERMEDIA" -> {
                    valorSujeitoIntermedia = valorSujeitoIntermedia.add(valorLinha);
                    valorIvaIntermedia = valorIvaIntermedia.add(valorIva);
                }
                case "NORMAL" -> {
                    valorSujeitoNormal = valorSujeitoNormal.add(valorLinha);
                    valorIvaNormal = valorIvaNormal.add(valorIva);
                }
                default -> throw new BadRequestException("Tipo de taxa IVA ainda não suportado nos totais fixos: " + tipoTaxa);
            }
        }

        documento.setValorBruto(valorBruto.setScale(6, RoundingMode.HALF_UP));
        documento.setValorDesconto(valorDesconto.setScale(6, RoundingMode.HALF_UP));
        documento.setValorIsento(valorIsento.setScale(6, RoundingMode.HALF_UP));
        documento.setValorSujeitoReduzida(valorSujeitoReduzida.setScale(6, RoundingMode.HALF_UP));
        documento.setValorSujeitoIntermedia(valorSujeitoIntermedia.setScale(6, RoundingMode.HALF_UP));
        documento.setValorSujeitoNormal(valorSujeitoNormal.setScale(6, RoundingMode.HALF_UP));
        documento.setValorIvaReduzida(valorIvaReduzida.setScale(6, RoundingMode.HALF_UP));
        documento.setValorIvaIntermedia(valorIvaIntermedia.setScale(6, RoundingMode.HALF_UP));
        documento.setValorIvaNormal(valorIvaNormal.setScale(6, RoundingMode.HALF_UP));
        BigDecimal valorIvaTotal = valorIvaReduzida.add(valorIvaIntermedia).add(valorIvaNormal);
        BigDecimal valorTotal = valorIsento
                .add(valorSujeitoReduzida)
                .add(valorSujeitoIntermedia)
                .add(valorSujeitoNormal)
                .add(valorIvaTotal)
                .subtract(documento.getValorRetencao() != null ? documento.getValorRetencao() : ZERO);
        documento.setValorIvaTotal(valorIvaTotal.setScale(6, RoundingMode.HALF_UP));
        documento.setValorTotal(valorTotal.setScale(6, RoundingMode.HALF_UP));
        documento.setPeso(linhas.isEmpty() ? null : peso.setScale(3, RoundingMode.HALF_UP));
        documentoRepository.save(documento);
    }

    private DocumentoComercial findDocumento(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Documento comercial não encontrado: " + id));
    }

    private LinhaDocumentoComercial findLinha(Long documentoId, Long linhaId) {
        LinhaDocumentoComercial linha = linhaRepository.findById(linhaId)
                .orElseThrow(() -> new NotFoundException("Linha não encontrada: " + linhaId));
        if (!linha.getDocumentoComercial().getId().equals(documentoId)) {
            throw new NotFoundException("Linha não encontrada no documento: " + linhaId);
        }
        return linha;
    }

    private Artigo findArtigo(String id) {
        return artigoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Artigo não encontrado: " + id));
    }

    private TipoTaxaIva findTipoTaxaIvaOrDefault(String id, TipoTaxaIva defaultValue) {
        if (id == null || id.isBlank()) {
            return defaultValue;
        }
        return tipoTaxaIvaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de taxa IVA não encontrado: " + id));
    }

    private BigDecimal scale6(BigDecimal value) {
        return value.setScale(6, RoundingMode.HALF_UP);
    }

    private void validateRascunho(DocumentoComercial documento) {
        if (documento.isAnulado()) {
            throw new BadRequestException("Documento comercial anulado nao pode ter linhas alteradas");
        }
        if (documento.getEstado() != EstadoDocumentoComercial.RASCUNHO) {
            throw new BadRequestException("Documento emitido não pode ter linhas alteradas");
        }
    }
}
