package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.CodPostalCreateDto;
import com.ar2lda.fac.controller.dto.CodPostalDto;
import com.ar2lda.fac.controller.dto.CodPostalUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.CodPostalMapper;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.repository.CodPostalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodPostalService {

    private final CodPostalRepository repository;
    private final CodPostalMapper mapper;

    public CodPostalDto create(CodPostalCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Código postal já existe: " + dto.id());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<CodPostalDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public CodPostalDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, CodPostalUpdateDto dto) {
        CodPostal existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private CodPostal findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Código postal não encontrado: " + id));
    }
}
