package com.ar2lda.fac.reporting.listagens;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.ListagemDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.ListagemLinhaComercialDto;
import com.ar2lda.fac.controller.dto.ListagemLinhaFinanceiraDto;
import com.ar2lda.fac.service.ListagensService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListagemTabularExporter {
    public static final String PDF_MEDIA_TYPE = "application/pdf";
    public static final String XLSX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int EXPORT_LIMIT = 100_000;

    private final ListagensService service;

    public ExportedFile export(String source, String format, LocalDate inicio, LocalDate fim,
                               List<Long> clienteIds, List<String> artigoIds,
                               boolean mostrarAnulados, boolean mostrarTexto) {
        Report report = report(source, inicio, fim, clienteIds, artigoIds, mostrarAnulados, mostrarTexto);
        return switch (format) {
            case "pdf" -> new ExportedFile(fileName(source, inicio, fim, "pdf"), PDF_MEDIA_TYPE, pdf(report));
            case "xlsx" -> new ExportedFile(fileName(source, inicio, fim, "xlsx"), XLSX_MEDIA_TYPE, excel(report));
            default -> throw new IllegalArgumentException("Formato de exportacao nao suportado");
        };
    }

    private Report report(String source, LocalDate inicio, LocalDate fim, List<Long> clientes,
                          List<String> artigos, boolean anulados, boolean texto) {
        var page = PageRequest.of(0, EXPORT_LIMIT);
        List<List<String>> rows = new ArrayList<>();
        return switch (source) {
            case "documentos-comerciais" -> {
                for (ListagemDocumentoComercialDto item : service.documentosComerciais(inicio, fim, null, clientes, anulados, page)) {
                    var d = item.documento();
                    rows.add(List.of(ref(d.tipoDocumentoCodigoFiscal(), d.tipoDocumentoId(), d.serie(), d.numeroDocumento()), date(d.dataEmissao()), value(d.clienteNome()), value(d.clienteNif()), value(d.moedaId()), value(d.valorTotal()), value(d.estado())));
                }
                yield new Report("Documentos comerciais", List.of("Documento", "Data", "Cliente", "NIF", "Moeda", "Total", "Estado"), rows);
            }
            case "linhas-comerciais" -> {
                for (ListagemLinhaComercialDto item : service.linhasComerciais(inicio, fim, null, clientes, null, artigos, texto, page)) {
                    var d = item.documento(); var l = item.linha();
                    rows.add(List.of(ref(d.tipoDocumentoCodigoFiscal(), d.tipoDocumentoId(), d.serie(), d.numeroDocumento()), date(d.dataEmissao()), value(d.clienteNome()), value(l.numeroLinha()), value(l.artigoId()), value(l.descricao()), value(l.quantidade()), value(l.valorLinha())));
                }
                yield new Report("Detalhe dos documentos comerciais", List.of("Documento", "Data", "Cliente", "Linha", "Artigo", "Descricao", "Quantidade", "Valor"), rows);
            }
            case "documentos-financeiros" -> {
                for (DocumentoFinanceiroDto d : service.documentosFinanceiros(inicio, fim, null, clientes, anulados, page)) {
                    rows.add(List.of(ref(d.tipoDocumentoCodigoFiscal(), d.tipoDocumentoId(), d.serie(), d.numeroDocumento()), date(d.dataEmissao()), value(d.clienteId()), value(d.mPagamentoId()), value(d.valorPagamentoLiquido()), d.anulado() ? "ANULADO" : "EMITIDO"));
                }
                yield new Report("Documentos financeiros", List.of("Documento", "Data", "Cliente", "Modo", "Recebido", "Estado"), rows);
            }
            case "linhas-financeiras" -> {
                for (ListagemLinhaFinanceiraDto item : service.linhasFinanceiras(inicio, fim, null, clientes, page)) {
                    var d = item.documento(); var l = item.linha();
                    rows.add(List.of(ref(d.tipoDocumentoCodigoFiscal(), d.tipoDocumentoId(), d.serie(), d.numeroDocumento()), ref(l.tipoDocumentoId(), l.tipoDocumentoId(), l.serieDocumento(), l.numeroDocumento()), date(d.dataEmissao()), value(l.valorPendenteAntes()), value(l.valorALiquidar()), value(l.valorPagamentoLiquido()), value(l.novoValorPendente())));
                }
                yield new Report("Detalhe dos documentos financeiros", List.of("Recebimento", "Documento liquidado", "Data", "Pendente antes", "Valor liquidado", "Recebido", "Novo pendente"), rows);
            }
            default -> throw new IllegalArgumentException("Listagem nao suportada");
        };
    }

    private byte[] excel(Report report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Listagem");
            Font bold = workbook.createFont(); bold.setBold(true);
            CellStyle title = workbook.createCellStyle(); title.setFont(bold);
            CellStyle header = workbook.createCellStyle(); header.setFont(bold); header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Row titleRow = sheet.createRow(0); titleRow.createCell(0).setCellValue(report.title()); titleRow.getCell(0).setCellStyle(title);
            Row headers = sheet.createRow(2);
            for (int i = 0; i < report.headers().size(); i++) { headers.createCell(i).setCellValue(report.headers().get(i)); headers.getCell(i).setCellStyle(header); }
            int rowIndex = 3;
            for (List<String> values : report.rows()) { Row row = sheet.createRow(rowIndex++); for (int i = 0; i < values.size(); i++) row.createCell(i).setCellValue(values.get(i)); }
            sheet.createFreezePane(0, 3);
            if (!report.rows().isEmpty()) sheet.setAutoFilter(new CellRangeAddress(2, rowIndex - 1, 0, report.headers().size() - 1));
            for (int i = 0; i < report.headers().size(); i++) { sheet.autoSizeColumn(i); sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 768, 45 * 256)); }
            workbook.write(output); return output.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("Nao foi possivel gerar o Excel da listagem", e); }
    }

    private byte[] pdf(Report report) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            StringBuilder rows = new StringBuilder();
            for (List<String> row : report.rows()) { rows.append("<tr>"); for (String cell : row) rows.append("<td>").append(esc(cell)).append("</td>"); rows.append("</tr>"); }
            StringBuilder headers = new StringBuilder(); for (String header : report.headers()) headers.append("<th>").append(esc(header)).append("</th>");
            String html = "<html><head><meta charset='UTF-8'/><style>@page{size:A4 landscape;margin:12mm}body{font-family:Arial;font-size:8pt;color:#38434d}h1{font-size:17pt}table{width:100%;border-collapse:collapse;-fs-table-paginate:paginate}th{background:#f2f3f1;text-align:left;padding:5px;border-bottom:1px solid #bbb}td{padding:5px;border-bottom:1px solid #ddd}tr{page-break-inside:avoid}</style></head><body><h1>" + esc(report.title()) + "</h1><p>" + report.rows().size() + " registos</p><table><thead><tr>" + headers + "</tr></thead><tbody>" + rows + "</tbody></table></body></html>";
            PdfRendererBuilder builder = new PdfRendererBuilder(); builder.useFastMode(); builder.withHtmlContent(html, null); builder.toStream(output); builder.run(); return output.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("Nao foi possivel gerar o PDF da listagem", e); }
    }

    private String fileName(String source, LocalDate inicio, LocalDate fim, String extension) { return source + "_" + inicio + "_" + fim + "." + extension; }
    private String ref(String fiscal, String fallback, String serie, Long numero) { return value(fiscal == null || fiscal.isBlank() ? fallback : fiscal) + " " + value(serie) + "/" + value(numero); }
    private String date(LocalDate raw) { return raw == null ? "" : DATE.format(raw); }
    private String value(Object raw) { return raw == null ? "" : String.valueOf(raw); }
    private String esc(String raw) { return value(raw).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }

    private record Report(String title, List<String> headers, List<List<String>> rows) {}
    public record ExportedFile(String filename, String mediaType, byte[] content) {}
}
