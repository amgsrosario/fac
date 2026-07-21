package com.ar2lda.fac.reporting.pendentes;

import com.ar2lda.fac.controller.dto.PendenteListagemDto;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PendentesExcelExporter {

    public static final String MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] HEADERS = {"Cliente", "Documento", "Data", "Vencimento", "Total", "Recebido", "Pendente"};

    private final PendentesReportDataService dataService;

    public ExportedExcel exportPendentes(List<Long> clienteIds) {
        return export(dataService.pendentes(clienteIds));
    }

    public ExportedExcel exportPendentesAData(LocalDate dataReferencia, List<Long> clienteIds) {
        return export(dataService.pendentesAData(dataReferencia, clienteIds));
    }

    ExportedExcel export(PendentesReportData data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Styles styles = new Styles(workbook);
            Sheet sheet = workbook.createSheet(data.titulo());
            metadata(sheet, 0, "", data.titulo(), styles.title);
            metadata(sheet, 1, "Empresa", value(data.empresa().nome()) + " | NIF " + value(data.empresa().nif()), styles.label);
            metadata(sheet, 2, "Filtros", data.filtros(), styles.label);
            metadata(sheet, 3, "Emissao", DATE_TIME.format(data.geradoEm().toLocalDateTime()), styles.label);
            if (data.dataReferencia() != null) {
                metadata(sheet, 4, "Data de referência", String.valueOf(data.dataReferencia()), styles.label);
            }

            int headerRowIndex = 6;
            Row header = sheet.createRow(headerRowIndex);
            for (int column = 0; column < HEADERS.length; column++) {
                Cell cell = header.createCell(column);
                cell.setCellValue(HEADERS[column]);
                cell.setCellStyle(styles.header);
            }

            int rowIndex = headerRowIndex + 1;
            for (PendenteListagemDto linha : data.pendentes().linhas()) {
                lineRow(sheet.createRow(rowIndex++), linha, styles);
            }
            totalRow(sheet.createRow(rowIndex), data, styles.total);

            sheet.createFreezePane(0, headerRowIndex + 1);
            if (!data.pendentes().linhas().isEmpty()) {
                sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, rowIndex - 1, 0, HEADERS.length - 1));
            }
            int[] widths = {34, 18, 12, 12, 16, 16, 16};
            for (int column = 0; column < widths.length; column++) {
                sheet.setColumnWidth(column, widths[column] * 256);
            }
            workbook.write(output);
            return new ExportedExcel(PendentesFileName.build(data, "xlsx"), output.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel gerar o Excel de pendentes", exception);
        }
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

    private void lineRow(Row row, PendenteListagemDto linha, Styles styles) {
        row.createCell(0).setCellValue("%d - %s".formatted(linha.clienteId(), value(linha.clienteNome())));
        row.createCell(1).setCellValue(value(linha.documento()));
        date(row, 2, linha.data(), styles.date);
        date(row, 3, linha.vencimento(), styles.date);
        number(row, 4, linha.total(), styles.money);
        number(row, 5, linha.recebido(), styles.money);
        number(row, 6, linha.pendente(), styles.money);
    }

    private void totalRow(Row row, PendentesReportData data, CellStyle style) {
        Cell label = row.createCell(1);
        label.setCellValue("Totais");
        label.setCellStyle(style);
        number(row, 4, data.pendentes().totais().total(), style);
        number(row, 5, data.pendentes().totais().recebido(), style);
        number(row, 6, data.pendentes().totais().pendente(), style);
    }

    private void date(Row row, int column, LocalDate value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void number(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue((value == null ? BigDecimal.ZERO : value).doubleValue());
        cell.setCellStyle(style);
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
        private final CellStyle money;
        private final CellStyle total;

        private Styles(XSSFWorkbook workbook) {
            short dateFormat = workbook.createDataFormat().getFormat("dd/mm/yyyy");
            short moneyFormat = workbook.createDataFormat().getFormat("#,##0.00");

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

            money = workbook.createCellStyle();
            money.setDataFormat(moneyFormat);
            money.setAlignment(HorizontalAlignment.RIGHT);

            total = workbook.createCellStyle();
            total.cloneStyleFrom(money);
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            total.setFont(totalFont);
            total.setBorderTop(BorderStyle.MEDIUM);
        }
    }
}
