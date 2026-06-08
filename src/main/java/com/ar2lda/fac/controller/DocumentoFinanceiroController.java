package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroCreateDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDiagnosticoDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import com.ar2lda.fac.service.DocumentoFinanceiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/documentos-financeiros")
@RequiredArgsConstructor
public class DocumentoFinanceiroController implements GenericController {

    private final DocumentoFinanceiroService service;

    @PostMapping
    public ResponseEntity<DocumentoFinanceiroDto> create(@RequestBody @Valid DocumentoFinanceiroCreateDto dto) {
        DocumentoFinanceiroDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<DocumentoFinanceiroDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public DocumentoFinanceiroDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/impressao")
    public DocumentoFinanceiroImpressaoDto getImpressao(@PathVariable Long id) {
        return service.getImpressao(id);
    }

    @GetMapping("/{id}/diagnostico")
    public DocumentoFinanceiroDiagnosticoDto getDiagnostico(@PathVariable Long id) {
        return service.getDiagnostico(id);
    }

    @GetMapping(value = "/{id}/diagnostico/html", produces = MediaType.TEXT_HTML_VALUE)
    public String getDiagnosticoHtml(@PathVariable Long id) {
        return service.getDiagnosticoHtml(id);
    }

    @PostMapping("/{id}/anular")
    public DocumentoFinanceiroDto anular(@PathVariable Long id) {
        return service.anular(id);
    }
}
