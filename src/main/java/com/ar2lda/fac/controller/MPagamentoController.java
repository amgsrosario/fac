package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.MPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.MPagamentoDto;
import com.ar2lda.fac.controller.dto.MPagamentoUpdateDto;
import com.ar2lda.fac.service.MPagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/mpagamentos")
@RequiredArgsConstructor
public class MPagamentoController implements GenericController {

    private final MPagamentoService service;

    @PostMapping
    public ResponseEntity<MPagamentoDto> create(@RequestBody @Valid MPagamentoCreateDto dto) {
        MPagamentoDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<MPagamentoDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public MPagamentoDto getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody @Valid MPagamentoUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
