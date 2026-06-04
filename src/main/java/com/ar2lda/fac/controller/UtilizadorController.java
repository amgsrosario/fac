package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.UtilizadorCreateDto;
import com.ar2lda.fac.controller.dto.UtilizadorDto;
import com.ar2lda.fac.controller.dto.UtilizadorUpdateDto;
import com.ar2lda.fac.service.UtilizadorService;
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
@RequestMapping("/utilizadores")
@RequiredArgsConstructor
public class UtilizadorController implements GenericController {

    private final UtilizadorService service;

    @PostMapping
    public ResponseEntity<UtilizadorDto> create(@RequestBody @Valid UtilizadorCreateDto dto) {
        UtilizadorDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.codigo());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<UtilizadorDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{codigo}")
    public UtilizadorDto getByCodigo(@PathVariable String codigo) {
        return service.getByCodigo(codigo);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void> update(@PathVariable String codigo, @RequestBody @Valid UtilizadorUpdateDto dto) {
        service.update(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> delete(@PathVariable String codigo) {
        service.delete(codigo);
        return ResponseEntity.noContent().build();
    }
}
