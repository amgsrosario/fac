package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.PaisCreateDto;
import com.ar2lda.fac.controller.dto.PaisDto;
import com.ar2lda.fac.controller.dto.PaisUpdateDto;
import com.ar2lda.fac.service.PaisService;
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
@RequestMapping("/paises")
@RequiredArgsConstructor
public class PaisController implements GenericController {

    private final PaisService service;

    @PostMapping
    public ResponseEntity<PaisDto> create(@RequestBody @Valid PaisCreateDto dto) {
        PaisDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<PaisDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public PaisDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody @Valid PaisUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
