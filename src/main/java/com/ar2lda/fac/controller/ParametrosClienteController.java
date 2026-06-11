package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ParametrosClienteCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosClienteDto;
import com.ar2lda.fac.controller.dto.ParametrosClienteUpdateDto;
import com.ar2lda.fac.service.ParametrosClienteService;
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
@RequestMapping("/parametros-cliente")
@RequiredArgsConstructor
public class ParametrosClienteController {

    private final ParametrosClienteService service;

    @PostMapping
    public ResponseEntity<ParametrosClienteDto> create(@RequestBody @Valid ParametrosClienteCreateDto dto) {
        return ResponseEntity.created(URI.create("/parametros-cliente")).body(service.create(dto));
    }

    @GetMapping
    public ParametrosClienteDto get() {
        return service.get();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid ParametrosClienteUpdateDto dto) {
        service.update(dto);
        return ResponseEntity.noContent().build();
    }
}
