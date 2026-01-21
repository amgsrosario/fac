package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
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

    public CodPostal create(CodPostal entity) {
        if (repository.existsById(entity.getId())) {
            throw new ConflictException("Código postal já existe: " + entity.getId());
        }
        return repository.save(entity);
    }

    public CodPostal getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Código postal não encontrado: " + id));
    }

    public Page<CodPostal> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void update(String id, String nome) {
        CodPostal existing = getById(id);
        existing.setNome(nome);
        repository.save(existing);
    }

    public void delete(String id) {
        CodPostal existing = getById(id);
        repository.delete(existing);
    }
}
