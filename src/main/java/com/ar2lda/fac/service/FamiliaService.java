package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.FamiliaCreateDto;
import com.ar2lda.fac.controller.dto.FamiliaDto;
import com.ar2lda.fac.controller.dto.FamiliaUpdateDto;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.FamiliaMapper;
import com.ar2lda.fac.model.Familia;
import com.ar2lda.fac.repository.FamiliaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FamiliaService {

    private final FamiliaRepository repository;
    private final FamiliaMapper mapper;

    public FamiliaDto create(FamiliaCreateDto dto) {
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<FamiliaDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public FamiliaDto getById(Long id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(Long id, FamiliaUpdateDto dto) {
        Familia existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findEntityById(id));
    }

    private Familia findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Família não encontrada: " + id));
    }
}
