package com.ar2lda.fac;

import com.ar2lda.fac.controller.dto.ClienteDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoFinanceiroDto;
import com.ar2lda.fac.service.DocumentoFinanceiroPdfService;
import com.ar2lda.fac.service.DocumentoFinanceiroService;
import com.ar2lda.fac.service.FiscalQrService;
import com.ar2lda.fac.service.QrCodeImageService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoFinanceiroPdfServiceTests {

    @Mock
    private DocumentoFinanceiroService documentoService;

    @Mock
    private FiscalQrService fiscalQrService;

    private DocumentoFinanceiroPdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new DocumentoFinanceiroPdfService(documentoService, new QrCodeImageService(), fiscalQrService);
    }

    @Test
    void geraPdfDoReciboComAtcudEQrCodeEMarcaDocumentoComoImpresso() throws Exception {
        Long documentoId = 29L;
        EmpresaDto empresa = new EmpresaDto(1L, "FAC Lda", "500000000", "Rua da Empresa, 1", null,
                "3750-001", "Agueda", "PT", null, null, null, null, null,
                BigDecimal.ZERO, "CRC Agueda", "62010", "Programacao", "fac@example.pt", "https://fac.example.pt");
        ClienteDto cliente = new ClienteDto(1001L, "Cliente Lda", "Rua do Cliente, 10", null, "Agueda",
                "509999990", null, null, "cliente@example.pt", null, null, null, false, false, null,
                "3750-001", "PT", "EUR", 1001, "P30", "CON", 1001);
        LinhaDocumentoFinanceiroDto linha = new LinhaDocumentoFinanceiroDto(1L, 1, 10L,
                LocalDate.of(2026, 6, 8), LocalDate.of(2026, 7, 8), "FT", 9L, "2026",
                new BigDecimal("123.00"), new BigDecimal("123.00"), new BigDecimal("50.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("50.00"), new BigDecimal("73.00"), "EUR");
        String qrPayload = "A:500000000*B:509999990*C:PT*D:RC*E:N*F:20260609*G:RC 2026/3*H:ABCD1234-3*I1:PT*N:0.00*O:50.00*Q:YhGV*R:9999";
        DocumentoFinanceiroDto documento = new DocumentoFinanceiroDto(documentoId, 1001L, "RC", null, "2026", 3L,
                "ABCD1234-3", true, qrPayload,
                LocalDate.of(2026, 6, 9), "EUR", new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("50.00"), 1001, OffsetDateTime.parse("2026-06-09T10:00:00Z"), "DEMO",
                OffsetDateTime.parse("2026-06-09T10:00:00Z"), "Recebimento parcial", false, false, List.of(linha));
        when(documentoService.getImpressao(documentoId))
                .thenReturn(new DocumentoFinanceiroImpressaoDto(empresa, cliente, documento));

        DocumentoFinanceiroPdfService.PdfDocumento pdf = pdfService.gerar(documentoId);

        assertThat(pdf.filename()).isEqualTo("RC-2026-3.pdf");
        assertThat(new String(pdf.content(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(pdf.content().length).isGreaterThan(1_000);
        try (PDDocument pdfDocument = Loader.loadPDF(pdf.content())) {
            String text = new PDFTextStripper().getText(pdfDocument);
            assertThat(text).contains("ATCUD");
            assertThat(text).contains("ABCD1234-3");
        }
        verify(documentoService).marcarComoImpresso(documentoId);
    }
}
