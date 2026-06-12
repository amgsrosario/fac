package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.PaisCreateDto;
import com.ar2lda.fac.controller.dto.PaisDto;
import com.ar2lda.fac.controller.dto.PaisUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.PaisMapper;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.repository.PaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaisService {

    private final PaisRepository repository;
    private final PaisMapper mapper;

    public PaisDto create(PaisCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("País já existe: " + dto.id());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<PaisDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public PaisDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, PaisUpdateDto dto) {
        Pais existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private Pais findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("País não encontrado: " + id));
    }
}
