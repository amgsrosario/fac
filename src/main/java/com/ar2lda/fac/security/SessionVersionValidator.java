package com.ar2lda.fac.security;

import com.ar2lda.fac.repository.UtilizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SessionVersionValidator implements OAuth2TokenValidator<Jwt> {
    private final UtilizadorRepository utilizadores;
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Object claim = jwt.getClaim("token_version");
        Long version = claim instanceof Integer || claim instanceof Long
                ? ((Number) claim).longValue()
                : null;
        var user = utilizadores.findById(jwt.getSubject()).orElse(null);
        return user != null && !user.isInativo() && version != null && version == user.getTokenVersion()
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
    }
}
