package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.TipoDocumentoCreateDto;
import com.ar2lda.fac.controller.dto.TipoDocumentoDto;
import com.ar2lda.fac.controller.dto.TipoDocumentoUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.TipoDocumentoMapper;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TipoDocumentoService {

    private final TipoDocumentoRepository repository;
    private final TipoDocumentoMapper mapper;

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

    public void update(String id, TipoDocumentoUpdateDto dto) {
        TipoDocumento existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
    }

    private TipoDocumento findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de documento não encontrado: " + id));
    }
}
