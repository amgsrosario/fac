package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.FreguesiaCreateDto;
import com.ar2lda.fac.controller.dto.FreguesiaDto;
import com.ar2lda.fac.controller.dto.FreguesiaUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.FreguesiaMapper;
import com.ar2lda.fac.model.Freguesia;
import com.ar2lda.fac.repository.FreguesiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FreguesiaService {

    private final FreguesiaRepository repository;
    private final FreguesiaMapper mapper;

    public FreguesiaDto create(FreguesiaCreateDto dto) {
        if (repository.existsById(dto.codigo())) {
            throw new ConflictException("Freguesia já existe: " + dto.codigo());
        }
        return mapper.toDTO(repository.save(mapper.fromCreateDTO(dto)));
    }

    public Page<FreguesiaDto> list(String search, Pageable pageable) {
        Pageable safePageable = withAllowedSort(pageable);
        String term = normalizeSearch(search);
        Page<Freguesia> result = term.isEmpty()
                ? repository.findAll(safePageable)
                : repository.findAllBySearch(term, safePageable);
        return result.map(mapper::toDTO);
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return "";
        }
        return Normalizer.normalize(search.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    public FreguesiaDto getByCodigo(String codigo) {
        return mapper.toDTO(findEntityByCodigo(codigo));
    }

    public void update(String codigo, FreguesiaUpdateDto dto) {
        Freguesia existing = findEntityByCodigo(codigo);
        mapper.applyUpdate(dto, existing);
        repository.save(existing);
    }

    public void delete(String codigo) {
        repository.delete(findEntityByCodigo(codigo));
        repository.flush();
    }

    private Freguesia findEntityByCodigo(String codigo) {
        return repository.findById(codigo)
                .orElseThrow(() -> new NotFoundException("Freguesia não encontrada: " + codigo));
    }

    private Pageable withAllowedSort(Pageable pageable) {
        Sort safeSort = Sort.by(pageable.getSort().stream()
                .filter(order -> order.getProperty().equals("codigo")
                        || order.getProperty().equals("concelho")
                        || order.getProperty().equals("nome")
                        || order.getProperty().equals("extinta"))
                .toList());
        if (safeSort.isUnsorted()) safeSort = Sort.by(Sort.Direction.ASC, "codigo");
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }
}
