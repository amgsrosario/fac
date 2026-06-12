package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.TransporteCreateDto;
import com.ar2lda.fac.controller.dto.TransporteDto;
import com.ar2lda.fac.controller.dto.TransporteUpdateDto;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.TransporteMapper;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.repository.TransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransporteService {

    private final TransporteRepository repository;
    private final TransporteMapper mapper;

    public TransporteDto create(TransporteCreateDto dto) {
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<TransporteDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public TransporteDto getById(Integer id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(Integer id, TransporteUpdateDto dto) {
        Transporte existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(Integer id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private Transporte findEntityById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transporte não encontrado: " + id));
    }
}
