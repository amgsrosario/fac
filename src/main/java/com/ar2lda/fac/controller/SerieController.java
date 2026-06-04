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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/series")
@RequiredArgsConstructor
public class SerieController implements GenericController {

    private final SerieService service;

    @PostMapping
    public ResponseEntity<SerieDto> create(@RequestBody @Valid SerieCreateDto dto) {
        SerieDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.serie());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<SerieDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{serie}")
    public SerieDto getById(@PathVariable String serie) {
        return service.getById(serie);
    }

    @PutMapping("/{serie}")
    public ResponseEntity<Void> update(@PathVariable String serie, @RequestBody @Valid SerieUpdateDto dto) {
        service.update(serie, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{serie}")
    public ResponseEntity<Void> delete(@PathVariable String serie) {
        service.delete(serie);
        return ResponseEntity.noContent().build();
    }
}
