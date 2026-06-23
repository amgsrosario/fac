package com.ar2lda.fac.demo;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DemoResetSafetyTests {
    @Test void aceitaApenasBaseDemoExplicitamenteAutorizada() {
        assertThatCode(() -> DemoResetSafety.validate("fac_demo", "fac_demo", true)).doesNotThrowAnyException();
    }
    @Test void recusaDesenvolvimentoProducaoNomeErradoOuSemAutorizacao() {
        assertThatThrownBy(() -> DemoResetSafety.validate("fac", "fac_demo", true)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> DemoResetSafety.validate("fac_prod", "fac_demo", true)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> DemoResetSafety.validate("fac_demo", "fac_demo", false)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> DemoResetSafety.validate("outra_demo", "outra_demo", true)).isInstanceOf(IllegalStateException.class);
    }
}
