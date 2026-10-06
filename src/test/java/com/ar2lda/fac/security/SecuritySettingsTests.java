package com.ar2lda.fac.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import static org.assertj.core.api.Assertions.*;

class SecuritySettingsTests {
    private static final String VALID = "X7p6Q2v9L4b1N8d5R0c3H6z9M2s7F4k1"; // Synthetic fixture only.
    private MockEnvironment environment(String... profiles) {
        var env = new MockEnvironment(); env.setActiveProfiles(profiles); return env;
    }
    @Test void defaultEnabledAndOperationalBypassRejected() {
        assertThat(new SecuritySettings(environment("prod")).enabled()).isTrue();
        for (String[] profiles : new String[][]{{"prod"},{"demo"},{"dev"},{"test","prod"},{"test"}}) {
            var env = environment(profiles).withProperty("fac.security.enabled","false");
            assertThatThrownBy(() -> new SecuritySettings(env)).isInstanceOf(IllegalStateException.class);
        }
    }
    @Test void testBypassRequiresBothContextMarkerAndExclusiveProfile() {
        var env = environment("test").withProperty("fac.security.test-context","true").withProperty("fac.security.enabled","false");
        assertThat(new SecuritySettings(env).enabled()).isFalse();
        env.setActiveProfiles("test","demo");
        assertThatThrownBy(() -> new SecuritySettings(env)).isInstanceOf(IllegalStateException.class);
    }
    @Test void operationalSecretRequiredAndInvalidValuesNeverDisclosed() {
        var settings = new SecuritySettings(environment("prod"));
        for (String secret : new String[]{"", "short", "a".repeat(64), "change-me-" + VALID}) {
            assertThatThrownBy(() -> settings.validateSecret(secret)).isInstanceOf(IllegalStateException.class)
                    .hasMessageNotContaining(secret.isEmpty() ? "EMPTY_SENTINEL" : secret);
        }
        assertThatCode(() -> settings.validateSecret(VALID)).doesNotThrowAnyException();
        assertThatCode(() -> new SecuritySettings(environment("dev")).validateSecret("")).doesNotThrowAnyException();
        var implicitDev = new MockEnvironment(); implicitDev.setDefaultProfiles("dev");
        assertThatThrownBy(() -> new SecuritySettings(implicitDev).validateSecret(""))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test void invalidDurationRejected() {
        for (String duration : new String[]{"0","-1","1441","bad",""}) {
            assertThatThrownBy(() -> new SecuritySettings(environment("prod").withProperty("fac.security.jwt.expiration-minutes",duration)))
                    .isInstanceOf(RuntimeException.class);
        }
        assertThat(new SecuritySettings(environment("prod")).expirationMinutes()).isEqualTo(60);
    }
    @Test void contextFailsBeforeKeyAndValidSecretCreatesKey() {
        var runner = new ApplicationContextRunner().withUserConfiguration(KeyConfiguration.class)
                .withPropertyValues("spring.profiles.active=prod","fac.security.enabled=true");
        runner.withPropertyValues("fac.security.jwt.secret=").run(context -> assertThat(context).hasFailed());
        runner.withPropertyValues("fac.security.jwt.secret="+VALID).run(context -> assertThat(context).hasNotFailed().hasSingleBean(javax.crypto.SecretKey.class));
        runner.withPropertyValues("fac.security.enabled=false","fac.security.jwt.secret="+VALID)
                .run(context -> assertThat(context).hasFailed());
    }
    @Configuration(proxyBeanMethods=false) static class KeyConfiguration {
        @Bean SecuritySettings settings(Environment env) { return new SecuritySettings(env); }
        @Bean javax.crypto.SecretKey key(SecuritySettings settings, Environment env) {
            return new SecurityConfig().jwtSecretKey(env.getProperty("fac.security.jwt.secret",""),settings);
        }
    }
    @Test void unicodePasswordBytesAreBoundedWithoutAmbiguousTruncation() {
        var encoder = new BoundedBCryptPasswordEncoder();
        String boundary = "É".repeat(32)+"Ab1!xxxx";
        String hash = encoder.encode(boundary);
        assertThat(encoder.matches(boundary,hash)).isTrue();
        assertThatThrownBy(() -> encoder.encode(boundary+"é")).isInstanceOf(com.ar2lda.fac.exception.BadRequestException.class);
        assertThat(encoder.matches(boundary+"é",hash)).isFalse();
        assertThat(encoder.matches("incorrect",hash)).isFalse();
    }
}
