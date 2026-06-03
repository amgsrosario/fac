package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.MoedaCreateDto;
import com.ar2lda.fac.controller.dto.MoedaDto;
import com.ar2lda.fac.controller.dto.MoedaUpdateDto;
import com.ar2lda.fac.service.MoedaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/moedas")
@RequiredArgsConstructor
public class MoedaController implements GenericController {

    private final MoedaService service;

    @PostMapping
    public ResponseEntity<MoedaDto> create(@RequestBody @Valid MoedaCreateDto dto) {
        MoedaDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<MoedaDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public MoedaDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody @Valid MoedaUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
