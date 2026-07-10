package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.MPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.MPagamentoDto;
import com.ar2lda.fac.controller.dto.MPagamentoUpdateDto;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.MPagamentoMapper;
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
    private final MPagamentoMapper mapper;

    public MPagamentoDto create(MPagamentoCreateDto dto) {
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<MPagamentoDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public MPagamentoDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, MPagamentoUpdateDto dto) {
        MPagamento existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private MPagamento findEntityById(String id) {
        return repository.findById(id.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Modo de pagamento não encontrado: " + id));
    }
}
