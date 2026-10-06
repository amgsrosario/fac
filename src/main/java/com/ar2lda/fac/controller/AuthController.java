package com.ar2lda.fac.controller;

import com.ar2lda.fac.controller.dto.LoginRequestDto;
import com.ar2lda.fac.controller.dto.LoginResponseDto;
import com.ar2lda.fac.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.ar2lda.fac.security.LoginAttemptLimiter limiter;

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody @Valid LoginRequestDto request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        limiter.check(request.username(), httpRequest.getRemoteAddr());
        return authService.login(request);
    }
}
