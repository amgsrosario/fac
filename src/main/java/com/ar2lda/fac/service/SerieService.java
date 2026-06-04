package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.SerieCreateDto;
import com.ar2lda.fac.controller.dto.SerieDto;
import com.ar2lda.fac.controller.dto.SerieUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.SerieMapper;
import com.ar2lda.fac.model.Serie;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.repository.SerieRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SerieService {

    private final SerieRepository repository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SerieMapper mapper;

    public SerieDto create(SerieCreateDto dto) {
        if (repository.existsById(dto.serie())) {
            throw new ConflictException("Série já existe: " + dto.serie());
        }
        validateCodigoAt(dto.codigoAt(), dto.dataCodigoAt());
        Serie entity = mapper.fromCreateDTO(dto);
        entity.setTipoDocumento(findTipoDocumento(dto.tipoDocumentoId()));
        return mapper.toDTO(repository.save(entity));
    }

    public Page<SerieDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public SerieDto getById(String serie) {
        return mapper.toDTO(findEntityById(serie));
    }

    public void update(String serie, SerieUpdateDto dto) {
        validateCodigoAt(dto.codigoAt(), dto.dataCodigoAt());
        Serie existing = findEntityById(serie);
        mapper.applyUpdate(dto, existing);
        existing.setTipoDocumento(findTipoDocumento(dto.tipoDocumentoId()));
        repository.save(existing);
    }

    public void delete(String serie) {
        repository.delete(findEntityById(serie));
    }

    private Serie findEntityById(String serie) {
        return repository.findById(serie)
                .orElseThrow(() -> new NotFoundException("Série não encontrada: " + serie));
    }

    private TipoDocumento findTipoDocumento(String id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de documento não encontrado: " + id));
    }

    private void validateCodigoAt(String codigoAt, LocalDate dataCodigoAt) {
        boolean hasCodigoAt = codigoAt != null && !codigoAt.isBlank();
        boolean hasDataCodigoAt = dataCodigoAt != null;
        if (hasCodigoAt != hasDataCodigoAt) {
            throw new BadRequestException("Código AT e data do código AT devem ser preenchidos em conjunto");
        }
    }
}
