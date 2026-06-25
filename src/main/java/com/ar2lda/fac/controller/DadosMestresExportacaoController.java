package com.ar2lda.fac.controller;

import com.ar2lda.fac.model.TipoDadosMestres;
import com.ar2lda.fac.service.DadosMestresTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exportacoes")
@RequiredArgsConstructor
public class DadosMestresExportacaoController {

    private final DadosMestresTransferService service;

    @GetMapping("/clientes")
    public ResponseEntity<byte[]> exportarClientes(@RequestParam(defaultValue = "csv") String formato,
                                                   @RequestParam(required = false) Boolean ativos) {
        return download(service.exportar(TipoDadosMestres.CLIENTES, formato, ativos));
    }

    @GetMapping("/artigos")
    public ResponseEntity<byte[]> exportarArtigos(@RequestParam(defaultValue = "csv") String formato,
                                                  @RequestParam(required = false) Boolean ativos) {
        return download(service.exportar(TipoDadosMestres.ARTIGOS, formato, ativos));
    }

    private ResponseEntity<byte[]> download(DadosMestresTransferService.ExportedFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .body(file.content());
    }
}
