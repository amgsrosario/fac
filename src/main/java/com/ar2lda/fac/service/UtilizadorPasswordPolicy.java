package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import java.util.Locale;
import java.util.Set;

/** Shared existing password rule; no change to CRUD semantics. */
public final class UtilizadorPasswordPolicy {
    private UtilizadorPasswordPolicy() { }
    private static final Set<String> PROIBIDAS=Set.of("password","password123!","admin123!","fac12345!","1234567890");
    public static void validate(String password,String codigo) {
        String lower=password.toLowerCase(Locale.ROOT);
        if(PROIBIDAS.contains(lower) || lower.contains(codigo.toLowerCase(Locale.ROOT)))
            throw new BadRequestException("A password escolhida é demasiado simples ou previsível");
    }
}
