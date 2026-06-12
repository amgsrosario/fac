package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ArmazemCreateDto;
import com.ar2lda.fac.controller.dto.ArmazemDto;
import com.ar2lda.fac.controller.dto.ArmazemUpdateDto;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.ArmazemMapper;
import com.ar2lda.fac.model.Armazem;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Freguesia;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.repository.ArmazemRepository;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.FreguesiaRepository;
import com.ar2lda.fac.repository.PaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArmazemService {

    private final ArmazemRepository armazemRepository;
    private final CodPostalRepository codPostalRepository;
    private final PaisRepository paisRepository;
    private final FreguesiaRepository freguesiaRepository;
    private final ArmazemMapper mapper;

    public ArmazemDto create(ArmazemCreateDto dto) {
        Armazem entity = mapper.fromCreateDTO(dto);
        applyRelations(dto.codPostalId(), dto.paisId(), dto.freguesiaId(), entity);
        return mapper.toDTO(armazemRepository.save(entity));
    }

    public Page<ArmazemDto> list(Pageable pageable) {
        return armazemRepository.findAll(pageable).map(mapper::toDTO);
    }

    public ArmazemDto getById(Long id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(Long id, ArmazemUpdateDto dto) {
        Armazem existing = findEntityById(id);
        mapper.applyUpdate(dto, existing);
        applyRelations(dto.codPostalId(), dto.paisId(), dto.freguesiaId(), existing);
        armazemRepository.save(existing);
    }

    public void delete(Long id) {
        armazemRepository.delete(findEntityById(id));
        armazemRepository.flush();
    }

    private Armazem findEntityById(Long id) {
        return armazemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Armazém não encontrado: " + id));
    }

    private void applyRelations(String codPostalId, String paisId, String freguesiaId, Armazem entity) {
        entity.setCodPostal(findCodPostal(codPostalId));
        entity.setPais(findPais(paisId));
        entity.setFreguesia(findFreguesia(freguesiaId));
    }

    private CodPostal findCodPostal(String id) {
        return codPostalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Código postal não encontrado: " + id));
    }

    private Pais findPais(String id) {
        return paisRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("País não encontrado: " + id));
    }

    private Freguesia findFreguesia(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return freguesiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Freguesia não encontrada: " + id));
    }
}
