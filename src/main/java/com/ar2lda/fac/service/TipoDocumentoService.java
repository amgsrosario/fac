package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.TipoDocumentoCreateDto;
import com.ar2lda.fac.controller.dto.TipoDocumentoDto;
import com.ar2lda.fac.controller.dto.TipoDocumentoUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
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

    /** Serialize configuration edits and first emission before reading fiscal attributes or numbering. */
    @Transactional(propagation = Propagation.MANDATORY)
    public TipoDocumento lockConfiguration(String id) {
        entityManager.flush(); // Include a type created earlier in this transaction before refreshing it.
        TipoDocumento type = findEntityById(id);
        entityManager.refresh(type, LockModeType.PESSIMISTIC_WRITE);
        return type;
    }

    public TipoDocumentoDto create(TipoDocumentoCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Tipo de documento já existe: " + dto.id());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
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
        if (existing.isConfiguracaoBloqueada() && (
                !Objects.equals(existing.getCodigoFiscal(), fiscal)
                || !Objects.equals(existing.getAreaGestao(), dto.areaGestao())
                || !Objects.equals(existing.getEntidade(), dto.entidade())
                || !Objects.equals(existing.getSinalContabilistico(), dto.sinalContabilistico())
                || existing.isLiquidacaoImediata() != dto.liquidacaoImediata())) {
            throw new ConflictException("Configuração fiscal e financeira bloqueada: já existe emissão documental deste tipo");
        }
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
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
