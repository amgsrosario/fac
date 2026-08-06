package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.DashboardComercialDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.service.DashboardComercialService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardComercialController {

    private final DashboardComercialService service;

    @GetMapping("/comercial")
    public DashboardComercialDto comercial(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        if (dataInicio.isAfter(dataFim)) {
            throw new BadRequestException("A data inicial nao pode ser posterior a data final");
        }
        return service.consultar(dataInicio, dataFim);
    }
}
