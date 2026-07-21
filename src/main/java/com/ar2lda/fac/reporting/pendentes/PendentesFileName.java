package com.ar2lda.fac.reporting.pendentes;

import java.time.LocalDate;

final class PendentesFileName {

    private PendentesFileName() {
    }

    static String build(PendentesReportData data, String extension) {
        LocalDate date = data.dataReferencia() == null ? LocalDate.now() : data.dataReferencia();
        String prefix = data.pendentesAData() ? "pendentes-a-data" : "todos-pendentes";
        return "%s-%s.%s".formatted(prefix, date, extension);
    }
}
