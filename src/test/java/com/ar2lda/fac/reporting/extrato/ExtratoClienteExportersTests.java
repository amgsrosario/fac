package com.ar2lda.fac.reporting.extrato;

import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteMoedaDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteMovimentoDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteTotaisDto;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExtratoClienteExportersTests {

    @Test
    void geraExcelComEstruturaTiposFormatacaoEValoresOficiais() throws Exception {
        ExtratoClienteReportData data = reportData(movements(2));
        var exporter = new ExtratoClienteExcelExporter(mock(ExtratoClienteReportDataService.class));

        var file = exporter.export(data);

        assertThat(file.filename()).isEqualTo("extrato-cliente-000125-2026-01-01-2026-06-30.xlsx");
        assertThat(file.content()).isNotEmpty();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file.content()))) {
            var sheet = workbook.getSheet("Extrato Cliente");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Extrato de Cliente");
            assertThat(sheet.getRow(8).getCell(0).getStringCellValue()).isEqualTo("Data");
            assertThat(sheet.getRow(9).getCell(2).getStringCellValue()).isEqualTo("Anterior");
            assertThat(sheet.getRow(10).getCell(0).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(DateUtil.isCellDateFormatted(sheet.getRow(10).getCell(0))).isTrue();
            assertThat(sheet.getRow(10).getCell(4).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(sheet.getRow(10).getCell(4).getCellStyle().getDataFormatString()).contains("0.00");
            assertThat(sheet.getRow(12).getCell(2).getStringCellValue()).isEqualTo("Total do periodo");
            assertThat(sheet.getRow(13).getCell(2).getStringCellValue()).isEqualTo("Total final");
            assertThat(sheet.getRow(13).getCell(6).getNumericCellValue()).isEqualTo(105d);
            assertThat(sheet.getPaneInformation()).isNotNull();
            assertThat(sheet.getPaneInformation().isFreezePane()).isTrue();
            assertThat(sheet.getCTWorksheet().isSetAutoFilter()).isTrue();
            assertThat(sheet.getColumnWidth(2)).isGreaterThan(sheet.getColumnWidth(0));
        }
    }

    @Test
    void geraExcelSemMovimentosSemFormulaInvalida() throws Exception {
        var exporter = new ExtratoClienteExcelExporter(mock(ExtratoClienteReportDataService.class));
        var file = exporter.export(reportData(List.of()));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file.content()))) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(10).getCell(2).getStringCellValue()).isEqualTo("Total do periodo");
            assertThat(sheet.getRow(11).getCell(2).getStringCellValue()).isEqualTo("Total final");
            sheet.forEach(row -> row.forEach(cell -> assertThat(cell.getCellType()).isNotEqualTo(CellType.FORMULA)));
        }
    }

    @Test
    void geraPdfA4MultipaginaComCabecalhosTotaisECaracteresPortugueses() throws Exception {
        var exporter = new ExtratoClientePdfExporter(mock(ExtratoClienteReportDataService.class));
        var file = exporter.export(reportData(movements(90)));

        assertThat(file.filename()).isEqualTo("extrato-cliente-000125-2026-01-01-2026-06-30.pdf");
        assertThat(file.content()).isNotEmpty();
        try (var document = Loader.loadPDF(file.content())) {
            assertThat(document.getNumberOfPages()).isGreaterThan(1);
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Extrato de Cliente", "Cliente Açores", "Anterior", "Total do periodo", "Total final");
            assertThat(text).contains("Descrição portuguesa");
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                PDFTextStripper pageStripper = new PDFTextStripper();
                pageStripper.setStartPage(page);
                pageStripper.setEndPage(page);
                assertThat(pageStripper.getText(document)).contains("Data", "Saldo");
            }
        }
    }

    @Test
    void pdfEExcelRecebemOsMesmosMovimentosETotaisDaResposta() throws Exception {
        ExtratoClienteReportData data = reportData(movements(2));
        var excel = new ExtratoClienteExcelExporter(mock(ExtratoClienteReportDataService.class)).export(data);
        var pdf = new ExtratoClientePdfExporter(mock(ExtratoClienteReportDataService.class)).export(data);

        assertThat(data.extrato().moedas().getFirst().movimentos()).hasSize(2);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel.content()));
             var document = Loader.loadPDF(pdf.content())) {
            assertThat(workbook.getSheetAt(0).getRow(13).getCell(6).getNumericCellValue())
                    .isEqualTo(data.extrato().moedas().getFirst().totalFinal().saldo().doubleValue());
            String pdfText = new PDFTextStripper().getText(document);
            assertThat(pdfText).contains("105,00");
            data.extrato().moedas().getFirst().movimentos()
                    .forEach(movement -> assertThat(pdfText).contains(movement.descricao()));
        }
    }

    private ExtratoClienteReportData reportData(List<ExtratoClienteMovimentoDto> movements) {
        EmpresaDto empresa = new EmpresaDto(
                1L, "FAC Portugal, Lda.", "509000001", "Rua Central", null, "1000-001", "Lisboa", "PT",
                null, null, null, null, null, new BigDecimal("5000"), "Registo 1", "62010",
                "Programacao", "geral@fac.pt", "https://fac.pt"
        );
        ExtratoClienteTotaisDto anterior = totals("100", "25", "75");
        BigDecimal debitoPeriodo = movements.stream().map(ExtratoClienteMovimentoDto::debito)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditoPeriodo = movements.stream().map(ExtratoClienteMovimentoDto::credito)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ExtratoClienteTotaisDto periodo = new ExtratoClienteTotaisDto(
                debitoPeriodo, creditoPeriodo, debitoPeriodo.subtract(creditoPeriodo));
        ExtratoClienteTotaisDto finalTotals = new ExtratoClienteTotaisDto(
                anterior.debito().add(periodo.debito()), anterior.credito().add(periodo.credito()),
                anterior.saldo().add(periodo.saldo()));
        ExtratoClienteDto extrato = new ExtratoClienteDto(
                125L, "Cliente Açores", "500000125", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                OffsetDateTime.parse("2026-06-30T18:30:00Z"),
                List.of(new ExtratoClienteMoedaDto("EUR", anterior, movements, periodo, finalTotals))
        );
        return new ExtratoClienteReportData(empresa, extrato);
    }

    private List<ExtratoClienteMovimentoDto> movements(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ExtratoClienteMovimentoDto(
                        (long) index + 1, index % 2 == 0 ? "COMERCIAL" : "FINANCEIRO",
                        LocalDate.of(2026, 1, 1).plusDays(index), OffsetDateTime.parse("2026-01-01T10:00:00Z").plusDays(index),
                        index % 2 == 0 ? "FT" : "RC", "2026", (long) index + 1,
                        "Descrição portuguesa " + (index + 1), LocalDate.of(2026, 2, 1).plusDays(index),
                        index % 2 == 0 ? new BigDecimal("50") : BigDecimal.ZERO,
                        index % 2 == 0 ? BigDecimal.ZERO : new BigDecimal("20"),
                        new BigDecimal("75").add(new BigDecimal("30").multiply(BigDecimal.valueOf(index / 2L)))
                )).toList();
    }

    private ExtratoClienteTotaisDto totals(String debit, String credit, String balance) {
        return new ExtratoClienteTotaisDto(new BigDecimal(debit), new BigDecimal(credit), new BigDecimal(balance));
    }
}
