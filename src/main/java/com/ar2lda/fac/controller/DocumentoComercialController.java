package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.DocumentoComercialComLinhaCreateDto;
import com.ar2lda.fac.controller.dto.AnularDocumentoRequest;
import com.ar2lda.fac.controller.dto.DocumentoComercialDiagnosticoDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialEmitirDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialUpdateDto;
import com.ar2lda.fac.service.DocumentoComercialService;
import com.ar2lda.fac.service.DocumentoComercialCriacaoService;
import com.ar2lda.fac.service.DocumentoComercialPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.controller.dto.DocumentoComercialResumoDto;
import java.time.LocalDate;
import java.util.Set;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final DocumentoComercialCriacaoService criacaoService;
    private final DocumentoComercialPdfService pdfService;

    @PostMapping
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_CRIAR')")
    public ResponseEntity<DocumentoComercialDto> create(
            @RequestBody @Valid DocumentoComercialComLinhaCreateDto dto) {
        DocumentoComercialDto created = criacaoService.createComPrimeiraLinha(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_CONSULTAR')")
    public Page<DocumentoComercialResumoDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoDocumentoComercial estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEmissao,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String cliente,
            Pageable pageable) {
        return service.list(search, estado, dataEmissao, documento, cliente, validatedPageable(pageable));
    }

    private Pageable validatedPageable(Pageable pageable) {
        Set<String> allowed = Set.of("id", "dataEmissao", "numeroDocumentoCompleto", "clienteNome", "valorTotal", "estado");
        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw new ResponseStatusException(BAD_REQUEST, "Ordenacao nao suportada: " + order.getProperty());
            }
        }
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Order.desc("dataEmissao"), Sort.Order.desc("id"));
        if (sort.getOrderFor("id") == null) sort = sort.and(Sort.by(Sort.Order.desc("id")));
        int size = pageable.getPageSize() == 50 ? 50 : 20;
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_CONSULTAR')")
    public DocumentoComercialDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/impressao")
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_CONSULTAR')")
    public DocumentoComercialImpressaoDto getImpressao(@PathVariable Long id) {
        return service.getImpressao(id);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_OBTER_PDF')")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        DocumentoComercialPdfService.PdfDocumento pdf = pdfService.gerar(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.filename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.content());
    }

    @GetMapping("/{id}/diagnostico")
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_CONSULTAR')")
    public DocumentoComercialDiagnosticoDto getDiagnostico(@PathVariable Long id) {
        return service.getDiagnostico(id);
    }

    @GetMapping(value = "/{id}/diagnostico/html", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_CONSULTAR')")
    public String getDiagnosticoHtml(@PathVariable Long id) {
        return service.getDiagnosticoHtml(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_EDITAR_RASCUNHO')")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody @Valid DocumentoComercialUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/emitir")
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_EMITIR')")
    public DocumentoComercialDto emitir(@PathVariable Long id, @RequestBody @Valid DocumentoComercialEmitirDto dto) {
        return service.emitir(id, dto);
    }

    @PostMapping("/{id}/anular")
    public DocumentoComercialDto anular(@PathVariable Long id, @RequestBody @Valid AnularDocumentoRequest request) {
        return service.anular(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@functionalAuthorization.has('DOCUMENTO_ELIMINAR_RASCUNHO')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
