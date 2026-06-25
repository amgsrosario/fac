package com.ar2lda.fac.backup;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class BackupSafety {
    private static final Pattern DATABASE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");
    private static final Pattern SAFE_TOKEN = Pattern.compile("[A-Za-z0-9_.-]+");
    private static final Set<String> FORBIDDEN_DATABASES = Set.of("fac", "postgres", "template0", "template1");
    private static final Set<String> ALLOWED_RESTORE_TARGETS = Set.of("fac_restore_test", "fac_demo", "fac_test");
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    private BackupSafety() {
    }

    public static void validateSourceDatabase(String database) {
        validateDatabaseName(database);
        String normalized = database.toLowerCase(Locale.ROOT);
        if (FORBIDDEN_DATABASES.contains(normalized)) {
            throw new IllegalArgumentException("base de dados proibida para backup: " + database);
        }
    }

    public static void validateRestoreTarget(String database) {
        validateDatabaseName(database);
        String normalized = database.toLowerCase(Locale.ROOT);
        if (FORBIDDEN_DATABASES.contains(normalized) || !ALLOWED_RESTORE_TARGETS.contains(normalized)) {
            throw new IllegalArgumentException("destino de restauro nao autorizado: " + database);
        }
    }

    public static String backupFileName(String project, String environment, String database, String version, Clock clock) {
        validateSafeToken(project, "projeto");
        validateSafeToken(environment, "ambiente");
        validateSourceDatabase(database);
        String timestamp = LocalDateTime.now(clock).format(BACKUP_TIMESTAMP);
        String suffix = version == null || version.isBlank() ? "" : "_" + sanitizeVersion(version);
        return project + "_" + environment + "_" + database + "_" + timestamp + suffix + ".backup";
    }

    public static String metadataFileName(String backupFileName) {
        if (backupFileName == null || !backupFileName.endsWith(".backup")) {
            throw new IllegalArgumentException("ficheiro de backup deve terminar em .backup");
        }
        return backupFileName.substring(0, backupFileName.length() - ".backup".length()) + ".metadata.json";
    }

    private static void validateDatabaseName(String database) {
        if (database == null || database.isBlank()) {
            throw new IllegalArgumentException("nome da base de dados e obrigatorio");
        }
        if (database.contains("*") || database.contains("?") || !DATABASE_NAME.matcher(database).matches()) {
            throw new IllegalArgumentException("nome da base de dados invalido: " + database);
        }
    }

    private static void validateSafeToken(String value, String label) {
        if (value == null || value.isBlank() || !SAFE_TOKEN.matcher(value).matches()) {
            throw new IllegalArgumentException(label + " invalido");
        }
    }

    private static String sanitizeVersion(String version) {
        String sanitized = version.trim().replaceAll("[^A-Za-z0-9_.-]", "-");
        return sanitized.length() > 32 ? sanitized.substring(0, 32) : sanitized;
    }
}
