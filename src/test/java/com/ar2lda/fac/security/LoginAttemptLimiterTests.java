package com.ar2lda.fac.security;

import com.ar2lda.fac.exception.LoginLimitException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import java.time.*;
import static org.assertj.core.api.Assertions.*;

class LoginAttemptLimiterTests {
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now; }
    }
    @Test void identifierCaseAndAddressesCannotBypassLimitAndWindowRecovers() {
        var clock = new MutableClock(); var limiter = new LoginAttemptLimiter(clock,new MockEnvironment());
        for (int i=0;i<8;i++) limiter.check("User", "ip"+i);
        assertThatThrownBy(() -> limiter.check(" USER ","other")).isInstanceOf(LoginLimitException.class);
        clock.now = clock.now.plusSeconds(60);
        assertThatCode(() -> limiter.check("user","other")).doesNotThrowAnyException();
    }
    @Test void sharedAddressIsBoundedAcrossIdentifiers() {
        var env = new MockEnvironment().withProperty("fac.security.login.address-limit","3");
        var limiter = new LoginAttemptLimiter(new MutableClock(),env);
        for (int i=0;i<3;i++) limiter.check("user"+i,"one-address");
        assertThatThrownBy(() -> limiter.check("new-user","one-address")).isInstanceOf(LoginLimitException.class);
    }
    @Test void randomKeysCannotEvictBlockedStateOrGrowStorage() {
        var clock = new MutableClock();
        var env = new MockEnvironment().withProperty("fac.security.login.capacity","16");
        var limiter = new LoginAttemptLimiter(clock,env);
        for (int i=0;i<8;i++) limiter.check("u"+i,"ip"+i);
        for (int i=0;i<100;i++) {
            int index=i;
            assertThatThrownBy(() -> limiter.check("random"+index,"random"+index)).isInstanceOf(LoginLimitException.class);
        }
        var state=(java.util.Map<?,?>)org.springframework.test.util.ReflectionTestUtils.getField(limiter,"buckets");
        assertThat(state).hasSize(16);
        clock.now=clock.now.plusSeconds(60); limiter.check("new","new"); assertThat(state).hasSize(2);
    }
    @Test void globalBoundAndInvalidConfiguration() {
        var env=new MockEnvironment().withProperty("fac.security.login.global-limit","2");
        var limiter=new LoginAttemptLimiter(new MutableClock(),env);
        limiter.check("u1","ip1");limiter.check("u2","ip2");
        assertThatThrownBy(() -> limiter.check("u3","ip3")).isInstanceOf(LoginLimitException.class);
        assertThatThrownBy(() -> new LoginAttemptLimiter(new MutableClock(),new MockEnvironment().withProperty("fac.security.login.capacity","0")))
                .isInstanceOf(IllegalStateException.class);
    }
}
