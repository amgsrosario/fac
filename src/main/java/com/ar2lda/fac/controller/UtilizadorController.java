package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.UtilizadorCreateDto;
import com.ar2lda.fac.controller.dto.UtilizadorDto;
import com.ar2lda.fac.controller.dto.UtilizadorUpdateDto;
import com.ar2lda.fac.controller.dto.UtilizadorEstadoDto;
import com.ar2lda.fac.controller.dto.UtilizadorPerfilDto;
import com.ar2lda.fac.controller.dto.UtilizadorPasswordResetDto;
import com.ar2lda.fac.model.PapelUtilizador;
import com.ar2lda.fac.service.UtilizadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/utilizadores")
@RequiredArgsConstructor
public class UtilizadorController implements GenericController {

    private final UtilizadorService service;

    @PostMapping
    public ResponseEntity<UtilizadorDto> create(@RequestBody @Valid UtilizadorCreateDto dto) {
        UtilizadorDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.codigo());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<UtilizadorDto> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) PapelUtilizador papel,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable) {
        return service.list(q, papel, ativo, pageable);
    }

    @GetMapping("/{codigo}")
    public UtilizadorDto getByCodigo(@PathVariable String codigo) {
        return service.getByCodigo(codigo);
    }

    @PutMapping("/{codigo}")
    public UtilizadorDto update(@PathVariable String codigo, @RequestBody @Valid UtilizadorUpdateDto dto) {
        return service.update(codigo, dto);
    }

    @PatchMapping("/{codigo}/estado")
    public UtilizadorDto alterarEstado(@PathVariable String codigo, @RequestBody @Valid UtilizadorEstadoDto dto) {
        return service.alterarEstado(codigo, dto);
    }

    @PatchMapping("/{codigo}/perfil")
    public UtilizadorDto alterarPerfil(@PathVariable String codigo, @RequestBody @Valid UtilizadorPerfilDto dto) {
        return service.alterarPerfil(codigo, dto);
    }

    @PostMapping("/{codigo}/redefinir-password")
    public ResponseEntity<Void> redefinirPassword(@PathVariable String codigo,
                                                   @RequestBody @Valid UtilizadorPasswordResetDto dto) {
        service.redefinirPassword(codigo, dto);
        return ResponseEntity.noContent().build();
    }
}
