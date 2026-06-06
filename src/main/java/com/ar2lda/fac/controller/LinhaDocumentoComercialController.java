package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialUpdateDto;
import com.ar2lda.fac.service.LinhaDocumentoComercialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;

@RestController
@RequestMapping("/documentos-comerciais/{documentoId}/linhas")
@RequiredArgsConstructor
public class LinhaDocumentoComercialController {

    private final LinhaDocumentoComercialService service;

    @PostMapping
    public ResponseEntity<LinhaDocumentoComercialDto> create(
            @PathVariable Long documentoId,
            @RequestBody @Valid LinhaDocumentoComercialCreateDto dto) {
        LinhaDocumentoComercialDto created = service.create(documentoId, dto);
        return ResponseEntity
                .created(URI.create("/documentos-comerciais/" + documentoId + "/linhas/" + created.id()))
                .body(created);
    }

    @GetMapping
    public List<LinhaDocumentoComercialDto> list(@PathVariable Long documentoId) {
        return service.list(documentoId);
    }

    @GetMapping("/{linhaId}")
    public LinhaDocumentoComercialDto getById(@PathVariable Long documentoId, @PathVariable Long linhaId) {
        return service.getById(documentoId, linhaId);
    }

    @PutMapping("/{linhaId}")
    public ResponseEntity<Void> update(
            @PathVariable Long documentoId,
            @PathVariable Long linhaId,
            @RequestBody @Valid LinhaDocumentoComercialUpdateDto dto) {
        service.update(documentoId, linhaId, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{linhaId}")
    public ResponseEntity<Void> delete(@PathVariable Long documentoId, @PathVariable Long linhaId) {
        service.delete(documentoId, linhaId);
        return ResponseEntity.noContent().build();
    }
}
