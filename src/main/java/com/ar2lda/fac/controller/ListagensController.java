package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.ListagemDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.ListagemLinhaComercialDto;
import com.ar2lda.fac.controller.dto.ListagemLinhaFinanceiraDto;
import com.ar2lda.fac.controller.dto.PendentesListagemDto;
import com.ar2lda.fac.service.ListagensService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/listagens")
@RequiredArgsConstructor
public class ListagensController {

    private final ListagensService service;

    @GetMapping("/documentos-comerciais")
    public Page<ListagemDocumentoComercialDto> documentosComerciais(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) Long clienteId,
            Pageable pageable
    ) {
        return service.documentosComerciais(dataInicial, dataFinal, clienteId, pageable);
    }

    @GetMapping("/linhas-comerciais")
    public Page<ListagemLinhaComercialDto> linhasComerciais(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String artigoId,
            Pageable pageable
    ) {
        return service.linhasComerciais(dataInicial, dataFinal, clienteId, artigoId, pageable);
    }

    @GetMapping("/documentos-financeiros")
    public Page<DocumentoFinanceiroDto> documentosFinanceiros(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) Long clienteId,
            Pageable pageable
    ) {
        return service.documentosFinanceiros(dataInicial, dataFinal, clienteId, pageable);
    }

    @GetMapping("/linhas-financeiras")
    public Page<ListagemLinhaFinanceiraDto> linhasFinanceiras(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) Long clienteId,
            Pageable pageable
    ) {
        return service.linhasFinanceiras(dataInicial, dataFinal, clienteId, pageable);
    }

    @GetMapping("/pendentes")
    public PendentesListagemDto pendentes(@RequestParam(required = false) List<Long> clienteIds) {
        return service.pendentes(clienteIds);
    }
}
