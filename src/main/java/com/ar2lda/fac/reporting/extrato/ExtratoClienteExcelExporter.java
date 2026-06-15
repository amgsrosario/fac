package com.ar2lda.fac.reporting.extrato;

import com.ar2lda.fac.controller.dto.ExtratoClienteMoedaDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteMovimentoDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteTotaisDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ExtratoClienteExcelExporter {

    public static final String MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] HEADERS = {
            "Data", "Documento", "Descricao", "Vencimento", "Debito", "Credito", "Saldo"
    };

    private final ExtratoClienteReportDataService dataService;

    public ExportedExcel export(Long clienteId, java.time.LocalDate dataInicial, java.time.LocalDate dataFinal) {
        return export(dataService.getData(clienteId, dataInicial, dataFinal));
    }

    ExportedExcel export(ExtratoClienteReportData data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Styles styles = new Styles(workbook);
            for (int index = 0; index < data.extrato().moedas().size(); index++) {
                createSheet(workbook, data, data.extrato().moedas().get(index), index, styles);
            }
            workbook.write(output);
            return new ExportedExcel(ExtratoClienteFileName.build(data, "xlsx"), output.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel gerar o Excel do extrato de cliente", exception);
        }
    }

    private void createSheet(
            XSSFWorkbook workbook,
            ExtratoClienteReportData data,
            ExtratoClienteMoedaDto moeda,
            int index,
            Styles styles
    ) {
        String name = index == 0 ? "Extrato Cliente" : WorkbookUtil.createSafeSheetName("Extrato " + moeda.moedaId());
        Sheet sheet = workbook.createSheet(name);
        var extrato = data.extrato();
        var empresa = data.empresa();
        CurrencyStyles currencyStyles = styles.currency(workbook, moeda.moedaId());

        metadata(sheet, 0, "", "Extrato de Cliente", styles.title);
        metadata(sheet, 1, "Empresa", value(empresa.nome()) + " | NIF " + value(empresa.nif()), styles.label);
        metadata(sheet, 2, "Cliente", extrato.clienteId() + " - " + value(extrato.clienteNome()), styles.label);
        metadata(sheet, 3, "NIF", value(extrato.clienteNif()), styles.label);
        metadata(sheet, 4, "Periodo", extrato.dataInicial() + " a " + extrato.dataFinal(), styles.label);
        metadata(sheet, 5, "Filtros", "Cliente e periodo selecionados; ordenacao cronologica", styles.label);
        metadata(sheet, 6, "Emissao", DATE_TIME.format(extrato.geradoEm().toLocalDateTime()), styles.label);
        metadata(sheet, 7, "Moeda", moeda.moedaId(), styles.label);

        int headerRowIndex = 8;
        Row header = sheet.createRow(headerRowIndex);
        for (int column = 0; column < HEADERS.length; column++) {
            Cell cell = header.createCell(column);
            cell.setCellValue(HEADERS[column]);
            cell.setCellStyle(styles.header);
        }

        int rowIndex = headerRowIndex + 1;
        totalRow(sheet.createRow(rowIndex++), "Anterior", moeda.anterior(), currencyStyles.total);
        int firstMovementRow = rowIndex;
        for (ExtratoClienteMovimentoDto movimento : moeda.movimentos()) {
            movementRow(sheet.createRow(rowIndex++), movimento, styles, currencyStyles);
        }
        int lastMovementRow = rowIndex - 1;
        totalRow(sheet.createRow(rowIndex++), "Total do periodo", moeda.totalPeriodo(), currencyStyles.total);
        totalRow(sheet.createRow(rowIndex), "Total final", moeda.totalFinal(), currencyStyles.grandTotal);

        sheet.createFreezePane(0, headerRowIndex + 1);
        if (lastMovementRow >= firstMovementRow) {
            sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, lastMovementRow, 0, HEADERS.length - 1));
        }
        int[] widths = {12, 22, 48, 12, 16, 16, 16};
        for (int column = 0; column < widths.length; column++) {
            sheet.setColumnWidth(column, widths[column] * 256);
        }
        sheet.setRepeatingRows(CellRangeAddress.valueOf((headerRowIndex + 1) + ":" + (headerRowIndex + 1)));
    }

    private void metadata(Sheet sheet, int rowIndex, String label, String text, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(text);
        if (rowIndex == 0) {
            valueCell.setCellStyle(style);
        }
    }

    private void movementRow(Row row, ExtratoClienteMovimentoDto movimento, Styles styles, CurrencyStyles currencyStyles) {
        Cell date = row.createCell(0);
        date.setCellValue(movimento.data());
        date.setCellStyle(styles.date);
        row.createCell(1).setCellValue(documentReference(movimento));
        Cell description = row.createCell(2);
        description.setCellValue(value(movimento.descricao()));
        description.setCellStyle(styles.wrap);
        Cell dueDate = row.createCell(3);
        if (movimento.dataVencimento() != null) {
            dueDate.setCellValue(movimento.dataVencimento());
            dueDate.setCellStyle(styles.date);
        }
        number(row, 4, movimento.debito().doubleValue(), currencyStyles.money);
        number(row, 5, movimento.credito().doubleValue(), currencyStyles.money);
        number(row, 6, movimento.saldoAcumulado().doubleValue(), currencyStyles.money);
    }

    private void totalRow(Row row, String label, ExtratoClienteTotaisDto totals, CellStyle style) {
        Cell labelCell = row.createCell(2);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        number(row, 4, totals.debito().doubleValue(), style);
        number(row, 5, totals.credito().doubleValue(), style);
        number(row, 6, totals.saldo().doubleValue(), style);
    }

    private void number(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String documentReference(ExtratoClienteMovimentoDto movimento) {
        return "%s %s/%s".formatted(
                value(movimento.tipoDocumentoId()), value(movimento.serie()), value(movimento.numeroDocumento()));
    }

    private String value(Object raw) {
        return raw == null ? "" : String.valueOf(raw);
    }

    public record ExportedExcel(String filename, byte[] content) {
    }

    private static final class Styles {
        private final CellStyle title;
        private final CellStyle label;
        private final CellStyle header;
        private final CellStyle date;
        private final CellStyle wrap;

        private Styles(XSSFWorkbook workbook) {
            short dateFormat = workbook.createDataFormat().getFormat("dd/mm/yyyy");

            title = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            title.setFont(titleFont);

            label = workbook.createCellStyle();
            Font labelFont = workbook.createFont();
            labelFont.setBold(true);
            label.setFont(labelFont);

            header = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            header.setFont(headerFont);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setBorderBottom(BorderStyle.THIN);

            date = workbook.createCellStyle();
            date.setDataFormat(dateFormat);

            wrap = workbook.createCellStyle();
            wrap.setWrapText(true);
        }

        private CurrencyStyles currency(XSSFWorkbook workbook, String currency) {
            CellStyle money = workbook.createCellStyle();
            money.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"" + currency + "\""));
            money.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle total = workbook.createCellStyle();
            total.cloneStyleFrom(money);
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            total.setFont(totalFont);
            total.setBorderTop(BorderStyle.THIN);

            CellStyle grandTotal = workbook.createCellStyle();
            grandTotal.cloneStyleFrom(total);
            grandTotal.setBorderTop(BorderStyle.MEDIUM);
            return new CurrencyStyles(money, total, grandTotal);
        }
    }

    private record CurrencyStyles(CellStyle money, CellStyle total, CellStyle grandTotal) {
    }
}
