package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.model.PPagamento;
import com.ar2lda.fac.repository.PPagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PPagamentoService {

    private final PPagamentoRepository repository;

    public PPagamento create(PPagamento entity) {
        return repository.save(entity);
    }

    public Page<PPagamento> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public PPagamento getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prazo de pagamento não encontrado: " + id));
    }

    public void update(String id, PPagamento update) {
        PPagamento existing = getById(id);
        existing.setNome(update.getNome());
        existing.setDias(update.getDias());
        repository.save(existing);
    }

    public void delete(String id) {
        PPagamento existing = getById(id);
        repository.delete(existing);
    }
}
