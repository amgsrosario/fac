package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ExtratoClienteDto;
import com.ar2lda.fac.service.ExtratoClienteService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{clienteId}")
    public ExtratoClienteDto getExtrato(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        return service.getExtrato(clienteId, dataInicial, dataFinal);
    }
}
