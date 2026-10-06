package com.ar2lda.fac.exception;

public class LoginLimitException extends RuntimeException {
    private final long retryAfter;
    public LoginLimitException(long retryAfter) {
        super("Demasiadas tentativas de login; tente novamente mais tarde");
        this.retryAfter = retryAfter;
    }
    public long retryAfter() { return retryAfter; }
}
