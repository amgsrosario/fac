package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ParametrosAplicacaoCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosAplicacaoDto;
import com.ar2lda.fac.controller.dto.ParametrosAplicacaoUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.ParametrosAplicacaoMapper;
import com.ar2lda.fac.model.ParametrosAplicacao;
import com.ar2lda.fac.repository.ParametrosAplicacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParametrosAplicacaoService {

    private final ParametrosAplicacaoRepository parametrosAplicacaoRepository;
    private final ParametrosAplicacaoMapper mapper;

    public ParametrosAplicacaoDto create(ParametrosAplicacaoCreateDto dto) {
        if (parametrosAplicacaoRepository.existsById(ParametrosAplicacao.PARAMETROS_ID)) {
            throw new ConflictException("Parâmetros da aplicação já existem");
        }
        ParametrosAplicacao entity = mapper.fromCreateDTO(dto);
        return mapper.toDTO(parametrosAplicacaoRepository.save(entity));
    }

    public ParametrosAplicacaoDto get() {
        return mapper.toDTO(findParametros());
    }

    public void update(ParametrosAplicacaoUpdateDto dto) {
        ParametrosAplicacao existing = findParametros();
        mapper.applyUpdate(dto, existing);
        parametrosAplicacaoRepository.save(existing);
    }

    private ParametrosAplicacao findParametros() {
        return parametrosAplicacaoRepository.findById(ParametrosAplicacao.PARAMETROS_ID)
                .orElseThrow(() -> new NotFoundException("Parâmetros da aplicação não encontrados"));
    }
}
