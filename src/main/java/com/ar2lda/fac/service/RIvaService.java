package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.RIvaCreateDto;
import com.ar2lda.fac.controller.dto.RIvaDto;
import com.ar2lda.fac.controller.dto.RIvaUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.RIvaMapper;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.repository.RIvaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RIvaService {

    private final RIvaRepository repository;
    private final RIvaMapper mapper;

    public RIvaDto create(RIvaCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Regime de IVA já existe: " + dto.id());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<RIvaDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public RIvaDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, RIvaUpdateDto dto) {
        RIva existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
    }

    private RIva findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Regime de IVA não encontrado: " + id));
    }
}
