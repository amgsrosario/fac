package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class AtcudService {

    public String gerar(String codigoValidacaoAt, Long numeroSequencial) {
        if (codigoValidacaoAt == null || codigoValidacaoAt.isBlank()) {
            throw new BadRequestException("O código de validação da AT é obrigatório");
        }
        if (numeroSequencial == null || numeroSequencial <= 0) {
            throw new BadRequestException("O número sequencial deve ser superior a zero");
        }

        String codigoNormalizado = codigoValidacaoAt.trim();
        if (codigoNormalizado.regionMatches(true, 0, "ATCUD:", 0, "ATCUD:".length())) {
            throw new BadRequestException("O código de validação da AT não pode incluir o prefixo ATCUD:");
        }

        return codigoNormalizado + "-" + numeroSequencial;
    }
}
