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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private static final int EXPORT_PAGE_SIZE = 500;
    // The PDF renderer still needs its complete HTML model to preserve the existing layout.
    private static final int PDF_EXPORT_LIMIT = 100_000;

    private final ListagensService service;

    public ExportedFile export(String source, String format, LocalDate inicio, LocalDate fim,
                               List<Long> clienteIds, List<String> artigoIds,
                               boolean mostrarAnulados, boolean mostrarTexto) {
        return switch (format) {
            case "pdf" -> {
                Report report = report(source, inicio, fim, clienteIds, artigoIds, mostrarAnulados, mostrarTexto);
                yield new ExportedFile(fileName(source, inicio, fim, "pdf"), PDF_MEDIA_TYPE, pdf(report));
            }
            case "xlsx" -> new ExportedFile(fileName(source, inicio, fim, "xlsx"), XLSX_MEDIA_TYPE,
                    excel(source, inicio, fim, clienteIds, artigoIds, mostrarAnulados, mostrarTexto));
            default -> throw new IllegalArgumentException("Formato de exportacao nao suportado");
        };
    }

    private Report report(String source, LocalDate inicio, LocalDate fim, List<Long> clientes,
                          List<String> artigos, boolean anulados, boolean texto) {
        var page = PageRequest.of(0, PDF_EXPORT_LIMIT);
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

    private byte[] excel(String source, LocalDate inicio, LocalDate fim, List<Long> clientes, List<String> artigos,
                         boolean anulados, boolean texto) {
        Report template = reportTemplate(source);
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        try (workbook; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Listagem");
            Font bold = workbook.createFont(); bold.setBold(true);
            CellStyle title = workbook.createCellStyle(); title.setFont(bold);
            CellStyle header = workbook.createCellStyle(); header.setFont(bold); header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Row titleRow = sheet.createRow(0); titleRow.createCell(0).setCellValue(template.title()); titleRow.getCell(0).setCellStyle(title);
            Row headers = sheet.createRow(2);
            for (int i = 0; i < template.headers().size(); i++) { headers.createCell(i).setCellValue(template.headers().get(i)); headers.getCell(i).setCellStyle(header); }
            int rowIndex = 3, pageNumber = 0, exported = 0;
            boolean hasNext;
            do {
                var page = PageRequest.of(pageNumber++, EXPORT_PAGE_SIZE, exportSort(source));
                List<List<String>> values = pageRows(source, inicio, fim, clientes, artigos, anulados, texto, page);
                for (List<String> value : values) { Row row = sheet.createRow(rowIndex++); for (int i = 0; i < value.size(); i++) row.createCell(i).setCellValue(value.get(i)); }
                exported += values.size();
                hasNext = values.size() == EXPORT_PAGE_SIZE;
            } while (hasNext);
            sheet.createFreezePane(0, 3);
            if (exported > 0) sheet.setAutoFilter(new CellRangeAddress(2, rowIndex - 1, 0, template.headers().size() - 1));
            for (int i = 0; i < template.headers().size(); i++) sheet.setColumnWidth(i, 22 * 256);
            workbook.write(output); return output.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("Nao foi possivel gerar o Excel da listagem", e); }
        finally { workbook.dispose(); }
    }

    private Report reportTemplate(String source) {
        return switch (source) {
            case "documentos-comerciais" -> new Report("Documentos comerciais", List.of("Documento", "Data", "Cliente", "NIF", "Moeda", "Total", "Estado"), List.of());
            case "linhas-comerciais" -> new Report("Detalhe dos documentos comerciais", List.of("Documento", "Data", "Cliente", "Linha", "Artigo", "Descricao", "Quantidade", "Valor"), List.of());
            case "documentos-financeiros" -> new Report("Documentos financeiros", List.of("Documento", "Data", "Cliente", "Modo", "Recebido", "Estado"), List.of());
            case "linhas-financeiras" -> new Report("Detalhe dos documentos financeiros", List.of("Recebimento", "Documento liquidado", "Data", "Pendente antes", "Valor liquidado", "Recebido", "Novo pendente"), List.of());
            default -> throw new IllegalArgumentException("Listagem nao suportada");
        };
    }

    private Sort exportSort(String source) {
        return switch (source) {
            case "documentos-comerciais", "documentos-financeiros" -> Sort.by("dataEmissao").ascending().and(Sort.by("id"));
            case "linhas-comerciais" -> Sort.by("documentoComercial.dataEmissao").ascending().and(Sort.by("documentoComercial.id")).and(Sort.by("id"));
            case "linhas-financeiras" -> Sort.by("documentoFinanceiro.dataEmissao").ascending().and(Sort.by("documentoFinanceiro.id")).and(Sort.by("id"));
            default -> throw new IllegalArgumentException("Listagem nao suportada");
        };
    }

    private List<List<String>> pageRows(String source, LocalDate inicio, LocalDate fim, List<Long> clientes, List<String> artigos, boolean anulados, boolean texto, org.springframework.data.domain.Pageable page) {
        return switch (source) {
            case "documentos-comerciais" -> service.documentosComerciais(inicio, fim, null, clientes, anulados, page).map(item -> { var d = item.documento(); return List.of(ref(d.tipoDocumentoCodigoFiscal(), d.tipoDocumentoId(), d.serie(), d.numeroDocumento()), date(d.dataEmissao()), value(d.clienteNome()), value(d.clienteNif()), value(d.moedaId()), value(d.valorTotal()), value(d.estado())); }).getContent();
            case "linhas-comerciais" -> service.linhasComerciais(inicio, fim, null, clientes, null, artigos, texto, page).map(item -> { var d = item.documento(); var l = item.linha(); return List.of(ref(d.tipoDocumentoCodigoFiscal(), d.tipoDocumentoId(), d.serie(), d.numeroDocumento()), date(d.dataEmissao()), value(d.clienteNome()), value(l.numeroLinha()), value(l.artigoId()), value(l.descricao()), value(l.quantidade()), value(l.valorLinha())); }).getContent();
            case "documentos-financeiros" -> service.documentosFinanceiros(inicio, fim, null, clientes, anulados, page).map(d -> List.of(ref(d.tipoDocumentoCodigoFiscal(), d.tipoDocumentoId(), d.serie(), d.numeroDocumento()), date(d.dataEmissao()), value(d.clienteId()), value(d.mPagamentoId()), value(d.valorPagamentoLiquido()), d.anulado() ? "ANULADO" : "EMITIDO")).getContent();
            case "linhas-financeiras" -> service.linhasFinanceiras(inicio, fim, null, clientes, page).map(item -> { var d = item.documento(); var l = item.linha(); return List.of(ref(d.tipoDocumentoCodigoFiscal(), d.tipoDocumentoId(), d.serie(), d.numeroDocumento()), ref(l.tipoDocumentoId(), l.tipoDocumentoId(), l.serieDocumento(), l.numeroDocumento()), date(d.dataEmissao()), value(l.valorPendenteAntes()), value(l.valorALiquidar()), value(l.valorPagamentoLiquido()), value(l.novoValorPendente())); }).getContent();
            default -> throw new IllegalArgumentException("Listagem nao suportada");
        };
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
