package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.MIsencaoCreateDto;
import com.ar2lda.fac.controller.dto.MIsencaoDto;
import com.ar2lda.fac.controller.dto.MIsencaoUpdateDto;
import com.ar2lda.fac.service.MIsencaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/motivos-isencao")
@RequiredArgsConstructor
public class MIsencaoController implements GenericController {

    private final MIsencaoService service;

    @PostMapping
    public ResponseEntity<MIsencaoDto> create(@RequestBody @Valid MIsencaoCreateDto dto) {
        MIsencaoDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<MIsencaoDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public MIsencaoDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody @Valid MIsencaoUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
