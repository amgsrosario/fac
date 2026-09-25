package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.ArtigoCreateDto;
import com.ar2lda.fac.controller.dto.ArtigoDto;
import com.ar2lda.fac.controller.dto.ArtigoUpdateDto;
import com.ar2lda.fac.service.ArtigoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/artigos")
@RequiredArgsConstructor
public class ArtigoController implements GenericController {

    private final ArtigoService service;

    @PostMapping
    public ResponseEntity<ArtigoDto> create(@RequestBody @Valid ArtigoCreateDto dto) {
        ArtigoDto created = service.create(dto);
        URI location = gerarHeaderLocation(created.codigo());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<ArtigoDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean inativo,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String unidade,
            @RequestParam(required = false) String ivaVendaId,
            Pageable pageable) {
        return service.list(search, inativo, codigo, descricao, unidade, ivaVendaId, validatedPageable(pageable));
    }

    private Pageable validatedPageable(Pageable pageable) {
        Set<String> allowed = Set.of("codigo", "descricao", "unidade", "pvp", "ivaVendaId", "inativo");
        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ordenacao nao suportada: " + order.getProperty());
            }
        }
        List<Sort.Order> orders = new ArrayList<>();
        pageable.getSort().forEach(order -> orders.add(new Sort.Order(
                order.getDirection(), "ivaVendaId".equals(order.getProperty()) ? "ivaVenda.id" : order.getProperty())));
        Sort sort = orders.isEmpty() ? Sort.by("codigo").ascending() : Sort.by(orders);
        if (sort.getOrderFor("codigo") == null) sort = sort.and(Sort.by("codigo").ascending());
        int size = pageable.getPageSize() == 50 ? 50 : 20;
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }

    @GetMapping("/{codigo}")
    public ArtigoDto getByCodigo(@PathVariable String codigo) {
        return service.getByCodigo(codigo);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void> update(@PathVariable String codigo, @RequestBody @Valid ArtigoUpdateDto dto) {
        service.update(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> delete(@PathVariable String codigo) {
        service.delete(codigo);
        return ResponseEntity.noContent().build();
    }
}
