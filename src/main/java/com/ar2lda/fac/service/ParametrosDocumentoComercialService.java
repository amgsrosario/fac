package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.ParametrosDocumentoComercialMapper;
import com.ar2lda.fac.model.Armazem;
import com.ar2lda.fac.model.ParametrosDocumentoComercial;
import com.ar2lda.fac.model.Serie;
import com.ar2lda.fac.model.SerieId;
import com.ar2lda.fac.repository.ArmazemRepository;
import com.ar2lda.fac.repository.ParametrosDocumentoComercialRepository;
import com.ar2lda.fac.repository.SerieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParametrosDocumentoComercialService {

    private final ParametrosDocumentoComercialRepository repository;
    private final SerieRepository serieRepository;
    private final ArmazemRepository armazemRepository;
    private final ParametrosDocumentoComercialMapper mapper;

    @Transactional
    public ParametrosDocumentoComercialDto create(ParametrosDocumentoComercialCreateDto dto) {
        if (repository.existsById(ParametrosDocumentoComercial.PARAMETROS_ID)) {
            throw new ConflictException("Parâmetros de documento comercial já existem");
        }
        ParametrosDocumentoComercial entity = mapper.fromCreateDTO(dto);
        applyRelations(dto.tipoDocumentoId(), dto.serie(), dto.armazemCargaId(), entity);
        return mapper.toDTO(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ParametrosDocumentoComercialDto get() {
        return mapper.toDTO(findParametros());
    }

    @Transactional
    public void update(ParametrosDocumentoComercialUpdateDto dto) {
        ParametrosDocumentoComercial entity = findParametros();
        mapper.applyUpdate(dto, entity);
        applyRelations(dto.tipoDocumentoId(), dto.serie(), dto.armazemCargaId(), entity);
        repository.save(entity);
    }

    private ParametrosDocumentoComercial findParametros() {
        return repository.findById(ParametrosDocumentoComercial.PARAMETROS_ID)
                .orElseThrow(() -> new NotFoundException("Parâmetros de documento comercial não encontrados"));
    }

    private void applyRelations(String tipoDocumentoId, String serie, Long armazemCargaId,
                                ParametrosDocumentoComercial entity) {
        Serie serieEncontrada = findSerie(tipoDocumentoId, serie);
        entity.setTipoDocumento(serieEncontrada != null ? serieEncontrada.getTipoDocumento() : null);
        entity.setSerie(serieEncontrada != null ? serieEncontrada.getSerie() : null);
        entity.setArmazemCarga(findArmazem(armazemCargaId));
    }

    private Serie findSerie(String tipoDocumentoId, String serie) {
        boolean tipoVazio = tipoDocumentoId == null || tipoDocumentoId.isBlank();
        boolean serieVazia = serie == null || serie.isBlank();
        if (tipoVazio && serieVazia) return null;
        if (tipoVazio || serieVazia) {
            throw new BadRequestException("Tipo de documento e série devem ser preenchidos em conjunto");
        }
        Serie encontrada = serieRepository.findById(new SerieId(tipoDocumentoId, serie))
                .orElseThrow(() -> new NotFoundException("Série não encontrada: " + tipoDocumentoId + "/" + serie));
        Integer areaGestao = encontrada.getTipoDocumento().getAreaGestao();
        if (areaGestao != 1 && areaGestao != 2) {
            throw new BadRequestException("A série configurada não pertence a um documento comercial");
        }
        return encontrada;
    }

    private Armazem findArmazem(Long id) {
        if (id == null) return null;
        return armazemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Armazém não encontrado: " + id));
    }
}
