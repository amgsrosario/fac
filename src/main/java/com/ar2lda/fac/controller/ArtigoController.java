package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ArtigoCreateDto;
import com.ar2lda.fac.controller.dto.ArtigoDto;
import com.ar2lda.fac.controller.dto.ArtigoUpdateDto;
import com.ar2lda.fac.service.ArtigoService;
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
@RequestMapping("/artigos")
@RequiredArgsConstructor
public class ArtigoController implements GenericController {

    private final ArtigoService service;

    @PostMapping
    public ResponseEntity<ArtigoDto> create(@RequestBody @Valid ArtigoCreateDto dto) {
        ArtigoDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.codigo());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<ArtigoDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{codigo}")
    public ArtigoDto getByCodigo(@PathVariable String codigo) {
        return service.getByCodigo(codigo);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void> update(@PathVariable String codigo, @RequestBody @Valid ArtigoUpdateDto dto) {
        service.update(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> delete(@PathVariable String codigo) {
        service.delete(codigo);
        return ResponseEntity.noContent().build();
    }
}
