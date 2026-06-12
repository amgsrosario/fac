package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.MoedaCreateDto;
import com.ar2lda.fac.controller.dto.MoedaDto;
import com.ar2lda.fac.controller.dto.MoedaUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.MoedaMapper;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.repository.MoedaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MoedaService {

    private final MoedaRepository repository;
    private final MoedaMapper mapper;

    public MoedaDto create(MoedaCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Moeda já existe: " + dto.id());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<MoedaDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public MoedaDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, MoedaUpdateDto dto) {
        Moeda existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private Moeda findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moeda não encontrada: " + id));
    }
}
