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
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
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
    private final DocumentoComercialRepository documentoComercialRepository;
    private final DocumentoFinanceiroRepository documentoFinanceiroRepository;
    private final SerieMapper mapper;

    @Transactional
    public SerieDto create(SerieCreateDto dto) {
        SerieCreateDto normalized = normalize(dto);
        TipoDocumento tipoDocumento = findTipoDocumento(normalized.tipoDocumentoId());
        SerieId id = new SerieId(normalized.tipoDocumentoId(), normalized.serie());
        if (repository.existsById(id)) {
            throw new ConflictException("Série já existe: " + formatId(id));
        }
        validateCodigoAt(normalized.codigoAt(), normalized.dataCodigoAt());
        Serie entity = mapper.fromCreateDTO(normalized, tipoDocumento);
        return mapper.toDTO(repository.save(entity));
    }

    public Page<SerieDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public SerieDto getById(String tipoDocumentoId, String serie) {
        return mapper.toDTO(findEntityById(tipoDocumentoId, serie));
    }

    @Transactional
    public void update(String tipoDocumentoId, String serie, SerieUpdateDto dto) {
        SerieUpdateDto normalized = normalize(dto);
        validateCodigoAt(normalized.codigoAt(), normalized.dataCodigoAt());
        Serie existing = findEntityById(tipoDocumentoId, serie);
        if (existing.getNumerador() > 0 && codigoAtChanged(existing, normalized)) {
            throw new BadRequestException("Não é possível alterar o código AT de uma série já utilizada");
        }
        mapper.applyUpdate(normalized, existing);
        repository.save(existing);
    }

    @Transactional
    public void delete(String tipoDocumentoId, String serie) {
        Serie existing = findEntityById(tipoDocumentoId, serie);
        String normalizedTipoDocumentoId = existing.getTipoDocumento().getId();
        String normalizedSerie = existing.getSerie();
        boolean hasDocuments = documentoComercialRepository
                .existsByTipoDocumentoIdAndSerie(normalizedTipoDocumentoId, normalizedSerie)
                || documentoFinanceiroRepository
                .existsByTipoDocumentoIdAndSerie(normalizedTipoDocumentoId, normalizedSerie);
        if (existing.getNumerador() > 0 || hasDocuments) {
            throw new BadRequestException("Não é possível eliminar uma série já utilizada");
        }
        repository.delete(existing);
        repository.flush();
    }

    @Transactional
    public Long proximoNumero(String tipoDocumentoId, String serie) {
        return findForUpdate(tipoDocumentoId, serie).proximoNumero();
    }

    @Transactional
    public SerieNumeracao proximoNumeroParaEmissao(String tipoDocumentoId, String serie) {
        Serie entity = findForUpdate(tipoDocumentoId, serie);
        if (!entity.temCodigoAt()) {
            throw new BadRequestException("A série selecionada não possui código de validação atribuído pela AT.");
        }
        return new SerieNumeracao(entity.proximoNumero(), entity.getCodigoAt().trim());
    }

    private Serie findForUpdate(String tipoDocumentoId, String serie) {
        String normalizedTipoDocumentoId = normalizeTipoDocumentoId(tipoDocumentoId);
        String normalizedSerie = normalizeSerie(serie);
        SerieId id = new SerieId(normalizedTipoDocumentoId, normalizedSerie);
        return repository.findForUpdate(normalizedTipoDocumentoId, normalizedSerie)
                .orElseThrow(() -> new NotFoundException("Série não encontrada: " + formatId(id)));
    }

    private Serie findEntityById(String tipoDocumentoId, String serie) {
        SerieId id = new SerieId(normalizeTipoDocumentoId(tipoDocumentoId), normalizeSerie(serie));
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

    private SerieCreateDto normalize(SerieCreateDto dto) {
        return new SerieCreateDto(
                normalizeSerie(dto.serie()),
                normalizeTipoDocumentoId(dto.tipoDocumentoId()),
                normalizeRequired(dto.nome(), "Nome da série", 50),
                normalizeOptional(dto.codigoAt(), "Código AT", 100),
                dto.dataCodigoAt()
        );
    }

    private SerieUpdateDto normalize(SerieUpdateDto dto) {
        return new SerieUpdateDto(
                normalizeRequired(dto.nome(), "Nome da série", 50),
                normalizeOptional(dto.codigoAt(), "Código AT", 100),
                dto.dataCodigoAt()
        );
    }

    private String normalizeSerie(String value) {
        return normalizeRequired(value, "Série", 10);
    }

    private String normalizeTipoDocumentoId(String value) {
        String normalized = normalizeRequired(value, "Tipo de documento", 3);
        if (normalized.length() < 2) {
            throw new BadRequestException("Tipo de documento deve ter pelo menos 2 caracteres");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String field, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BadRequestException(field + " é obrigatório");
        }
        if (normalized.length() > maxLength) {
            throw new BadRequestException(field + " deve ter no máximo " + maxLength + " caracteres");
        }
        return normalized;
    }

    private String normalizeOptional(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BadRequestException(field + " deve ter no máximo " + maxLength + " caracteres");
        }
        return normalized;
    }

    private boolean codigoAtChanged(Serie existing, SerieUpdateDto normalized) {
        return !java.util.Objects.equals(
                normalizeOptional(existing.getCodigoAt(), "Código AT", 100),
                normalized.codigoAt())
                || !java.util.Objects.equals(existing.getDataCodigoAt(), normalized.dataCodigoAt());
    }

    private String formatId(SerieId id) {
        return id.tipoDocumento() + "/" + id.serie();
    }
}
