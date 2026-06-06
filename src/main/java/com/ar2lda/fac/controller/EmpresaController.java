package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.EmpresaCreateDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.EmpresaUpdateDto;
import com.ar2lda.fac.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService service;

    @PostMapping
    public ResponseEntity<EmpresaDto> create(@RequestBody @Valid EmpresaCreateDto dto) {
        return ResponseEntity.created(URI.create("/empresa")).body(service.create(dto));
    }

    @GetMapping
    public EmpresaDto get() {
        return service.get();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid EmpresaUpdateDto dto) {
        service.update(dto);
        return ResponseEntity.noContent().build();
    }
}
