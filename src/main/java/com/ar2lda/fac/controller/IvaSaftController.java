package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.IvaSaftCreateDto;
import com.ar2lda.fac.controller.dto.IvaSaftDto;
import com.ar2lda.fac.controller.dto.IvaSaftUpdateDto;
import com.ar2lda.fac.mapper.IvaSaftMapper;
import com.ar2lda.fac.model.IvaSaft;
import com.ar2lda.fac.service.IvaSaftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/iva-saft")
@RequiredArgsConstructor
public class IvaSaftController implements GenericController {

    private final IvaSaftService service;
    private final IvaSaftMapper mapper;

    @PostMapping
    public ResponseEntity<IvaSaftDto> create(@RequestBody @Valid IvaSaftCreateDto dto) {
        IvaSaftDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<IvaSaftDto> list(Pageable pageable) {
        return service.list(pageable).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    public IvaSaftDto getById(@PathVariable("id") String id) {
        IvaSaft e = service.getById(id);
        return mapper.toDTO(e);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") String id,
                                       @RequestBody @Valid IvaSaftUpdateDto dto) {
        service.update(id, dto.nome());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
