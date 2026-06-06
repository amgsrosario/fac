package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ParametrosAplicacaoCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosAplicacaoDto;
import com.ar2lda.fac.controller.dto.ParametrosAplicacaoUpdateDto;
import com.ar2lda.fac.service.ParametrosAplicacaoService;
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
@RequestMapping("/parametros-aplicacao")
@RequiredArgsConstructor
public class ParametrosAplicacaoController {

    private final ParametrosAplicacaoService service;

    @PostMapping
    public ResponseEntity<ParametrosAplicacaoDto> create(@RequestBody @Valid ParametrosAplicacaoCreateDto dto) {
        return ResponseEntity.created(URI.create("/parametros-aplicacao")).body(service.create(dto));
    }

    @GetMapping
    public ParametrosAplicacaoDto get() {
        return service.get();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid ParametrosAplicacaoUpdateDto dto) {
        service.update(dto);
        return ResponseEntity.noContent().build();
    }
}
