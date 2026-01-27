package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.PPagamentoCreateDto;
import com.ar2lda.fac.controller.dto.PPagamentoDto;
import com.ar2lda.fac.controller.dto.PPagamentoUpdateDto;
import com.ar2lda.fac.mapper.PPagamentoMapper;
import com.ar2lda.fac.model.PPagamento;
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
    private final PPagamentoMapper mapper;

    @PostMapping
    public ResponseEntity<PPagamentoDto> create(@RequestBody @Valid PPagamentoCreateDto dto) {
        PPagamento toSave = mapper.fromCreate(dto);
        PPagamento created = service.create(toSave);
        PPagamentoDto body = mapper.toDTO(created);
        URI location = gerarHeaderLocation(created.getId());
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping
    public Page<PPagamentoDto> list(Pageable pageable) {
        return service.list(pageable).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    public PPagamentoDto getById(@PathVariable("id") Integer id) {
        return mapper.toDTO(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Integer id,
                                       @RequestBody @Valid PPagamentoUpdateDto dto) {
        PPagamento update = new PPagamento();
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
