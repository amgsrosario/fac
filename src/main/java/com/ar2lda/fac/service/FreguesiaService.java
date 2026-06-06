package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.FreguesiaCreateDto;
import com.ar2lda.fac.controller.dto.FreguesiaDto;
import com.ar2lda.fac.controller.dto.FreguesiaUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.FreguesiaMapper;
import com.ar2lda.fac.model.Freguesia;
import com.ar2lda.fac.repository.FreguesiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FreguesiaService {

    private final FreguesiaRepository repository;
    private final FreguesiaMapper mapper;

    public FreguesiaDto create(FreguesiaCreateDto dto) {
        if (repository.existsById(dto.codigo())) {
            throw new ConflictException("Freguesia já existe: " + dto.codigo());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<FreguesiaDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public FreguesiaDto getByCodigo(String codigo) {
        return mapper.toDTO(findEntityByCodigo(codigo));
    }

    public void update(String codigo, FreguesiaUpdateDto dto) {
        Freguesia existing = findEntityByCodigo(codigo);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String codigo) {
        repository.delete(findEntityByCodigo(codigo));
    }

    private Freguesia findEntityByCodigo(String codigo) {
        return repository.findById(codigo)
                .orElseThrow(() -> new NotFoundException("Freguesia não encontrada: " + codigo));
    }
}
