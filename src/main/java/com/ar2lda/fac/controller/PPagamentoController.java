package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.PPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.PPagamentoDto;
import com.ar2lda.fac.controller.dto.PPagamentoUpdateDto;
import com.ar2lda.fac.service.PPagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/p-pagamentos")
@RequiredArgsConstructor
public class PPagamentoController implements GenericController {

    private final PPagamentoService service;

    @PostMapping
    public ResponseEntity<PPagamentoDto> create(@RequestBody @Valid PPagamentoCreateDto dto) {
        PPagamentoDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<PPagamentoDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public PPagamentoDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody @Valid PPagamentoUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
