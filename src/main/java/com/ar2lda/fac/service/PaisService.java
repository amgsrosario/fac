package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.PaisCreateDto;
import com.ar2lda.fac.controller.dto.PaisDto;
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
            throw new ConflictException("Pais já existe: " + dto.id());
        }
        Pais entity = new Pais(dto.id(), dto.nome());
        Pais saved = repository.save(entity);
        return mapper.toDTO(saved);
    }


    public Pais getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pais não encontrado: " + id));
    }

    public Page<Pais> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void update(String id, String nome) {
        Pais existing = getById(id);
        existing.setNome(nome);
        repository.save(existing);
    }

    public void delete(String id) {
        Pais existing = getById(id);
        repository.delete(existing);
    }
}
