package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.RIvaCreateDto;
import com.ar2lda.fac.controller.dto.RIvaDto;
import com.ar2lda.fac.controller.dto.RIvaTaxaDto;
import com.ar2lda.fac.controller.dto.RIvaUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.RIvaMapper;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.RIvaTaxa;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RIvaService {

    private final RIvaRepository repository;
    private final TipoTaxaIvaRepository tipoTaxaIvaRepository;
    private final RIvaMapper mapper;

    public RIvaDto create(RIvaCreateDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Regime de IVA já existe: " + dto.id());
        }
        RIva entity = mapper.fromCreateDTO(dto);
        entity.substituirTaxas(buildTaxas(dto.taxas(), Set.of()));
        return mapper.toDTO(repository.save(entity));
    }

    public Page<RIvaDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public RIvaDto getById(String id) {
        return mapper.toDTO(findEntityById(id));
    }

    public void update(String id, RIvaUpdateDto dto) {
        RIva existing = findEntityById(id);
        Set<String> tiposAtuais = existing.getTaxas().stream()
                .map(taxa -> taxa.getTipoTaxaIva().getId())
                .collect(java.util.stream.Collectors.toSet());
        mapper.applyUpdate(dto, existing);
        existing.substituirTaxas(buildTaxas(dto.taxas(), tiposAtuais));
        repository.save(existing);
    }

    public void delete(String id) {
        repository.delete(findEntityById(id));
        repository.flush();
    }

    private RIva findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Regime de IVA não encontrado: " + id));
    }

    private List<RIvaTaxa> buildTaxas(List<RIvaTaxaDto> taxas, Set<String> tiposAtuais) {
        Set<String> tiposRecebidos = new HashSet<>();
        return taxas.stream()
                .map(dto -> {
                    if (!tiposRecebidos.add(dto.tipoTaxaIvaId())) {
                        throw new BadRequestException("Tipo de taxa de IVA repetido: " + dto.tipoTaxaIvaId());
                    }
                    TipoTaxaIva tipo = tipoTaxaIvaRepository.findById(dto.tipoTaxaIvaId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Tipo de taxa de IVA não encontrado: " + dto.tipoTaxaIvaId()
                            ));
                    if (tipo.isInativo() && !tiposAtuais.contains(tipo.getId())) {
                        throw new BadRequestException("Tipo de taxa de IVA está inativo: " + tipo.getId());
                    }
                    return new RIvaTaxa(null, tipo, dto.valor());
                })
                .toList();
    }
}
