package com.ar2lda.fac.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoFiscalQrTests {

    @Test
    void documentoComercialAtribuiQrFiscalUmaUnicaVez() {
        DocumentoComercial documento = new DocumentoComercial();

        documento.atribuirQrFiscal(" A:500000000*H:ABCD1234-1 ", " AT-QR-1 ");

        assertThat(documento.getQrPayload()).isEqualTo("A:500000000*H:ABCD1234-1");
        assertThat(documento.getQrPayloadVersion()).isEqualTo("AT-QR-1");
        assertThat(documento.temQrFiscal()).isTrue();
        assertThatThrownBy(() -> documento.atribuirQrFiscal("A:500000000*H:ABCD1234-2", "AT-QR-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("O payload QR do documento comercial já foi atribuído");
    }

    @Test
    void documentoFinanceiroAtribuiQrFiscalUmaUnicaVez() {
        DocumentoFinanceiro documento = new DocumentoFinanceiro();

        documento.atribuirQrFiscal(" A:500000000*H:ABCD1234-1 ", " AT-QR-1 ");

        assertThat(documento.getQrPayload()).isEqualTo("A:500000000*H:ABCD1234-1");
        assertThat(documento.getQrPayloadVersion()).isEqualTo("AT-QR-1");
        assertThat(documento.temQrFiscal()).isTrue();
        assertThatThrownBy(() -> documento.atribuirQrFiscal("A:500000000*H:ABCD1234-2", "AT-QR-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("O payload QR do documento financeiro já foi atribuído");
    }

    @Test
    void qrFiscalExigePayloadEVersao() {
        DocumentoComercial documento = new DocumentoComercial();

        assertThat(documento.temQrFiscal()).isFalse();
        assertThatThrownBy(() -> documento.atribuirQrFiscal(" ", "AT-QR-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payload e versão do QR fiscal são obrigatórios");
        assertThatThrownBy(() -> documento.atribuirQrFiscal("A:500000000", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payload e versão do QR fiscal são obrigatórios");
    }
}
