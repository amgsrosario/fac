package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.TipoDocumentoCreateDto;
import com.ar2lda.fac.controller.dto.TipoDocumentoDto;
import com.ar2lda.fac.controller.dto.TipoDocumentoUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.fiscal.catalog.FiscalCatalog;
import com.ar2lda.fac.fiscal.catalog.FtDeferredSemantics;
import com.ar2lda.fac.mapper.TipoDocumentoMapper;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TipoDocumentoService {

    private final TipoDocumentoRepository repository;
    private final TipoDocumentoMapper mapper;
    private final EntityManager entityManager;
    private final FiscalCatalog fiscalCatalog;

    /** Serialize configuration edits and first emission before reading fiscal attributes or numbering. */
    @Transactional(propagation = Propagation.MANDATORY)
    public TipoDocumento lockConfiguration(String id) {
        entityManager.flush(); // Include a type created earlier in this transaction before refreshing it.
        TipoDocumento type = findEntityById(id);
        entityManager.refresh(type, LockModeType.PESSIMISTIC_WRITE);
        return type;
    }

    @Transactional
    public TipoDocumentoDto create(TipoDocumentoCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Tipo de documento já existe: " + dto.id());
        }
        TipoDocumento entity = mapper.fromCreateDTO(dto);
        if ("FT".equals(entity.getCodigoFiscal()) && dto.liquidacaoImediata() == null)
            throw new BadRequestException("A modalidade de liquidação da FT deve ser explícita");
        if (deferredFt(entity.getCodigoFiscal(), entity.isLiquidacaoImediata())) {
            var revision = creationRevision();
            validateTuple(revision, entity.getCodigoFiscal(), entity.getAreaGestao(), entity.getEntidade(),
                    entity.getSinalContabilistico(), entity.isLiquidacaoImediata());
            entity = TipoDocumento.governed(entity, revision.key().fiscalCode(), revision.key().revision());
        }
        // Refusals must be observed inside the request transaction, before returning 201.
        // Assigned IDs would make save() merge an intervening concurrent row. Creation is INSERT-only.
        entityManager.persist(entity);
        entityManager.flush();
        return mapper.toDTO(entity);
    }

    public Page<TipoDocumentoDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public TipoDocumentoDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    @Transactional
    public void update(String id, TipoDocumentoUpdateDto dto) {
        TipoDocumento existing = lockConfiguration(id);
        String fiscal = dto.codigoFiscal() == null || dto.codigoFiscal().isBlank()
                ? null : dto.codigoFiscal().trim().toUpperCase(Locale.ROOT);
        boolean classificationChanged = (
                !Objects.equals(existing.getCodigoFiscal(), fiscal)
                || !Objects.equals(existing.getAreaGestao(), dto.areaGestao())
                || !Objects.equals(existing.getEntidade(), dto.entidade())
                || !Objects.equals(existing.getSinalContabilistico(), dto.sinalContabilistico())
                || existing.isLiquidacaoImediata() != dto.liquidacaoImediata());
        if (existing.isConfiguracaoBloqueada() && classificationChanged) {
            throw new ConflictException("Configuração fiscal e financeira bloqueada: já existe emissão documental deste tipo");
        }
        if (existing.getFiscalCatalogCode() != null) {
            FiscalCatalog.Revision revision;
            try { revision = fiscalCatalog.revision(existing.getFiscalCatalogCode(), existing.getFiscalCatalogRevision()); }
            catch (IllegalStateException error) { throw unavailable(); }
            validateTuple(revision, fiscal, dto.areaGestao(), dto.entidade(), dto.sinalContabilistico(), dto.liquidacaoImediata());
        } else if (classificationChanged && deferredFt(fiscal, dto.liquidacaoImediata())) {
            throw new ConflictException("A conversão exige uma nova configuração FT governada; o vínculo não pode ser acrescentado por alteração");
        }
        mapper.applyUpdate(dto, existing);
        repository.saveAndFlush(existing);
    }

    private static boolean deferredFt(String code, boolean immediate) { return "FT".equals(code) && !immediate; }
    private FiscalCatalog.Revision creationRevision() {
        try { return fiscalCatalog.ftDeferredCreation(); }
        catch (IllegalStateException error) { throw unavailable(); }
    }
    private static ConflictException unavailable() {
        return new ConflictException("Criação/utilização FT governada bloqueada: catálogo ou revisão aprovada indisponível");
    }
    private static void validateTuple(FiscalCatalog.Revision revision, String code, Integer area, Integer entity,
                                      Integer sign, boolean immediate) {
        final boolean permitted;
        try { permitted = FtDeferredSemantics.permits(revision.definition(), code, area, entity, sign, immediate); }
        catch (IllegalStateException error) { throw unavailable(); }
        if (!permitted) throw new BadRequestException("Configuração incompatível com a revisão fiscal governada");
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private TipoDocumento findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de documento não encontrado: " + id));
    }
}
