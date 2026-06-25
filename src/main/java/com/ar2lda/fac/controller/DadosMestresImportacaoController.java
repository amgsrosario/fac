package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ImportacaoResultadoDto;
import com.ar2lda.fac.controller.dto.ImportacaoValidacaoDto;
import com.ar2lda.fac.model.TipoDadosMestres;
import com.ar2lda.fac.service.DadosMestresTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/importacoes")
@RequiredArgsConstructor
public class DadosMestresImportacaoController {

    private final DadosMestresTransferService service;

    @PostMapping(value = "/clientes/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacaoValidacaoDto validarClientes(@RequestPart("file") MultipartFile file) {
        return service.validar(TipoDadosMestres.CLIENTES, file);
    }

    @PostMapping("/clientes/{id}/confirmar")
    public ImportacaoResultadoDto confirmarClientes(@PathVariable UUID id) {
        return service.confirmar(TipoDadosMestres.CLIENTES, id);
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> cancelarClientes(@PathVariable UUID id) {
        service.cancelar(TipoDadosMestres.CLIENTES, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clientes/modelo")
    public ResponseEntity<byte[]> modeloClientes(@RequestParam(defaultValue = "csv") String formato) {
        return download(service.modelo(TipoDadosMestres.CLIENTES, formato));
    }

    @PostMapping(value = "/artigos/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacaoValidacaoDto validarArtigos(@RequestPart("file") MultipartFile file) {
        return service.validar(TipoDadosMestres.ARTIGOS, file);
    }

    @PostMapping("/artigos/{id}/confirmar")
    public ImportacaoResultadoDto confirmarArtigos(@PathVariable UUID id) {
        return service.confirmar(TipoDadosMestres.ARTIGOS, id);
    }

    @DeleteMapping("/artigos/{id}")
    public ResponseEntity<Void> cancelarArtigos(@PathVariable UUID id) {
        service.cancelar(TipoDadosMestres.ARTIGOS, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/artigos/modelo")
    public ResponseEntity<byte[]> modeloArtigos(@RequestParam(defaultValue = "csv") String formato) {
        return download(service.modelo(TipoDadosMestres.ARTIGOS, formato));
    }

    private ResponseEntity<byte[]> download(DadosMestresTransferService.ExportedFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .body(file.content());
    }
}
