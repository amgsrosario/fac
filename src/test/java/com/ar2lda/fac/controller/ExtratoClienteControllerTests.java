package com.ar2lda.fac.controller;

import com.ar2lda.fac.reporting.extrato.ExtratoClienteExcelExporter;
import com.ar2lda.fac.reporting.extrato.ExtratoClientePdfExporter;
import com.ar2lda.fac.service.ExtratoClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExtratoClienteControllerTests {

    private ExtratoClientePdfExporter pdfExporter;
    private ExtratoClienteExcelExporter excelExporter;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        pdfExporter = mock(ExtratoClientePdfExporter.class);
        excelExporter = mock(ExtratoClienteExcelExporter.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ExtratoClienteController(
                mock(ExtratoClienteService.class), pdfExporter, excelExporter)).build();
    }

    @Test
    void devolvePdfComoAnexo() throws Exception {
        LocalDate initial = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        when(pdfExporter.export(125L, initial, end)).thenReturn(new ExtratoClientePdfExporter.ExportedPdf(
                "extrato-cliente-000125-2026-01-01-2026-06-30.pdf", new byte[]{1, 2, 3}));

        mockMvc.perform(get("/extratos/clientes/125/exportar/pdf")
                        .param("dataInicial", "2026-01-01").param("dataFinal", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExtratoClientePdfExporter.MEDIA_TYPE))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("extrato-cliente-000125-2026-01-01-2026-06-30.pdf")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void devolveExcelComoAnexo() throws Exception {
        LocalDate initial = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        when(excelExporter.export(125L, initial, end)).thenReturn(new ExtratoClienteExcelExporter.ExportedExcel(
                "extrato-cliente-000125-2026-01-01-2026-06-30.xlsx", new byte[]{4, 5, 6}));

        mockMvc.perform(get("/extratos/clientes/125/exportar/xlsx")
                        .param("dataInicial", "2026-01-01").param("dataFinal", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExtratoClienteExcelExporter.MEDIA_TYPE))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("extrato-cliente-000125-2026-01-01-2026-06-30.xlsx")))
                .andExpect(content().bytes(new byte[]{4, 5, 6}));
    }
}
