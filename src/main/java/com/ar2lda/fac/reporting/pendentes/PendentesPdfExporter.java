package com.ar2lda.fac.reporting.pendentes;

import com.ar2lda.fac.controller.dto.PendenteListagemDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PendentesPdfExporter {

    public static final String MEDIA_TYPE = "application/pdf";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale PT = Locale.forLanguageTag("pt-PT");

    private final PendentesReportDataService dataService;

    public ExportedPdf exportPendentes(List<Long> clienteIds) {
        return export(dataService.pendentes(clienteIds));
    }

    public ExportedPdf exportPendentesAData(LocalDate dataReferencia, List<Long> clienteIds) {
        return export(dataService.pendentesAData(dataReferencia, clienteIds));
    }

    ExportedPdf export(PendentesReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(buildHtml(data), null);
            builder.toStream(output);
            builder.run();
            return new ExportedPdf(PendentesFileName.build(data, "pdf"), output.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel gerar o PDF de pendentes", exception);
        }
    }

    private String buildHtml(PendentesReportData data) {
        StringBuilder rows = new StringBuilder();
        for (PendenteListagemDto linha : data.pendentes().linhas()) {
            rows.append("<tr>")
                    .append(td("%d - %s".formatted(linha.clienteId(), linha.clienteNome()), "client"))
                    .append(td(linha.documento(), "document"))
                    .append(td(date(linha.data()), "date"))
                    .append(td(date(linha.vencimento()), "date"))
                    .append(td(money(linha.total()), "amount"))
                    .append(td(money(linha.recebido()), "amount"))
                    .append(td(money(linha.pendente()), "amount"))
                    .append("</tr>");
        }
        if (data.pendentes().linhas().isEmpty()) {
            rows.append("<tr><td colspan=\"7\" class=\"empty\">Sem documentos para apresentar.</td></tr>");
        }
        rows.append("<tr class=\"total\"><td></td><td>Totais</td><td></td><td></td>")
                .append(td(money(data.pendentes().totais().total()), "amount"))
                .append(td(money(data.pendentes().totais().recebido()), "amount"))
                .append(td(money(data.pendentes().totais().pendente()), "amount"))
                .append("</tr>");

        var empresa = data.empresa();
        String referencia = data.dataReferencia() == null ? "" : "<br /><strong>Data de referência</strong><br />" + date(data.dataReferencia());
        return """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml" lang="pt">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page { size: A4 landscape; margin: 14mm 10mm 16mm;
                      @bottom-left { content: "%s"; font-size: 7.5pt; color: #737980; }
                      @bottom-right { content: "Pagina " counter(page) " de " counter(pages); font-size: 7.5pt; color: #737980; }
                    }
                    * { box-sizing: border-box; }
                    body { font-family: Arial, sans-serif; font-size: 8.5pt; color: #38434d; margin: 0; }
                    h1, p { margin: 0; }
                    .top { width: 100%%; border-bottom: 2px solid #ba963c; padding-bottom: 8px; margin-bottom: 10px; }
                    .top td { vertical-align: top; }
                    .company { width: 58%%; line-height: 1.4; }
                    .title { width: 42%%; text-align: right; }
                    .title h1 { font-size: 17pt; color: #44515d; }
                    .muted { color: #777f87; }
                    .info { width: 100%%; border: 1px solid #dfe2e3; border-collapse: collapse; margin-bottom: 12px; page-break-inside: avoid; }
                    .info td { width: 50%%; padding: 8px; vertical-align: top; line-height: 1.45; }
                    table.lines { width: 100%%; border-collapse: collapse; -fs-table-paginate: paginate; }
                    .lines thead { display: table-header-group; }
                    .lines tr { page-break-inside: avoid; }
                    .lines th { background: #f2f3f1; color: #44515d; font-size: 7.2pt; padding: 5px 3px; border-bottom: 1px solid #cfd3d5; text-align: left; }
                    .lines td { padding: 5px 3px; border-bottom: 1px solid #e5e7e7; vertical-align: top; }
                    .client { width: 27%%; }
                    .document { width: 14%%; }
                    .date { width: 10%%; white-space: nowrap; }
                    .amount { width: 12%%; text-align: right !important; white-space: nowrap; }
                    .empty { text-align: center; color: #777f87; padding: 12px !important; }
                    .total td { font-weight: bold; background: #fafaf8; border-top: 2px solid #ba963c; }
                  </style>
                </head>
                <body>
                  <table class="top"><tr>
                    <td class="company"><strong>%s</strong><br />NIF %s<br />%s<br />%s<br /><span class="muted">%s %s</span></td>
                    <td class="title"><h1>%s</h1><span class="muted">Emitido em %s</span></td>
                  </tr></table>
                  <table class="info"><tr>
                    <td><strong>Filtros</strong><br />%s</td>
                    <td><strong>Documentos</strong><br />%d linhas%s</td>
                  </tr></table>
                  <table class="lines"><thead><tr>
                    <th>Cliente</th><th>Documento</th><th>Data</th><th>Vencimento</th>
                    <th class="amount">Total</th><th class="amount">Recebido</th><th class="amount">Pendente</th>
                  </tr></thead><tbody>%s</tbody></table>
                </body></html>
                """.formatted(
                cssContent("FAC - " + data.titulo()),
                esc(empresa.nome()), esc(empresa.nif()), address(empresa.morada(), empresa.morada1()),
                esc(joinPostal(empresa.codPostalId(), empresa.localidade())), esc(empresa.email()), esc(empresa.web()),
                esc(data.titulo()), DATE_TIME.format(data.geradoEm().toLocalDateTime()),
                esc(data.filtros()), data.pendentes().linhas().size(), referencia, rows
        );
    }

    private String td(Object text, String cssClass) {
        return "<td class=\"" + cssClass + "\">" + esc(text) + "</td>";
    }

    private String date(LocalDate value) {
        return value == null ? "-" : DATE.format(value);
    }

    private String money(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return String.format(PT, "%.2f", safe.setScale(2, RoundingMode.HALF_UP));
    }

    private String address(String first, String second) {
        return esc(first) + (hasText(second) ? "<br />" + esc(second) : "");
    }

    private String joinPostal(String postal, String localidade) {
        return (value(postal) + " " + value(localidade)).trim();
    }

    private String esc(Object raw) {
        return value(raw).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String cssContent(String value) {
        return value(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String value(Object raw) {
        return raw == null ? "" : String.valueOf(raw);
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    public record ExportedPdf(String filename, byte[] content) {
    }
}
