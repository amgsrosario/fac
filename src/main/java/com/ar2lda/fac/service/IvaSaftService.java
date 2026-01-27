package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.IvaSaftCreateDto;
import com.ar2lda.fac.controller.dto.IvaSaftDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.IvaSaftMapper;
import com.ar2lda.fac.model.IvaSaft;
import com.ar2lda.fac.repository.IvaSaftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IvaSaftService {

    private final IvaSaftRepository repository;
    private final IvaSaftMapper mapper;

    public IvaSaftDto create(IvaSaftCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("IvaSaft já existe: " + dto.id());
        }
        IvaSaft entity = new IvaSaft(dto.id(), dto.nome());
        IvaSaft saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    public IvaSaft getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("IvaSaft não encontrado: " + id));
    }

    public Page<IvaSaft> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void update(String id, String nome) {
        IvaSaft existing = getById(id);
        existing.setNome(nome);
        repository.save(existing);
    }

    public void delete(String id) {
        IvaSaft existing = getById(id);
        repository.delete(existing);
    }
}
