package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ArtigoCreateDto;
import com.ar2lda.fac.controller.dto.ArtigoDto;
import com.ar2lda.fac.controller.dto.ArtigoUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.ArtigoMapper;
import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.Familia;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.repository.ArtigoRepository;
import com.ar2lda.fac.repository.FamiliaRepository;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtigoService {

    private final ArtigoRepository artigoRepository;
    private final FamiliaRepository familiaRepository;
    private final TipoTaxaIvaRepository tipoTaxaIvaRepository;
    private final ArtigoMapper mapper;

    @Transactional
    public ArtigoDto create(ArtigoCreateDto dto) {
        if (artigoRepository.existsById(dto.codigo())) {
            throw new ConflictException("Artigo já existe: " + dto.codigo());
        }
        String codigoIdentificacao = normalizeCodigoIdentificacao(dto.codigoIdentificacao());
        validateCodigoIdentificacaoCreate(codigoIdentificacao);

        Artigo artigo = mapper.fromCreateDTO(dto);
        artigo.setCodigoIdentificacao(codigoIdentificacao);
        applyRelations(dto.familiaId(), dto.ivaCompraId(), dto.ivaVendaId(), artigo, true);
        return mapper.toDTO(artigoRepository.save(artigo));
    }

    public Page<ArtigoDto> list(Pageable pageable) {
        return artigoRepository.findAll(pageable).map(mapper::toDTO);
    }

    public ArtigoDto getByCodigo(String codigo) {
        return mapper.toDTO(findEntityByCodigo(codigo));
    }

    @Transactional
    public void update(String codigo, ArtigoUpdateDto dto) {
        Artigo artigo = findEntityByCodigo(codigo);
        String codigoIdentificacao = normalizeCodigoIdentificacao(dto.codigoIdentificacao());
        validateCodigoIdentificacaoUpdate(codigoIdentificacao, artigo.getCodigo());

        mapper.applyUpdate(dto, artigo);
        artigo.setCodigoIdentificacao(codigoIdentificacao);
        applyRelations(dto.familiaId(), dto.ivaCompraId(), dto.ivaVendaId(), artigo, false);
        artigoRepository.save(artigo);
    }

    @Transactional
    public void delete(String codigo) {
        artigoRepository.delete(findEntityByCodigo(codigo));
        artigoRepository.flush();
    }

    private Artigo findEntityByCodigo(String codigo) {
        return artigoRepository.findById(codigo)
                .orElseThrow(() -> new NotFoundException("Artigo não encontrado: " + codigo));
    }

    private void applyRelations(Long familiaId, String ivaCompraId, String ivaVendaId, Artigo artigo, boolean novo) {
        artigo.setFamilia(findFamilia(familiaId));
        artigo.setIvaCompra(findTipoTaxaIva(ivaCompraId, artigo.getIvaCompra(), novo));
        artigo.setIvaVenda(findTipoTaxaIva(ivaVendaId, artigo.getIvaVenda(), novo));
    }

    private Familia findFamilia(Long id) {
        return familiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Família não encontrada: " + id));
    }

    private TipoTaxaIva findTipoTaxaIva(String id, TipoTaxaIva atual, boolean novo) {
        TipoTaxaIva tipo = tipoTaxaIvaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de taxa de IVA não encontrado: " + id));
        boolean mantemTipoAtual = atual != null && atual.getId().equals(tipo.getId());
        if (tipo.isInativo() && (novo || !mantemTipoAtual)) {
            throw new BadRequestException("Tipo de taxa de IVA está inativo: " + id);
        }
        return tipo;
    }

    private void validateCodigoIdentificacaoCreate(String codigoIdentificacao) {
        if (codigoIdentificacao != null && artigoRepository.existsByCodigoIdentificacao(codigoIdentificacao)) {
            throw new ConflictException("Código de identificação já está associado a outro artigo: " + codigoIdentificacao);
        }
    }

    private void validateCodigoIdentificacaoUpdate(String codigoIdentificacao, String codigo) {
        if (codigoIdentificacao != null
                && artigoRepository.existsByCodigoIdentificacaoAndCodigoNot(codigoIdentificacao, codigo)) {
            throw new ConflictException("Código de identificação já está associado a outro artigo: " + codigoIdentificacao);
        }
    }

    private String normalizeCodigoIdentificacao(String codigoIdentificacao) {
        if (codigoIdentificacao == null || codigoIdentificacao.isBlank()) {
            return null;
        }
        return codigoIdentificacao.trim();
    }
}
