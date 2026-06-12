package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.MIsencaoCreateDto;
import com.ar2lda.fac.controller.dto.MIsencaoDto;
import com.ar2lda.fac.controller.dto.MIsencaoUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.MIsencaoMapper;
import com.ar2lda.fac.model.IvaSaft;
import com.ar2lda.fac.model.MIsencao;
import com.ar2lda.fac.repository.IvaSaftRepository;
import com.ar2lda.fac.repository.MIsencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MIsencaoService {

    private final MIsencaoRepository repository;
    private final IvaSaftRepository ivaSaftRepository;
    private final MIsencaoMapper mapper;

    public MIsencaoDto create(MIsencaoCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Motivo de isenção já existe: " + dto.id());
        }
        MIsencao entity = mapper.fromCreateDTO(dto);
        entity.setIvaSaft(findIvaSaft(dto.ivaSaftId()));
        return mapper.toDTO(repository.save(entity));
    }

    public Page<MIsencaoDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public MIsencaoDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, MIsencaoUpdateDto dto) {
        MIsencao existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        existing.setIvaSaft(findIvaSaft(dto.ivaSaftId()));
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private MIsencao findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Motivo de isenção não encontrado: " + id));
    }

    private IvaSaft findIvaSaft(String id) {
        return ivaSaftRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("IVA SAF-T não encontrado: " + id));
    }
}
