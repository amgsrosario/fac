package com.ar2lda.fac.security;

import com.ar2lda.fac.exception.LoginLimitException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Clock;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Fixed windows, bounded state and no trust in forwarded headers. Per-instance only. */
@Component
public class LoginAttemptLimiter {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAttemptLimiter.class);
    private final Clock clock;
    private final int identifierLimit, addressLimit, globalLimit, capacity;
    private final long windowSeconds;
    private final Map<String, Bucket> buckets = new HashMap<>();
    private long windowStart = Long.MIN_VALUE;
    private int total;
    private long rejected;

    public LoginAttemptLimiter(Clock clock, Environment environment) {
        this.clock = clock;
        identifierLimit = setting(environment, "identifier-limit", 8, 1, 100);
        addressLimit = setting(environment, "address-limit", 60, 1, 1000);
        globalLimit = setting(environment, "global-limit", 600, 1, 10000);
        capacity = setting(environment, "capacity", 4096, 16, 16384);
        windowSeconds = setting(environment, "window-seconds", 60, 1, 3600);
    }

    public synchronized void check(String identifier, String remoteAddress) {
        long now = clock.instant().getEpochSecond();
        if (windowStart == Long.MIN_VALUE || now >= windowStart + windowSeconds) {
            if (rejected > 0) LOG.warn("Janela de proteccao do login concluida: {} pedidos recusados", rejected);
            buckets.clear(); total = 0; rejected = 0; windowStart = now;
        }
        String userKey = "u:" + identifier.trim().toLowerCase(Locale.ROOT);
        String ipKey = "i:" + remoteAddress;
        Bucket user = buckets.get(userKey), ip = buckets.get(ipKey);
        int newKeys = (user == null ? 1 : 0) + (ip == null ? 1 : 0);
        if (total >= globalLimit || user != null && user.count >= identifierLimit
                || ip != null && ip.count >= addressLimit || buckets.size() + newKeys > capacity) {
            if (rejected++ == 0) LOG.warn("Proteccao de login activa: pedidos excedentes recusados nesta janela");
            throw new LoginLimitException(Math.max(1, windowStart + windowSeconds - now));
        }
        buckets.computeIfAbsent(userKey, key -> new Bucket()).count++;
        buckets.computeIfAbsent(ipKey, key -> new Bucket()).count++;
        total++;
    }

    private int setting(Environment env, String key, int fallback, int min, int max) {
        int value = env.getProperty("fac.security.login." + key, Integer.class, fallback);
        if (value < min || value > max) throw new IllegalStateException("Limite de login invalido: " + key);
        return value;
    }
    private static class Bucket { private int count; }
}
