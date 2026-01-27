package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.repository.MPagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MPagamentoService {

    private final MPagamentoRepository repository;

    public MPagamento create(MPagamento entity) {
        return repository.save(entity);
    }

    public Page<MPagamento> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public MPagamento getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modo de pagamento não encontrado: " + id));
    }

    public void update(Integer id, MPagamento update) {
        MPagamento existing = getById(id);
        existing.setNome(update.getNome());
        repository.save(existing);
    }

    public void delete(Integer id) {
        MPagamento existing = getById(id);
        repository.delete(existing);
    }
}
