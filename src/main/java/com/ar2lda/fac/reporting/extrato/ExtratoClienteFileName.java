package com.ar2lda.fac.reporting.extrato;

final class ExtratoClienteFileName {

    private ExtratoClienteFileName() {
    }

    static String build(ExtratoClienteReportData data, String extension) {
        var extrato = data.extrato();
        String code = String.format("%06d", extrato.clienteId());
        String filename = "extrato-cliente-%s-%s-%s.%s".formatted(
                code, extrato.dataInicial(), extrato.dataFinal(), extension);
        return filename.replaceAll("[^A-Za-z0-9._-]", "-");
    }
}
