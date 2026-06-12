package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.TipoTaxaIvaCreateDto;
import com.ar2lda.fac.controller.dto.TipoTaxaIvaDto;
import com.ar2lda.fac.controller.dto.TipoTaxaIvaUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.TipoTaxaIvaMapper;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TipoTaxaIvaService {

    private final TipoTaxaIvaRepository repository;
    private final TipoTaxaIvaMapper mapper;

    public TipoTaxaIvaDto create(TipoTaxaIvaCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Tipo de taxa de IVA já existe: " + dto.id());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<TipoTaxaIvaDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public TipoTaxaIvaDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, TipoTaxaIvaUpdateDto dto) {
        TipoTaxaIva existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private TipoTaxaIva findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de taxa de IVA não encontrado: " + id));
    }
}
