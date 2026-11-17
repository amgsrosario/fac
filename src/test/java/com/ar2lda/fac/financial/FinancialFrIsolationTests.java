package com.ar2lda.fac.financial;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class FinancialFrIsolationTests {
    @Test void adapterAbsentFromDefaultApplicationContext() {
        new ApplicationContextRunner().withUserConfiguration(FinancialFrIssuer.class)
            .run(context->assertThat(context).doesNotHaveBean(FinancialFrIssuer.class));
    }
    @Test void disabledLegacyPermissionGateCannotAdmitUnauthenticatedActor() {
        var db=org.mockito.Mockito.mock(org.springframework.jdbc.core.JdbcTemplate.class);
        var users=org.mockito.Mockito.mock(com.ar2lda.fac.service.CurrentUserService.class);
        var permissions=org.mockito.Mockito.mock(com.ar2lda.fac.security.FunctionalAuthorization.class);
        var issuer=new FinancialFrIssuer(db,null,null,null,null,null,users,permissions);
        try {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            assertThatThrownBy(()->issuer.issue("synthetic",null)).hasMessageContaining("Authenticated");
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated("synthetic",""));
            assertThatThrownBy(()->issuer.issue("synthetic",null)).hasMessageContaining("Authenticated");
            org.mockito.Mockito.verifyNoInteractions(db,users);
        } finally {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
    }
}
