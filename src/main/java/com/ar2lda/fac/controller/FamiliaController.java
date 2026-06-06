package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.FamiliaCreateDto;
import com.ar2lda.fac.controller.dto.FamiliaDto;
import com.ar2lda.fac.controller.dto.FamiliaUpdateDto;
import com.ar2lda.fac.service.FamiliaService;
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
@RequestMapping("/familias")
@RequiredArgsConstructor
public class FamiliaController implements GenericController {

    private final FamiliaService service;

    @PostMapping
    public ResponseEntity<FamiliaDto> create(@RequestBody @Valid FamiliaCreateDto dto) {
        FamiliaDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<FamiliaDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public FamiliaDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody @Valid FamiliaUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
