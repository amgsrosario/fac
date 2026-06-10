package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ContaCorrenteClienteDiagnosticoDto;
import com.ar2lda.fac.controller.dto.PendenteDto;
import com.ar2lda.fac.service.PendenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/conta-corrente/clientes/{clienteId}/diagnostico")
    public ContaCorrenteClienteDiagnosticoDto diagnosticoContaCorrenteCliente(@PathVariable Long clienteId) {
        return service.diagnosticoContaCorrenteCliente(clienteId);
    }
}
