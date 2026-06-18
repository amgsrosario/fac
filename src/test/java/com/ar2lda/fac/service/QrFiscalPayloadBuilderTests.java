package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QrFiscalPayloadBuilderTests {

    private final QrFiscalValidator validator = new QrFiscalValidator();
    private final QrFiscalPayloadBuilder builder = new QrFiscalPayloadBuilder(validator);

    @Test
    void geraPayloadFiscalParaFaturaComIvaNormal() {
        String payload = builder.buildDocumentoComercial(impressaoComercial(), "YhGV", "9999");

        assertThat(payload).isEqualTo(
                "A:500000000*B:509999990*C:PT*D:FT*E:N*F:20260615*G:FT 2026/1*H:ABCD1234-1"
                        + "*I1:PT*I7:100.00*I8:23.00*N:23.00*O:123.00*Q:YhGV*R:9999"
        );
        assertThat(builder.payloadVersion()).isEqualTo("AT-QR-1.1");
    }

    @Test
    void usaConsumidorFinalQuandoNifDoClienteNaoExiste() {
        DocumentoComercialDto documento = documento(null);
        DocumentoComercialImpressaoDto impressao = new DocumentoComercialImpressaoDto(empresa(), documento, List.of());

        String payload = builder.buildDocumentoComercial(impressao, "YhGV", "9999");

        assertThat(payload).contains("B:999999990");
    }

    @Test
    void exigeHashECertificado() {
        DocumentoComercialImpressaoDto impressao = impressaoComercial();

        assertThatThrownBy(() -> builder.buildDocumentoComercial(impressao, null, "9999"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("4 caracteres do hash é obrigatório para gerar o QR fiscal");
        assertThatThrownBy(() -> builder.buildDocumentoComercial(impressao, "YhGV", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Número do certificado é obrigatório para gerar o QR fiscal");
    }

    private DocumentoComercialImpressaoDto impressaoComercial() {
        return new DocumentoComercialImpressaoDto(empresa(), documento("509999990"), List.of());
    }

    private EmpresaDto empresa() {
        return new EmpresaDto(
                1L,
                "FAC Lda",
                "500000000",
                "Rua da Empresa, 1",
                null,
                "3750-001",
                "Agueda",
                "PT",
                null,
                null,
                null,
                null,
                null,
                BigDecimal.ZERO,
                "CRC Agueda",
                "62010",
                "Programacao",
                "fac@example.pt",
                "https://fac.example.pt"
        );
    }

    private DocumentoComercialDto documento(String clienteNif) {
        return new DocumentoComercialDto(
                10L,
                "FT",
                "2026",
                1L,
                "ABCD1234-1",
                false,
                null,
                EstadoDocumentoComercial.EMITIDO,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 7, 15),
                1001L,
                null,
                1L,
                "EUR",
                "NOR",
                1,
                "P30",
                1,
                "Cliente Lda",
                clienteNif,
                "Rua do Cliente, 10",
                null,
                "3750-001",
                "Agueda",
                "PT",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 6, 15),
                null,
                null,
                "Armazem",
                "Rua do Armazem",
                null,
                "3750-001",
                "Agueda",
                "PT",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("100.000000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.000000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("23.000000"),
                BigDecimal.ZERO,
                new BigDecimal("23.000000"),
                new BigDecimal("123.000000"),
                null,
                null,
                null,
                "DEMO",
                false,
                false,
                false
        );
    }
}
