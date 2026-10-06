package com.ar2lda.fac.security;

import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.charset.StandardCharsets;

/** Avoids ambiguous BCrypt truncation; preserves the existing ASCII policy. */
class BoundedBCryptPasswordEncoder extends BCryptPasswordEncoder {
    @Override public String encode(CharSequence password) {
        if (tooLong(password)) throw new BadRequestException("Password nao pode exceder 72 bytes UTF-8");
        return super.encode(password);
    }
    @Override public boolean matches(CharSequence password, String hash) {
        return !tooLong(password) && super.matches(password, hash);
    }
    private boolean tooLong(CharSequence password) {
        return password != null && password.toString().getBytes(StandardCharsets.UTF_8).length > 72;
    }
}
