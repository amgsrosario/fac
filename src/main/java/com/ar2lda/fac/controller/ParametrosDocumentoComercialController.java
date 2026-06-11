package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.ParametrosDocumentoComercialUpdateDto;
import com.ar2lda.fac.service.ParametrosDocumentoComercialService;
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
@RequestMapping("/parametros-documento-comercial")
@RequiredArgsConstructor
public class ParametrosDocumentoComercialController {

    private final ParametrosDocumentoComercialService service;

    @PostMapping
    public ResponseEntity<ParametrosDocumentoComercialDto> create(
            @RequestBody @Valid ParametrosDocumentoComercialCreateDto dto) {
        return ResponseEntity.created(URI.create("/parametros-documento-comercial")).body(service.create(dto));
    }

    @GetMapping
    public ParametrosDocumentoComercialDto get() {
        return service.get();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid ParametrosDocumentoComercialUpdateDto dto) {
        service.update(dto);
        return ResponseEntity.noContent().build();
    }
}
