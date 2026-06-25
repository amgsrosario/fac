package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.UtilizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UtilizadorRepository utilizadorRepository;

    @Value("${fac.security.enabled:true}")
    private boolean securityEnabled;

    public Utilizador resolve(String fallbackCodigo, String operation) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String codigo = securityEnabled && authentication != null && authentication.isAuthenticated()
                ? authentication.getName()
                : fallbackCodigo;

        if (codigo == null || codigo.isBlank()) {
            throw new BadRequestException("Utilizador autenticado e obrigatorio para " + operation);
        }

        Utilizador utilizador = utilizadorRepository.findById(codigo)
                .orElseThrow(() -> new NotFoundException("Utilizador nao encontrado: " + codigo));
        if (utilizador.isInativo()) {
            throw new BadRequestException("Utilizador inativo nao pode " + operation);
        }
        return utilizador;
    }

    public String currentCodeOrSystem() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())
                ? authentication.getName()
                : "SISTEMA";
    }

    public Utilizador currentUserOrNull() {
        String codigo = currentCodeOrSystem();
        return "SISTEMA".equals(codigo) ? null : utilizadorRepository.findById(codigo).orElse(null);
    }
}
