package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ContaCorrenteClienteDiagnosticoDto;
import com.ar2lda.fac.controller.dto.ContaCorrentePendentePageDto;
import com.ar2lda.fac.controller.dto.PendenteDto;
import com.ar2lda.fac.service.PendenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/pendentes")
@RequiredArgsConstructor
public class PendenteController {

    private final PendenteService service;

    @GetMapping
    public Page<PendenteDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public PendenteDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/conta-corrente")
    public ContaCorrentePendentePageDto contaCorrente(
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "vencimento", defaultValue = "all") String vencimento,
            @RequestParam(name = "excluirLiquidados", defaultValue = "false") boolean excluirLiquidados,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.contaCorrente(clienteId, search, vencimento, excluirLiquidados, pageable);
    }

    @GetMapping("/clientes/{clienteId}/abertos")
    public List<PendenteDto> listAbertosPorCliente(
            @PathVariable Long clienteId,
            @RequestParam(name = "moedaId", required = false) String moedaId) {
        return service.listAbertosPorCliente(clienteId, moedaId);
    }

    @GetMapping("/conta-corrente/clientes/{clienteId}/diagnostico")
    public ContaCorrenteClienteDiagnosticoDto diagnosticoContaCorrenteCliente(@PathVariable Long clienteId) {
        return service.diagnosticoContaCorrenteCliente(clienteId);
    }
}
