package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.RIvaCreateDto;
import com.ar2lda.fac.controller.dto.RIvaDto;
import com.ar2lda.fac.controller.dto.RIvaUpdateDto;
import com.ar2lda.fac.service.RIvaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/riva")
@RequiredArgsConstructor
public class RIvaController implements GenericController {

    private final RIvaService service;

    @PostMapping
    public ResponseEntity<RIvaDto> create(@RequestBody @Valid RIvaCreateDto dto) {
        RIvaDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<RIvaDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public RIvaDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody @Valid RIvaUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
