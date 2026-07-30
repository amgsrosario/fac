package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.LinhaDocumentoComercialMapper;
import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.LinhaDocumentoComercial;
import com.ar2lda.fac.model.TipoDescontoLinha;
import com.ar2lda.fac.model.TipoLinhaDocumento;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        LinhaDocumentoComercial linha = new LinhaDocumentoComercial();
        linha.setDocumentoComercial(documento);
        linha.setNumeroLinha(linhaRepository.findMaxNumeroLinha(documentoId) + 1);
        TipoLinhaDocumento tipoLinha = dto.tipoLinha() != null ? dto.tipoLinha() : TipoLinhaDocumento.COMERCIAL;
        linha.setTipoLinha(tipoLinha);
        if (tipoLinha == TipoLinhaDocumento.TEXTO) {
            applyTextoValues(linha, dto.descricao());
        } else {
            applyComercialValues(linha, documento, dto.artigoId(), dto.descricao(), dto.quantidade(), dto.precoUnitario(),
                    dto.tipoDesconto(), dto.desconto(), dto.tipoTaxaIvaId(), dto.peso());
        }

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
    public List<LinhaDocumentoComercialDto> reordenarLinhas(Long documentoId, List<Long> linhaIds) {
        DocumentoComercial documento = findDocumento(documentoId);
        validateRascunho(documento);
        List<LinhaDocumentoComercial> linhasOrdenadas = reordenarLinhasPersistidas(documentoId, linhaIds);
        return linhasOrdenadas.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public LinhaDocumentoComercialDto update(Long documentoId, Long linhaId, LinhaDocumentoComercialUpdateDto dto) {
        DocumentoComercial documento = findDocumento(documentoId);
        validateRascunho(documento);
        LinhaDocumentoComercial linha = findLinha(documentoId, linhaId);
        TipoLinhaDocumento tipoLinha = dto.tipoLinha() != null ? dto.tipoLinha() : linha.getTipoLinha();
        linha.setTipoLinha(tipoLinha);
        if (tipoLinha == TipoLinhaDocumento.TEXTO) {
            applyTextoValues(linha, dto.descricao());
        } else {
            applyComercialValues(linha, documento, dto.artigoId(), dto.descricao(), dto.quantidade(), dto.precoUnitario(),
                    dto.tipoDesconto(), dto.desconto(), dto.tipoTaxaIvaId(), dto.peso());
        }
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
        linhaRepository.flush();
        compactarOrdem(documentoId);
        recalcularTotais(documento);
    }

    private List<LinhaDocumentoComercial> reordenarLinhasPersistidas(Long documentoId, List<Long> linhaIds) {
        List<LinhaDocumentoComercial> linhasAtuais = linhaRepository.findByDocumentoComercialIdOrderByNumeroLinha(documentoId);
        if (linhaIds == null) {
            throw new BadRequestException("Lista de linhas e obrigatoria");
        }
        if (!linhasAtuais.isEmpty() && linhaIds.isEmpty()) {
            throw new BadRequestException("Lista de linhas nao pode ser vazia quando o documento tem linhas");
        }
        if (linhasAtuais.isEmpty()) {
            if (!linhaIds.isEmpty()) {
                throw new BadRequestException("Lista de linhas nao corresponde ao documento");
            }
            return List.of();
        }

        Set<Long> idsRecebidos = new HashSet<>();
        for (Long id : linhaIds) {
            if (id == null) {
                throw new BadRequestException("Lista de linhas contem identificador nulo");
            }
            if (!idsRecebidos.add(id)) {
                throw new BadRequestException("Lista de linhas contem identificadores duplicados");
            }
        }

        Map<Long, LinhaDocumentoComercial> linhasPorId = linhasAtuais.stream()
                .collect(Collectors.toMap(LinhaDocumentoComercial::getId, Function.identity()));
        if (idsRecebidos.size() != linhasAtuais.size() || !linhasPorId.keySet().equals(idsRecebidos)) {
            throw new BadRequestException("Lista de linhas deve representar exatamente as linhas atuais do documento");
        }

        List<LinhaDocumentoComercial> novaOrdem = new ArrayList<>();
        for (Long id : linhaIds) {
            novaOrdem.add(linhasPorId.get(id));
        }
        aplicarOrdemSegura(novaOrdem);
        return novaOrdem;
    }

    private void compactarOrdem(Long documentoId) {
        aplicarOrdemSegura(linhaRepository.findByDocumentoComercialIdOrderByNumeroLinha(documentoId));
    }

    private void aplicarOrdemSegura(List<LinhaDocumentoComercial> linhasOrdenadas) {
        for (int index = 0; index < linhasOrdenadas.size(); index++) {
            linhasOrdenadas.get(index).setNumeroLinha(-1_000_000 - index);
        }
        linhaRepository.flush();

        for (int index = 0; index < linhasOrdenadas.size(); index++) {
            linhasOrdenadas.get(index).setNumeroLinha(index + 1);
        }
        linhaRepository.flush();
    }

    private void applyTextoValues(LinhaDocumentoComercial linha, String descricao) {
        linha.setDescricao(descricao == null || descricao.isBlank() ? "" : descricao);
        linha.setArtigo(null);
        linha.setQuantidade(null);
        linha.setPrecoUnitario(null);
        linha.setValorBruto(null);
        linha.setTipoDesconto(null);
        linha.setDesconto(null);
        linha.setValorDesconto(null);
        linha.setValorLinha(null);
        linha.setTipoTaxaIva(null);
        linha.setPercentagemIva(null);
        linha.setPeso(null);
    }

    private void applyComercialValues(LinhaDocumentoComercial linha, DocumentoComercial documento, String artigoId, String descricao,
                                      BigDecimal quantidade, BigDecimal precoUnitario, TipoDescontoLinha tipoDesconto,
                                      BigDecimal desconto, String tipoTaxaIvaId, BigDecimal peso) {
        Artigo artigo = findArtigo(requireText(artigoId, "Artigo e obrigatorio"));
        TipoTaxaIva tipoTaxaIva = findTipoTaxaIvaOrDefault(tipoTaxaIvaId, artigo.getIvaVenda());
        BigDecimal quantidade6 = scale6(requireValue(quantidade, "Quantidade e obrigatoria"));
        BigDecimal preco6 = scale6(requireValue(precoUnitario, "Preco unitario e obrigatorio"));
        TipoDescontoLinha tipo = tipoDesconto != null ? tipoDesconto : TipoDescontoLinha.VALOR;
        BigDecimal desconto6 = desconto != null ? scale6(desconto) : ZERO;
        BigDecimal valorBruto = quantidade6.multiply(preco6).setScale(6, RoundingMode.HALF_UP);
        BigDecimal valorDesconto = calcularValorDesconto(valorBruto, tipo, desconto6);

        if (valorDesconto.compareTo(valorBruto) > 0) {
            throw new BadRequestException("Valor do desconto não pode ser superior ao valor bruto da linha");
        }

        linha.setTipoLinha(TipoLinhaDocumento.COMERCIAL);
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

    public void recalcularTotais(DocumentoComercial documento) {
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
            if (linha.getTipoLinha() == TipoLinhaDocumento.TEXTO) {
                continue;
            }
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
        documento.setPeso(linhas.stream().noneMatch(linha -> linha.getTipoLinha() == TipoLinhaDocumento.COMERCIAL)
                ? null
                : peso.setScale(3, RoundingMode.HALF_UP));
        documentoRepository.save(documento);
    }

    public void consolidarSnapshotsFiscais(DocumentoComercial documento) {
        List<LinhaDocumentoComercial> linhas = linhaRepository
                .findByDocumentoComercialIdOrderByNumeroLinha(documento.getId());
        for (LinhaDocumentoComercial linha : linhas) {
            if (linha.getTipoLinha() == TipoLinhaDocumento.TEXTO) {
                continue;
            }
            BigDecimal base = linha.getValorLinha().setScale(6, RoundingMode.HALF_UP);
            BigDecimal imposto = base.multiply(linha.getPercentagemIva())
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            linha.consolidarSnapshotFiscal(base, imposto, base.add(imposto).setScale(6, RoundingMode.HALF_UP));
        }
        linhaRepository.saveAll(linhas);
    }

    private DocumentoComercial findDocumento(Long id) {
        return documentoRepository.findByIdForUpdate(id)
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

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value;
    }

    private BigDecimal requireValue(BigDecimal value, String message) {
        if (value == null) {
            throw new BadRequestException(message);
        }
        return value;
    }

    private void validateRascunho(DocumentoComercial documento) {
        if (documento.isAnulado()) {
            throw new ConflictException("Documento comercial anulado nao pode ter linhas alteradas");
        }
        if (documento.getEstado() != EstadoDocumentoComercial.RASCUNHO) {
            throw new ConflictException("Documento emitido não pode ter linhas alteradas");
        }
    }
}
