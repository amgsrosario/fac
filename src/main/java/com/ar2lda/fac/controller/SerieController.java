package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.SerieCreateDto;
import com.ar2lda.fac.controller.dto.SerieDto;
import com.ar2lda.fac.controller.dto.SerieUpdateDto;
import com.ar2lda.fac.service.SerieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/series")
@RequiredArgsConstructor
public class SerieController implements GenericController {

    private final SerieService service;

    @PostMapping
    @PreAuthorize("@functionalAuthorization.has('SERIE_GERIR')")
    public ResponseEntity<SerieDto> create(@RequestBody @Valid SerieCreateDto dto) {
        SerieDto created = service.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{tipoDocumentoId}/{serie}")
                .buildAndExpand(created.tipoDocumentoId(), created.serie())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @PreAuthorize("@functionalAuthorization.has('SERIE_CONSULTAR')")
    public Page<SerieDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{tipoDocumentoId}/{serie}")
    @PreAuthorize("@functionalAuthorization.has('SERIE_CONSULTAR')")
    public SerieDto getById(@PathVariable String tipoDocumentoId, @PathVariable String serie) {
        return service.getById(tipoDocumentoId, serie);
    }

    @PutMapping("/{tipoDocumentoId}/{serie}")
    @PreAuthorize("@functionalAuthorization.has('SERIE_GERIR')")
    public ResponseEntity<Void> update(@PathVariable String tipoDocumentoId, @PathVariable String serie,
                                       @RequestBody @Valid SerieUpdateDto dto) {
        service.update(tipoDocumentoId, serie, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tipoDocumentoId}/{serie}")
    @PreAuthorize("@functionalAuthorization.has('SERIE_GERIR')")
    public ResponseEntity<Void> delete(@PathVariable String tipoDocumentoId, @PathVariable String serie) {
        service.delete(tipoDocumentoId, serie);
        return ResponseEntity.noContent().build();
    }
}
