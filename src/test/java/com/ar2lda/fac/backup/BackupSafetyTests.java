package com.ar2lda.fac.backup;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BackupSafetyTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-25T15:30:00Z"), ZoneOffset.UTC);

    @Test
    void refusesEmptyFacAndSystemDatabases() {
        assertThatThrownBy(() -> BackupSafety.validateSourceDatabase(""))
                .hasMessageContaining("obrigatorio");
        assertThatThrownBy(() -> BackupSafety.validateSourceDatabase("fac"))
                .hasMessageContaining("proibida");
        assertThatThrownBy(() -> BackupSafety.validateSourceDatabase("postgres"))
                .hasMessageContaining("proibida");
        assertThatThrownBy(() -> BackupSafety.validateSourceDatabase("template1"))
                .hasMessageContaining("proibida");
    }

    @Test
    void refusesWildcardsAndInvalidNames() {
        assertThatThrownBy(() -> BackupSafety.validateSourceDatabase("fac_*"))
                .hasMessageContaining("invalido");
        assertThatThrownBy(() -> BackupSafety.validateSourceDatabase("1fac"))
                .hasMessageContaining("invalido");
    }

    @Test
    void allowsControlledSourcesAndRestoreTargets() {
        BackupSafety.validateSourceDatabase("fac_demo");
        BackupSafety.validateSourceDatabase("fac_test");
        BackupSafety.validateRestoreTarget("fac_restore_test");
    }

    @Test
    void refusesUnauthorizedRestoreTargets() {
        assertThatThrownBy(() -> BackupSafety.validateRestoreTarget("fac"))
                .hasMessageContaining("nao autorizado");
        assertThatThrownBy(() -> BackupSafety.validateRestoreTarget("cliente_real"))
                .hasMessageContaining("nao autorizado");
    }

    @Test
    void generatesSafeBackupAndMetadataNames() {
        String backup = BackupSafety.backupFileName("fac", "demo", "fac_demo", "abc123 dirty", clock);
        assertThat(backup).isEqualTo("fac_demo_fac_demo_2026-06-25_153000_abc123-dirty.backup");
        assertThat(BackupSafety.metadataFileName(backup))
                .isEqualTo("fac_demo_fac_demo_2026-06-25_153000_abc123-dirty.metadata.json");
    }
}
