package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.EmpresaCreateDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.EmpresaUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.EmpresaMapper;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.model.Freguesia;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.EmpresaRepository;
import com.ar2lda.fac.repository.FreguesiaRepository;
import com.ar2lda.fac.repository.PaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final CodPostalRepository codPostalRepository;
    private final PaisRepository paisRepository;
    private final FreguesiaRepository freguesiaRepository;
    private final EmpresaMapper mapper;

    public EmpresaDto create(EmpresaCreateDto dto) {
        if (empresaRepository.existsById(Empresa.EMPRESA_ID)) {
            throw new ConflictException("Ficha da empresa já existe");
        }
        Empresa entity = mapper.fromCreateDTO(dto);
        applyRelations(dto.codPostalId(), dto.paisId(), dto.freguesiaId(), entity);
        return mapper.toDTO(empresaRepository.save(entity));
    }

    public EmpresaDto get() {
        return mapper.toDTO(findEmpresa());
    }

    public void update(EmpresaUpdateDto dto) {
        Empresa existing = findEmpresa();
        mapper.applyUpdate(dto, existing);
        applyRelations(dto.codPostalId(), dto.paisId(), dto.freguesiaId(), existing);
        empresaRepository.save(existing);
    }

    private Empresa findEmpresa() {
        return empresaRepository.findById(Empresa.EMPRESA_ID)
                .orElseThrow(() -> new NotFoundException("Ficha da empresa não encontrada"));
    }

    private void applyRelations(String codPostalId, String paisId, String freguesiaId, Empresa entity) {
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
