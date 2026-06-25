package com.ar2lda.fac.security;

import com.ar2lda.fac.model.ResultadoAuditoria;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.UtilizadorRepository;
import com.ar2lda.fac.service.AuditoriaIsoladaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AdministrativeAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditoriaIsoladaService auditoriaIsoladaService;
    private final UtilizadorRepository utilizadorRepository;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (isAdministrativePath(request.getRequestURI())) {
            Utilizador utilizador = currentUser();
            TipoAuditoriaEvento evento = isImportExportPath(request.getRequestURI())
                    ? TipoAuditoriaEvento.TENTATIVA_IMPORTACAO_NEGADA
                    : TipoAuditoriaEvento.TENTATIVA_ADMINISTRATIVA_NEGADA;
            auditoriaIsoladaService.registar(evento,
                    "ADMIN", request.getRequestURI(), utilizador, ResultadoAuditoria.FALHA,
                    request.getMethod(), "Tentativa administrativa recusada", "{\"versao\":1}");
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Permissao funcional insuficiente\"}");
    }

    private boolean isAdministrativePath(String uri) {
        return uri != null && (uri.startsWith("/utilizadores") || uri.startsWith("/empresa")
                || uri.startsWith("/auditoria") || isImportExportPath(uri));
    }

    private boolean isImportExportPath(String uri) {
        return uri != null && (uri.startsWith("/importacoes") || uri.startsWith("/exportacoes"));
    }

    private Utilizador currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return utilizadorRepository.findById(authentication.getName()).orElse(null);
    }
}
