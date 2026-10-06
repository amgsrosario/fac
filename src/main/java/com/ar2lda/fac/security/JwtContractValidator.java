package com.ar2lda.fac.security;

import com.ar2lda.fac.model.PermissaoFuncional;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.Collection;

/** Runs before repository validation; malformed claims never reach identity lookup. */
class JwtContractValidator implements OAuth2TokenValidator<Jwt> {
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Object subject = jwt.getClaims().get("sub");
        Object expiry = jwt.getClaims().get("exp");
        Object issuer = jwt.getClaims().get("iss");
        Object version = jwt.getClaims().get("token_version");
        Object authorities = jwt.getClaims().get("authorities");
        boolean valid = subject instanceof String s && !s.isBlank() && s.length() <= 20
                && expiry instanceof Instant && "fac".equals(issuer)
                && (version instanceof Integer || version instanceof Long) && ((Number) version).longValue() >= 0
                && authorities instanceof Collection<?>;
        if (valid) for (Object authority : (Collection<?>) authorities) {
            if (!(authority instanceof String)) { valid = false; break; }
            try { PermissaoFuncional.valueOf((String) authority); }
            catch (IllegalArgumentException exception) { valid = false; break; }
        }
        return valid ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token invalido", null));
    }
}
