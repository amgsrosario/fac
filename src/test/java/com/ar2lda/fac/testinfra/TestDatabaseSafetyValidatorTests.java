package com.ar2lda.fac.testinfra;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDatabaseSafetyValidatorTests {

    @Test
    void permiteNomeDaBaseNormalizado() {
        assertThatCode(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"test"}, "  FAC_TEST  ", true
        )).doesNotThrowAnyException();
    }

    @Test
    void permitePerfilTestEmMaiusculasEntreOutrosPerfis() {
        assertThatCode(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"dev", "TEST"}, "fac_test", true
        )).doesNotThrowAnyException();
    }

    @Test
    void recusaPerfisNulos() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                null, "fac_test", true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("perfil Spring 'test'");
    }

    @Test
    void recusaPerfisVazios() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                new String[]{}, "fac_test", true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("perfil Spring 'test'");
    }

    @Test
    void recusaBaseDeDemonstracao() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"test"}, "fac_demo", true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não termina em '_test'");
    }

    @Test
    void recusaNomeDaBaseNulo() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"test"}, null, true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não termina em '_test'");
    }

    @Test
    void recusaNomeDaBaseVazio() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"test"}, "", true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não termina em '_test'");
    }

    @Test
    void recusaNomeDaBaseApenasComEspacos() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"test"}, "   ", true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não termina em '_test'");
    }

    @Test
    void permiteBaseDeTesteComPerfilEAutorizacaoExplicitos() {
        assertThatCode(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"test"}, "fac_test", true
        )).doesNotThrowAnyException();
    }

    @Test
    void recusaSemPerfilTest() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"dev"}, "fac_test", true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("perfil Spring 'test'");
    }

    @Test
    void recusaBaseDeDesenvolvimento() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"test"}, "fac", true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não termina em '_test'");
    }

    @Test
    void recusaQuandoAutorizacaoDestrutivaEstaDesativada() {
        assertThatThrownBy(() -> TestDatabaseSafetyValidator.validate(
                new String[]{"test"}, "fac_test", false
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("autorização destrutiva");
    }
}

