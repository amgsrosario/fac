package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ExtratoClienteDto;
import com.ar2lda.fac.reporting.extrato.ExtratoClienteExcelExporter;
import com.ar2lda.fac.reporting.extrato.ExtratoClientePdfExporter;
import com.ar2lda.fac.service.ExtratoClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/extratos/clientes")
@RequiredArgsConstructor
public class ExtratoClienteController {

    private final ExtratoClienteService service;
    private final ExtratoClientePdfExporter pdfExporter;
    private final ExtratoClienteExcelExporter excelExporter;

    @GetMapping("/{clienteId}")
    public ExtratoClienteDto getExtrato(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        return service.getExtrato(clienteId, dataInicial, dataFinal);
    }

    @GetMapping("/{clienteId}/exportar/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        var file = pdfExporter.export(clienteId, dataInicial, dataFinal);
        return download(file.filename(), ExtratoClientePdfExporter.MEDIA_TYPE, file.content());
    }

    @GetMapping("/{clienteId}/exportar/xlsx")
    public ResponseEntity<byte[]> exportExcel(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        var file = excelExporter.export(clienteId, dataInicial, dataFinal);
        return download(file.filename(), ExtratoClienteExcelExporter.MEDIA_TYPE, file.content());
    }

    private ResponseEntity<byte[]> download(String filename, String mediaType, byte[] content) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, java.nio.charset.StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(mediaType))
                .contentLength(content.length)
                .body(content);
    }
}
