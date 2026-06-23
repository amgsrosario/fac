package com.ar2lda.fac.service;

import com.ar2lda.fac.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditoriaIsoladaService {
    private final AuditoriaService auditoriaService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registar(TipoAuditoriaEvento tipo, String entidadeTipo, Object entidadeId, Utilizador utilizador,
                         ResultadoAuditoria resultado, String referencia, String descricao, String dadosEssenciais) {
        auditoriaService.registarComo(tipo, entidadeTipo, entidadeId, utilizador, resultado, referencia, descricao, dadosEssenciais);
    }
}
