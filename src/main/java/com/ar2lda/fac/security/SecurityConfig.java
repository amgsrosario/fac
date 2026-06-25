package com.ar2lda.fac.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecretKey jwtSecretKey(@Value("${fac.security.jwt.secret:}") String configuredSecret) {
        byte[] keyBytes = configuredSecret.isBlank()
                ? randomKey()
                : sha256(configuredSecret);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${fac.security.enabled:true}") boolean securityEnabled,
            AdministrativeAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (securityEnabled) {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                            .requestMatchers("/actuator/health").permitAll()
                            .requestMatchers("/utilizadores/**", "/empresa/**").hasAuthority("CONFIGURACAO_GERIR")
                            .requestMatchers(HttpMethod.POST, "/documentos-financeiros/*/anular").hasAuthority("DOCUMENTO_ANULAR")
                            .requestMatchers(HttpMethod.POST, "/documentos-financeiros").hasAuthority("TESOURARIA_GERIR")
                            .requestMatchers(HttpMethod.POST, "/clientes", "/artigos").hasAuthority("MESTRES_GERIR")
                            .requestMatchers(HttpMethod.PUT, "/clientes/**", "/artigos/**").hasAuthority("MESTRES_GERIR")
                            .requestMatchers(HttpMethod.DELETE, "/clientes/**", "/artigos/**").hasAuthority("MESTRES_GERIR")
                            .requestMatchers(HttpMethod.POST, configurationPaths()).hasAuthority("CONFIGURACAO_GERIR")
                            .requestMatchers(HttpMethod.PUT, configurationPaths()).hasAuthority("CONFIGURACAO_GERIR")
                            .requestMatchers(HttpMethod.DELETE, configurationPaths()).hasAuthority("CONFIGURACAO_GERIR")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth -> oauth
                            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                            .authenticationEntryPoint((request, response, exception) -> {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Autenticacao necessaria ou expirada\"}");
                            })
                            .accessDeniedHandler(accessDeniedHandler));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }
        return http.build();
    }

    private String[] configurationPaths() {
        return new String[]{
                "/empresa/**", "/parametros-aplicacao/**", "/parametros-cliente/**",
                "/parametros-documento-comercial/**", "/utilizadores/**", "/armazens/**",
                "/codpostal/**", "/familias/**", "/freguesias/**", "/iva-saft/**",
                "/motivos-isencao/**", "/moedas/**", "/mpagamentos/**", "/paises/**",
                "/p-pagamentos/**", "/riva/**", "/tipos-documento/**",
                "/tipos-taxa-iva/**", "/transportes/**"
        };
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("authorities");
        authorities.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    private byte[] randomKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel", exception);
        }
    }
}
