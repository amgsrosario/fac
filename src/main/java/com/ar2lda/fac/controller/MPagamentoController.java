package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.MPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.MPagamentoDto;
import com.ar2lda.fac.controller.dto.MPagamentoUpdateDto;
import com.ar2lda.fac.mapper.MPagamentoMapper;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.service.MPagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/mpagamentos")
@RequiredArgsConstructor
public class MPagamentoController implements GenericController {

    private final MPagamentoService service;
    private final MPagamentoMapper mapper;

    @PostMapping
    public ResponseEntity<MPagamentoDto> create(@RequestBody @Valid MPagamentoCreateDto dto) {
        MPagamento toSave = mapper.fromCreate(dto);
        MPagamento created = service.create(toSave);
        MPagamentoDto body = mapper.toDTO(created);
        URI location = gerarHeaderLocation(created.getId());
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping
    public Page<MPagamentoDto> list(Pageable pageable) {
        return service.list(pageable).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    public MPagamentoDto getById(@PathVariable("id") Integer id) {
        return mapper.toDTO(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Integer id,
                                       @RequestBody @Valid MPagamentoUpdateDto dto) {
        MPagamento update = new MPagamento();
        mapper.applyUpdate(dto, update);
        service.update(id, update);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
