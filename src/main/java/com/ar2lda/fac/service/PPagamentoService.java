package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.PPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.PPagamentoDto;
import com.ar2lda.fac.controller.dto.PPagamentoUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.PPagamentoMapper;
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
    private final PPagamentoMapper mapper;

    public PPagamentoDto create(PPagamentoCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Prazo de pagamento já existe: " + dto.id());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<PPagamentoDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public PPagamentoDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, PPagamentoUpdateDto dto) {
        PPagamento existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
    }

    private PPagamento findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prazo de pagamento não encontrado: " + id));
    }
}
