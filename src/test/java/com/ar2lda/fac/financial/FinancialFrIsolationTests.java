package com.ar2lda.fac.financial;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class FinancialFrIsolationTests {
    @Test void adapterAbsentFromDefaultApplicationContext() {
        new ApplicationContextRunner().withUserConfiguration(FinancialFrIssuer.class)
            .run(context->assertThat(context).doesNotHaveBean(FinancialFrIssuer.class));
    }
}
