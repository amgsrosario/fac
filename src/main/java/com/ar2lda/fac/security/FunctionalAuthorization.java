package com.ar2lda.fac.security;

import com.ar2lda.fac.model.PermissaoFuncional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("functionalAuthorization")
public class FunctionalAuthorization {

    @Value("${fac.security.enabled:true}")
    private boolean securityEnabled;

    public boolean has(String permissao) {
        if (!securityEnabled) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permissao));
    }

    public void require(PermissaoFuncional permissao) {
        if (!has(permissao.name())) {
            throw new AccessDeniedException("Permissao funcional necessaria: " + permissao.name());
        }
    }
}
