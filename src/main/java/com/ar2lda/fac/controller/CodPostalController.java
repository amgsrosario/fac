package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.CodPostalCreateDto;
import com.ar2lda.fac.controller.dto.CodPostalDto;
import com.ar2lda.fac.controller.dto.CodPostalUpdateDto;
import com.ar2lda.fac.mapper.CodPostalMapper;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.service.CodPostalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/cod-postais")
@RequiredArgsConstructor
public class CodPostalController implements GenericController {

    private final CodPostalService service;
    private final CodPostalMapper mapper;

    @PostMapping
    public ResponseEntity<CodPostalDto> create(@RequestBody @Valid CodPostalCreateDto dto) {
        CodPostal entity = mapper.fromCreate(dto);
        CodPostal created = service.create(entity);
        CodPostalDto body = mapper.toDTO(created);
        URI location = gerarHeaderLocation(created.getId());
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping
    public Page<CodPostalDto> list(Pageable pageable) {
        return service.list(pageable).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    public CodPostalDto getById(@PathVariable("id") String id) {
        return mapper.toDTO(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") String id,
                                       @RequestBody @Valid CodPostalUpdateDto dto) {
        service.update(id, dto.nome());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
