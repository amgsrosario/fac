package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.IvaSaftCreateDto;
import com.ar2lda.fac.controller.dto.IvaSaftDto;
import com.ar2lda.fac.controller.dto.IvaSaftUpdateDto;
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

    @PostMapping
    public ResponseEntity<IvaSaftDto> create(@RequestBody @Valid IvaSaftCreateDto dto) {
        IvaSaftDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<IvaSaftDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public IvaSaftDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody @Valid IvaSaftUpdateDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
