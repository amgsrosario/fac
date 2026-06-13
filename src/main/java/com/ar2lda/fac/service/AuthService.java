package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.LoginRequestDto;
import com.ar2lda.fac.controller.dto.LoginResponseDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.UtilizadorRepository;
import com.ar2lda.fac.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilizadorRepository utilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDto login(LoginRequestDto request) {
        String username = request.username().trim();
        Utilizador utilizador = utilizadorRepository
                .findByCodigoIgnoreCaseOrEmailIgnoreCase(username, username)
                .orElseThrow(this::invalidCredentials);

        if (utilizador.isInativo() || !passwordEncoder.matches(request.password(), utilizador.getPasswordHash())) {
            throw invalidCredentials();
        }

        return new LoginResponseDto(
                jwtService.generate(utilizador),
                "Bearer",
                jwtService.expirationSeconds(),
                utilizador.getCodigo(),
                utilizador.getNome()
        );
    }

    private BadRequestException invalidCredentials() {
        return new BadRequestException("Utilizador ou password invalidos");
    }
}
