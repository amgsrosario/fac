package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.EmpresaCreateDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.EmpresaUpdateDto;
import com.ar2lda.fac.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public EmpresaDto update(@RequestBody @Valid EmpresaUpdateDto dto) {
        return service.update(dto);
    }

    @PostMapping(value = "/logotipo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmpresaDto guardarLogotipo(@RequestPart("file") MultipartFile file) {
        return service.guardarLogotipo(file);
    }

    @GetMapping("/logotipo")
    public ResponseEntity<byte[]> obterLogotipo() {
        EmpresaService.LogoDocumento logo = service.obterLogotipo();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(logo.mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + logo.nomeInterno() + "\"")
                .body(logo.bytes());
    }

    @DeleteMapping("/logotipo")
    public ResponseEntity<Void> removerLogotipo() {
        service.removerLogotipo();
        return ResponseEntity.noContent().build();
    }
}
