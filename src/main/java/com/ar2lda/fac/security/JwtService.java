package com.ar2lda.fac.security;

import com.ar2lda.fac.model.Utilizador;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${fac.security.jwt.expiration-minutes:60}")
    private long expirationMinutes;

    public String generate(Utilizador utilizador) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("fac")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(utilizador.getCodigo())
                .claim("email", utilizador.getEmail())
                .claim("nome", utilizador.getNome())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expirationSeconds() {
        return expirationMinutes * 60;
    }
}
