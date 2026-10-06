package com.ar2lda.fac.security;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

/** Validates configuration before a security chain or signing key is created. */
@Component
public class SecuritySettings {
    private final boolean enabled;
    private final boolean nonOperational;
    private final long expirationMinutes;

    public SecuritySettings(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        boolean explicitDev = Arrays.equals(profiles, new String[]{"dev"});
        if (profiles.length == 0) profiles = environment.getDefaultProfiles();
        boolean testContext = Arrays.equals(profiles, new String[]{"test"})
                && environment.getProperty("fac.security.test-context", Boolean.class, false)
                && ClassUtils.isPresent("com.ar2lda.fac.testinfra.TestDatabaseSafetyValidator", getClass().getClassLoader());
        nonOperational = testContext || explicitDev;
        enabled = environment.getProperty("fac.security.enabled", Boolean.class, true);
        if (!enabled && !testContext) throw new IllegalStateException("Seguranca desactivada apenas permitida no contexto de testes");
        try { expirationMinutes = Long.parseLong(environment.getProperty("fac.security.jwt.expiration-minutes", "60").trim()); }
        catch (NumberFormatException exception) { throw new IllegalStateException("Duracao JWT deve ser um numero inteiro entre 1 e 1440"); }
        if (expirationMinutes < 1 || expirationMinutes > 1440)
            throw new IllegalStateException("Duracao JWT deve estar entre 1 e 1440 minutos");
    }

    public boolean enabled() { return enabled; }
    public long expirationMinutes() { return expirationMinutes; }

    public void validateSecret(String secret) {
        if (nonOperational && secret.isBlank()) return;
        // Length is only a floor: generation with a CSPRNG remains an operational obligation.
        String lower = secret.toLowerCase(Locale.ROOT);
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32 || secret.chars().distinct().count() < 12
                || secret.chars().anyMatch(Character::isWhitespace)
                || lower.contains("changeme") || lower.contains("change-me") || lower.contains("replace")
                || lower.contains("example") || lower.contains("your-secret"))
            throw new IllegalStateException("Secret JWT inadequado: gerar valor aleatorio com pelo menos 256 bits; nao usar placeholders");
    }
}
