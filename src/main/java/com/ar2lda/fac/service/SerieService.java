package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.SerieCreateDto;
import com.ar2lda.fac.controller.dto.SerieDto;
import com.ar2lda.fac.controller.dto.SerieUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.SerieMapper;
import com.ar2lda.fac.model.Serie;
import com.ar2lda.fac.model.SerieId;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.repository.SerieRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import jakarta.transaction.Transactional;
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
        SerieId id = new SerieId(dto.tipoDocumentoId(), dto.serie());
        if (repository.existsById(id)) {
            throw new ConflictException("Série já existe: " + formatId(id));
        }
        validateCodigoAt(dto.codigoAt(), dto.dataCodigoAt());
        Serie entity = mapper.fromCreateDTO(dto);
        entity.setTipoDocumento(findTipoDocumento(dto.tipoDocumentoId()));
        return mapper.toDTO(repository.save(entity));
    }

    public Page<SerieDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public SerieDto getById(String tipoDocumentoId, String serie) {
        return mapper.toDTO(findEntityById(tipoDocumentoId, serie));
    }

    public void update(String tipoDocumentoId, String serie, SerieUpdateDto dto) {
        validateCodigoAt(dto.codigoAt(), dto.dataCodigoAt());
        Serie existing = findEntityById(tipoDocumentoId, serie);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String tipoDocumentoId, String serie) {
        repository.delete(findEntityById(tipoDocumentoId, serie));
    }

    @Transactional
    public Long proximoNumero(String tipoDocumentoId, String serie) {
        SerieId id = new SerieId(tipoDocumentoId, serie);
        Serie entity = repository.findForUpdate(tipoDocumentoId, serie)
                .orElseThrow(() -> new NotFoundException("Série não encontrada: " + formatId(id)));
        return entity.proximoNumero();
    }

    private Serie findEntityById(String tipoDocumentoId, String serie) {
        SerieId id = new SerieId(tipoDocumentoId, serie);
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Série não encontrada: " + formatId(id)));
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

    private String formatId(SerieId id) {
        return id.tipoDocumento() + "/" + id.serie();
    }
}
