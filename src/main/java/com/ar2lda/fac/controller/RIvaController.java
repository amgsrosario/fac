package com.ar2lda.fac.controller;


import com.ar2lda.fac.controller.dto.RIvaDto;
import com.ar2lda.fac.mapper.RIvaMapper;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.service.RIvaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/riva")
@RequiredArgsConstructor
public class RIvaController implements GenericController{
    private final RIvaService service;
    private final RIvaMapper mapper;

    @PostMapping
    public ResponseEntity<Void> salvar(@RequestBody @Valid RIvaDto dto) {

        RIva rIva= mapper.toEntity(dto);
        RIva salvo=service.salvar(rIva);
        URI location= gerarHeaderLocation(salvo.getId());
        return ResponseEntity.created(location).build();
    }

    public ResponseEntity<Void> atualizar(@RequestBody @Valid RIvaDto dto){
        return ResponseEntity.noContent().build();
    }
}
