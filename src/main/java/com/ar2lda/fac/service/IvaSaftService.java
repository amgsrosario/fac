package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.IvaSaftCreateDto;
import com.ar2lda.fac.controller.dto.IvaSaftDto;
import com.ar2lda.fac.controller.dto.IvaSaftUpdateDto;
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
            throw new ConflictException("IVA SAF-T já existe: " + dto.id());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<IvaSaftDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public IvaSaftDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, IvaSaftUpdateDto dto) {
        IvaSaft existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
    }

    private IvaSaft findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("IVA SAF-T não encontrado: " + id));
    }
}
