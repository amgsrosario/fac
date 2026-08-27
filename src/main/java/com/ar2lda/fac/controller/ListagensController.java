package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.ListagemDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.ListagemLinhaComercialDto;
import com.ar2lda.fac.controller.dto.ListagemLinhaFinanceiraDto;
import com.ar2lda.fac.controller.dto.PendentesListagemDto;
import com.ar2lda.fac.reporting.pendentes.PendentesExcelExporter;
import com.ar2lda.fac.reporting.pendentes.PendentesPdfExporter;
import com.ar2lda.fac.reporting.listagens.ListagemTabularExporter;
import com.ar2lda.fac.service.ListagensService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/listagens")
@RequiredArgsConstructor
public class ListagensController {

    private static final Set<String> DOCUMENTO_COMERCIAL_SORTS = Set.of(
            "id", "dataEmissao", "numeroDocumento", "cliente.nome", "estado",
            "valorTotal", "dataVencimento", "liquidado"
    );
    private static final Set<String> LINHA_COMERCIAL_SORTS = Set.of(
            "id", "numeroLinha", "documentoComercial.id", "documentoComercial.dataEmissao",
            "documentoComercial.numeroDocumento", "documentoComercial.cliente.nome",
            "artigoCodigo", "descricao", "quantidade", "valorLinha"
    );

    private final ListagensService service;
    private final PendentesPdfExporter pendentesPdfExporter;
    private final PendentesExcelExporter pendentesExcelExporter;
    private final ListagemTabularExporter listagemTabularExporter;

    @GetMapping("/documentos-comerciais")
    public Page<ListagemDocumentoComercialDto> documentosComerciais(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(defaultValue = "false") boolean mostrarAnulados,
            Pageable pageable
    ) {
        return service.documentosComerciais(dataInicial, dataFinal, clienteId, clienteIds, mostrarAnulados,
                validatedPageable(pageable, DOCUMENTO_COMERCIAL_SORTS));
    }

    @GetMapping("/linhas-comerciais")
    public Page<ListagemLinhaComercialDto> linhasComerciais(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String artigoId,
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(required = false) List<String> artigoIds,
            @RequestParam(defaultValue = "false") boolean mostrarTexto,
            Pageable pageable
    ) {
        return service.linhasComerciais(dataInicial, dataFinal, clienteId, clienteIds, artigoId, artigoIds, mostrarTexto,
                validatedPageable(pageable, LINHA_COMERCIAL_SORTS));
    }

    @GetMapping("/documentos-financeiros")
    public Page<DocumentoFinanceiroDto> documentosFinanceiros(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(defaultValue = "false") boolean mostrarAnulados,
            Pageable pageable
    ) {
        return service.documentosFinanceiros(dataInicial, dataFinal, clienteId, clienteIds, mostrarAnulados, pageable);
    }

    @GetMapping("/linhas-financeiras")
    public Page<ListagemLinhaFinanceiraDto> linhasFinanceiras(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) List<Long> clienteIds,
            Pageable pageable
    ) {
        return service.linhasFinanceiras(dataInicial, dataFinal, clienteId, clienteIds, pageable);
    }

    @GetMapping("/pendentes")
    public PendentesListagemDto pendentes(
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(defaultValue = "false") boolean apenasVencidos
    ) {
        return service.pendentes(clienteIds, apenasVencidos);
    }

    @GetMapping("/{source}/exportar/{format}")
    public ResponseEntity<byte[]> exportarListagem(
            @PathVariable String source,
            @PathVariable String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(required = false) List<String> artigoIds,
            @RequestParam(defaultValue = "false") boolean mostrarAnulados,
            @RequestParam(defaultValue = "false") boolean mostrarTexto
    ) {
        if (!Set.of("documentos-comerciais", "linhas-comerciais", "documentos-financeiros", "linhas-financeiras").contains(source)
                || !Set.of("pdf", "xlsx").contains(format)) {
            throw new ResponseStatusException(BAD_REQUEST, "Exportacao nao suportada");
        }
        var file = listagemTabularExporter.export(source, format, dataInicial, dataFinal, clienteIds, artigoIds, mostrarAnulados, mostrarTexto);
        return download(file.filename(), file.mediaType(), file.content());
    }

    @GetMapping("/pendentes-a-data")
    public PendentesListagemDto pendentesAData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia,
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(defaultValue = "false") boolean apenasVencidos
    ) {
        return service.pendentesAData(dataReferencia, clienteIds, apenasVencidos);
    }

    @GetMapping("/pendentes/exportar/pdf")
    public ResponseEntity<byte[]> exportPendentesPdf(
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(defaultValue = "false") boolean apenasVencidos
    ) {
        var file = pendentesPdfExporter.exportPendentes(clienteIds, apenasVencidos);
        return download(file.filename(), PendentesPdfExporter.MEDIA_TYPE, file.content());
    }

    @GetMapping("/pendentes/exportar/xlsx")
    public ResponseEntity<byte[]> exportPendentesExcel(
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(defaultValue = "false") boolean apenasVencidos
    ) {
        var file = pendentesExcelExporter.exportPendentes(clienteIds, apenasVencidos);
        return download(file.filename(), PendentesExcelExporter.MEDIA_TYPE, file.content());
    }

    @GetMapping("/pendentes-a-data/exportar/pdf")
    public ResponseEntity<byte[]> exportPendentesADataPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia,
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(defaultValue = "false") boolean apenasVencidos
    ) {
        var file = pendentesPdfExporter.exportPendentesAData(dataReferencia, clienteIds, apenasVencidos);
        return download(file.filename(), PendentesPdfExporter.MEDIA_TYPE, file.content());
    }

    @GetMapping("/pendentes-a-data/exportar/xlsx")
    public ResponseEntity<byte[]> exportPendentesADataExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia,
            @RequestParam(required = false) List<Long> clienteIds,
            @RequestParam(defaultValue = "false") boolean apenasVencidos
    ) {
        var file = pendentesExcelExporter.exportPendentesAData(dataReferencia, clienteIds, apenasVencidos);
        return download(file.filename(), PendentesExcelExporter.MEDIA_TYPE, file.content());
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

    private Pageable validatedPageable(Pageable pageable, Set<String> allowedSorts) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSorts.contains(order.getProperty())) {
                throw new ResponseStatusException(BAD_REQUEST, "Ordenacao nao suportada: " + order.getProperty());
            }
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    }
}
