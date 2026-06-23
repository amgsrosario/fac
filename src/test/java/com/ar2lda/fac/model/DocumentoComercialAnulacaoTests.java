package com.ar2lda.fac.model;

import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static org.assertj.core.api.Assertions.*;

class DocumentoComercialAnulacaoTests {

    @Test
    void anularPreservaIdentificadoresFiscaisEGuardaSnapshotDoUtilizador() {
        DocumentoComercial documento = new DocumentoComercial();
        documento.setSerie("2026");
        documento.setNumeroDocumento(17L);
        documento.atribuirNumeroDocumentoCompleto("FT 2026/17");
        documento.atribuirAtcud("AT-CODE", "AT-CODE-17");
        documento.atribuirQrFiscal("A:500000000*B:123", "AT-QR-1.1");
        documento.setEstado(EstadoDocumentoComercial.EMITIDO);
        Utilizador utilizador = new Utilizador("ADMIN", "Ana Admin", "ana@example.test", "hash", false);
        OffsetDateTime momento = OffsetDateTime.parse("2026-06-23T09:00:00+01:00");

        documento.anular("Erro confirmado nos dados do cliente", utilizador, momento);

        assertThat(documento.getEstado()).isEqualTo(EstadoDocumentoComercial.ANULADO);
        assertThat(documento.isAnulado()).isTrue();
        assertThat(documento.getMotivoAnulacao()).isEqualTo("Erro confirmado nos dados do cliente");
        assertThat(documento.getDataHoraAnulacao()).isEqualTo(momento);
        assertThat(documento.getAnuladoPorUtilizadorId()).isEqualTo("ADMIN");
        assertThat(documento.getAnuladoPorNome()).isEqualTo("Ana Admin");
        assertThat(documento.getNumeroDocumento()).isEqualTo(17L);
        assertThat(documento.getNumeroDocumentoCompleto()).isEqualTo("FT 2026/17");
        assertThat(documento.getAtcud()).isEqualTo("AT-CODE-17");
        assertThat(documento.getQrPayload()).isEqualTo("A:500000000*B:123");
    }

    @Test
    void rascunhoEAnuladoNaoPodemSerAnulados() {
        Utilizador utilizador = new Utilizador("ADMIN", "Ana", "ana@example.test", "hash", false);
        DocumentoComercial rascunho = new DocumentoComercial();
        assertThatThrownBy(() -> rascunho.anular("Motivo valido", utilizador, OffsetDateTime.now()))
                .isInstanceOf(IllegalStateException.class);

        DocumentoComercial anulado = new DocumentoComercial();
        anulado.setEstado(EstadoDocumentoComercial.EMITIDO);
        anulado.anular("Primeira anulacao", utilizador, OffsetDateTime.now());
        assertThatThrownBy(() -> anulado.anular("Segunda anulacao", utilizador, OffsetDateTime.now()))
                .isInstanceOf(IllegalStateException.class);
    }
}
