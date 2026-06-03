package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.TransporteCreateDto;
import com.ar2lda.fac.controller.dto.TransporteDto;
import com.ar2lda.fac.controller.dto.TransporteUpdateDto;
import com.ar2lda.fac.service.TransporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/transportes")
@RequiredArgsConstructor
public class TransporteController implements GenericController {

    private final TransporteService service;

    @PostMapping
    public ResponseEntity<TransporteDto> create(@RequestBody @Valid TransporteCreateDto dto) {
        TransporteDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<TransporteDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public TransporteDto getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody @Valid TransporteUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
