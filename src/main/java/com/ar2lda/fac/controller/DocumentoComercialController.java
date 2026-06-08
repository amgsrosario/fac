package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.DocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialEmitirDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialUpdateDto;
import com.ar2lda.fac.service.DocumentoComercialService;
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
@RequestMapping("/documentos-comerciais")
@RequiredArgsConstructor
public class DocumentoComercialController implements GenericController {

    private final DocumentoComercialService service;

    @PostMapping
    public ResponseEntity<DocumentoComercialDto> create(@RequestBody @Valid DocumentoComercialCreateDto dto) {
        DocumentoComercialDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<DocumentoComercialDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public DocumentoComercialDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/impressao")
    public DocumentoComercialImpressaoDto getImpressao(@PathVariable Long id) {
        return service.getImpressao(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody @Valid DocumentoComercialUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/emitir")
    public DocumentoComercialDto emitir(@PathVariable Long id, @RequestBody @Valid DocumentoComercialEmitirDto dto) {
        return service.emitir(id, dto);
    }

    @PostMapping("/{id}/anular")
    public DocumentoComercialDto anular(@PathVariable Long id) {
        return service.anular(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
