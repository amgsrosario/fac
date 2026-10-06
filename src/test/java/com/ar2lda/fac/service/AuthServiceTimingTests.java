package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.LoginRequestDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.UtilizadorRepository;
import com.ar2lda.fac.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Clock;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class AuthServiceTimingTests {
    @Test void allInvalidCredentialPathsPerformOnePasswordCheckWithoutExposingCredentials() {
        var users=mock(UtilizadorRepository.class); var encoder=mock(PasswordEncoder.class);
        var audit=mock(AuditoriaIsoladaService.class);
        when(encoder.encode(any())).thenReturn("synthetic-dummy-hash");
        when(encoder.matches(any(),any())).thenReturn(false);
        when(users.findByCodigoIgnoreCaseOrEmailIgnoreCase("missing","missing")).thenReturn(Optional.empty());
        when(users.findByCodigoIgnoreCaseOrEmailIgnoreCase("inactive","inactive")).thenReturn(Optional.of(new Utilizador("inactive","Inactive","i@test", "stored-hash",true)));
        when(users.findByCodigoIgnoreCaseOrEmailIgnoreCase("active","active")).thenReturn(Optional.of(new Utilizador("active","Active","a@test", "stored-hash",false)));
        var service=new AuthService(users,encoder,mock(JwtService.class),audit,Clock.systemUTC());
        service.initialiseDummyHash();
        for(String username : new String[]{"missing","inactive","active"})
            assertThatThrownBy(() -> service.login(new LoginRequestDto(username,"Synthetic1!")))
                    .isInstanceOf(BadRequestException.class).hasMessage("Utilizador ou password invalidos");
        verify(encoder,times(2)).matches("Synthetic1!","synthetic-dummy-hash");
        verify(encoder).matches("Synthetic1!","stored-hash");
        verify(users,never()).save(any());
    }
}
