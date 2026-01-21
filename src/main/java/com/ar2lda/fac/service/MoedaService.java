package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
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

    public Moeda create(Moeda entity) {
        if (repository.existsById(entity.getId())) {
            throw new ConflictException("Moeda já existe: " + entity.getId());
        }
        return repository.save(entity);
    }

    public Moeda getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moeda não encontrada: " + id));
    }

    public Page<Moeda> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void update(String id, Moeda update) {
        Moeda existing = getById(id);
        existing.setNome(update.getNome());
        existing.setVcompra(update.getVcompra());
        existing.setVvenda(update.getVvenda());
        existing.setSimbolo(update.getSimbolo());
        existing.setNdecimais(update.getNdecimais());
        existing.setCiso(update.getCiso());
        repository.save(existing);
    }

    public void delete(String id) {
        Moeda existing = getById(id);
        repository.delete(existing);
    }
}
