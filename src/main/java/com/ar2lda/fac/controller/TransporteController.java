package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.TransporteDto;
import com.ar2lda.fac.mapper.TransporteMapper;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.service.TransporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/transportes")
@RequiredArgsConstructor
public class TransporteController implements GenericController{

    private final TransporteService service;
    private final TransporteMapper mapper;


    @PostMapping
    //@ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> salvar(@RequestBody @Valid TransporteDto dto){
        Transporte transporte=mapper.toEntity(dto);
        Transporte salvo=service.salvar(transporte);
        URI location=gerarHeaderLocationInteger(salvo.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransporteDto>obterDetalhes(@PathVariable("id") Integer id){
        return service
                .porId(id)
                .map(mapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable("id") Integer id,
            @RequestBody @Valid TransporteDto dto
    ){
        return service.porId(id)
                .<ResponseEntity<Void>>map(transporte ->{
                    transporte.alterarNome(dto.nome()); // ou dto.getNome()
                    service.atualizar(transporte);
                    return ResponseEntity.noContent().build();

                })
                .orElseGet(()->ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
   public ResponseEntity<Void> remover(@PathVariable("id") Integer id){
        Optional<Transporte> transporte=service.porId(id);
        if(transporte.isEmpty()){
            return ResponseEntity.notFound().build();
        }
       service.eliminar(transporte.get());
        return ResponseEntity.noContent().build();
   }


}
