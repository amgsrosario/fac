package com.ar2lda.fac.testinfra;

import java.util.Arrays;
import java.util.Locale;

public final class TestDatabaseSafetyValidator {

    private TestDatabaseSafetyValidator() {
    }

    public static void validate(String[] activeProfiles, String databaseName, boolean destructiveOperationsEnabled) {
        boolean testProfileActive = activeProfiles != null
                && Arrays.stream(activeProfiles).anyMatch("test"::equalsIgnoreCase);
        if (!testProfileActive) {
            throw new IllegalStateException(
                    "Operações de teste recusadas: o perfil Spring 'test' não está ativo"
            );
        }
        if (!destructiveOperationsEnabled) {
            throw new IllegalStateException(
                    "Operações de teste recusadas: a autorização destrutiva está desativada"
            );
        }
        String normalizedDatabaseName = databaseName == null
                ? ""
                : databaseName.trim().toLowerCase(Locale.ROOT);
        if (!normalizedDatabaseName.endsWith("_test")) {
            throw new IllegalStateException(
                    "Operações de teste recusadas: a base atual não termina em '_test'"
            );
        }
    }
}

