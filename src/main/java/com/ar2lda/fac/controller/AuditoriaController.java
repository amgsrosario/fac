package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.AuditoriaEventoDto;
import com.ar2lda.fac.model.ResultadoAuditoria;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import com.ar2lda.fac.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {
    private final AuditoriaService service;

    @GetMapping
    @PreAuthorize("@functionalAuthorization.has('AUDITORIA_CONSULTAR')")
    public Page<AuditoriaEventoDto> consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime ate,
            @RequestParam(required = false) TipoAuditoriaEvento tipoEvento,
            @RequestParam(required = false) String entidadeTipo,
            @RequestParam(required = false) String entidadeId,
            @RequestParam(required = false) String utilizadorId,
            @RequestParam(required = false) ResultadoAuditoria resultado,
            Pageable pageable) {
        return service.consultar(desde, ate, tipoEvento, entidadeTipo, entidadeId, utilizadorId, resultado, pageable);
    }
}
