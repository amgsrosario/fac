package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.MoedaCreateDto;
import com.ar2lda.fac.controller.dto.MoedaDto;
import com.ar2lda.fac.mapper.MoedaMapper;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.service.MoedaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/moedas")
@RequiredArgsConstructor
public class MoedaController implements GenericController {

    private final MoedaService service;
    private final MoedaMapper mapper;

    @PostMapping
    public ResponseEntity<MoedaDto> create(@RequestBody @Valid MoedaCreateDto dto) {
        Moeda entity = new Moeda(
                dto.id(),
                dto.nome(),
                dto.vcompra(),
                dto.vvenda(),
                dto.simbolo(),
                dto.ndecimais(),
                dto.ciso()
        );
        Moeda created = service.create(entity);
        MoedaDto body = mapper.toDTO(created);
        URI location = gerarHeaderLocation(created.getId());
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping
    public Page<MoedaDto> list(Pageable pageable) {
        return service.list(pageable).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    public MoedaDto getById(@PathVariable("id") String id) {
        return mapper.toDTO(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") String id,
                                       @RequestBody @Valid MoedaCreateDto dto) {
        Moeda update = new Moeda(
                id,
                dto.nome(),
                dto.vcompra(),
                dto.vvenda(),
                dto.simbolo(),
                dto.ndecimais(),
                dto.ciso()
        );
        service.update(id, update);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
