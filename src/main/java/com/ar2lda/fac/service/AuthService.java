package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.LoginRequestDto;
import com.ar2lda.fac.controller.dto.LoginResponseDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import com.ar2lda.fac.model.ResultadoAuditoria;
import com.ar2lda.fac.repository.UtilizadorRepository;
import com.ar2lda.fac.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilizadorRepository utilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaIsoladaService auditoriaIsoladaService;
    private final Clock clock;

    public LoginResponseDto login(LoginRequestDto request) {
        String username = request.username().trim();
        Utilizador utilizador = utilizadorRepository
                .findByCodigoIgnoreCaseOrEmailIgnoreCase(username, username).orElse(null);

        if (utilizador == null || utilizador.isInativo() || !passwordEncoder.matches(request.password(), utilizador.getPasswordHash())) {
            auditoriaIsoladaService.registar(TipoAuditoriaEvento.LOGIN_FALHADO, "AUTENTICACAO", username, utilizador,
                    ResultadoAuditoria.FALHA, null, "Login recusado", "{\"versao\":1}");
            throw invalidCredentials();
        }

        auditoriaIsoladaService.registar(TipoAuditoriaEvento.LOGIN_SUCESSO, "UTILIZADOR", utilizador.getCodigo(), utilizador,
                ResultadoAuditoria.SUCESSO, utilizador.getCodigo(), "Login efetuado", "{\"versao\":1}");
        utilizador.registarLogin(OffsetDateTime.now(clock));
        utilizadorRepository.save(utilizador);

        return new LoginResponseDto(
                jwtService.generate(utilizador),
                "Bearer",
                jwtService.expirationSeconds(),
                utilizador.getCodigo(),
                utilizador.getNome(),
                utilizador.getPapel().name(),
                utilizador.getPapel().permissoes().stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet())
        );
    }

    private BadRequestException invalidCredentials() {
        return new BadRequestException("Utilizador ou password invalidos");
    }
}
