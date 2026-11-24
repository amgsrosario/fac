package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtilizadorPasswordPolicyTests {
    private static final String RETIRED_EXAMPLE = "admin123";
    private static final String USER_CODE = "TSTOP918";

    @Test
    void rejectsRetiredExampleRegardlessOfCase() {
        assertRejected(RETIRED_EXAMPLE);
        assertRejected(RETIRED_EXAMPLE.toUpperCase(Locale.ROOT));
    }

    @Test
    void acceptsSimilarValuesOutsideTheDenylist() {
        assertThatCode(() -> UtilizadorPasswordPolicy.validate("Z9!" + RETIRED_EXAMPLE, USER_CODE))
                .doesNotThrowAnyException();
        assertThatCode(() -> UtilizadorPasswordPolicy.validate(RETIRED_EXAMPLE + "!8Q", USER_CODE))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"password", "password123!", "admin123!", "fac12345!", "1234567890"})
    void stillRejectsExistingForbiddenValues(String value) {
        assertRejected(value);
    }

    private static void assertRejected(String value) {
        assertThatThrownBy(() -> UtilizadorPasswordPolicy.validate(value, USER_CODE))
                .isInstanceOf(BadRequestException.class);
    }
}
