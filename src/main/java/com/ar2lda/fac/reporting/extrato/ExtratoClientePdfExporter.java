package com.ar2lda.fac.reporting.extrato;

import com.ar2lda.fac.controller.dto.ExtratoClienteMoedaDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteMovimentoDto;
import com.ar2lda.fac.controller.dto.ExtratoClienteTotaisDto;
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
public class ExtratoClientePdfExporter {

    public static final String MEDIA_TYPE = "application/pdf";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale PT = Locale.forLanguageTag("pt-PT");

    private final ExtratoClienteReportDataService dataService;

    public ExportedPdf export(Long clienteId, LocalDate dataInicial, LocalDate dataFinal) {
        return export(dataService.getData(clienteId, dataInicial, dataFinal));
    }

    public ExportedPdf export(List<Long> clienteIds, LocalDate dataInicial, LocalDate dataFinal) {
        return export(dataService.getData(clienteIds, dataInicial, dataFinal));
    }

    ExportedPdf export(ExtratoClienteReportData data) {
        return render(buildHtml(data), ExtratoClienteFileName.build(data, "pdf"));
    }

    ExportedPdf export(ExtratoClientesReportData data) {
        String filename = "extratos-clientes-%s-%s.pdf".formatted(data.dataInicial(), data.dataFinal());
        return render(buildHtml(data), filename);
    }

    private ExportedPdf render(String html, String filename) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return new ExportedPdf(filename, output.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel gerar o PDF do extrato de cliente", exception);
        }
    }

    private String buildHtml(ExtratoClientesReportData data) {
        if (data.extratos().isEmpty()) {
            return "<html><head><meta charset=\"UTF-8\" /></head><body>Sem clientes para apresentar.</body></html>";
        }
        String first = buildHtml(new ExtratoClienteReportData(data.empresa(), data.extratos().getFirst()));
        int bodyStart = first.indexOf("<body>") + "<body>".length();
        int bodyEnd = first.lastIndexOf("</body>");
        StringBuilder html = new StringBuilder(first.substring(0, bodyStart));
        for (int index = 0; index < data.extratos().size(); index++) {
            String clientHtml = buildHtml(new ExtratoClienteReportData(data.empresa(), data.extratos().get(index)));
            int clientBodyStart = clientHtml.indexOf("<body>") + "<body>".length();
            int clientBodyEnd = clientHtml.lastIndexOf("</body>");
            String style = index == 0 ? "" : " style=\"page-break-before: always;\"";
            html.append("<div class=\"client-page\"").append(style).append(">")
                    .append(clientHtml, clientBodyStart, clientBodyEnd)
                    .append("</div>");
        }
        return html.append(first.substring(bodyEnd)).toString();
    }

    private String buildHtml(ExtratoClienteReportData data) {
        var empresa = data.empresa();
        var extrato = data.extrato();
        StringBuilder sections = new StringBuilder();
        for (ExtratoClienteMoedaDto moeda : extrato.moedas()) {
            sections.append("<section class=\"currency\">")
                    .append("<h2>Moeda ").append(esc(moeda.moedaId())).append("</h2>")
                    .append("<table class=\"lines\"><thead>")
                    .append("<tr class=\"context\"><th colspan=\"7\">")
                    .append(esc(extrato.clienteNome())).append(" | ")
                    .append(date(extrato.dataInicial())).append(" a ").append(date(extrato.dataFinal()))
                    .append(" | ").append(esc(moeda.moedaId())).append("</th></tr>")
                    .append("<tr><th class=\"date\">Data</th><th class=\"document\">Documento</th>")
                    .append("<th>Descricao</th><th class=\"date\">Vencimento</th>")
                    .append("<th class=\"amount\">Debito</th><th class=\"amount\">Credito</th>")
                    .append("<th class=\"amount\">Saldo</th></tr></thead><tbody>")
                    .append(totalRow("Anterior", moeda.anterior(), "previous"));
            for (ExtratoClienteMovimentoDto movimento : moeda.movimentos()) {
                sections.append(movementRow(movimento));
            }
            if (moeda.movimentos().isEmpty()) {
                sections.append("<tr><td colspan=\"7\" class=\"empty\">Sem movimentos no periodo selecionado.</td></tr>");
            }
            sections.append(totalRow("Total do periodo", moeda.totalPeriodo(), "period-total"))
                    .append(totalRow("Total final", moeda.totalFinal(), "grand-total"))
                    .append("</tbody></table></section>");
        }

        return """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml" lang="pt">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page { size: A4 portrait; margin: 15mm 10mm 17mm;
                      @bottom-left { content: "FAC - Extrato de Cliente"; font-size: 7.5pt; color: #737980; }
                      @bottom-right { content: "Pagina " counter(page) " de " counter(pages); font-size: 7.5pt; color: #737980; }
                    }
                    * { box-sizing: border-box; }
                    body { font-family: Arial, sans-serif; font-size: 8.5pt; color: #38434d; margin: 0; }
                    h1, h2, p { margin: 0; }
                    .top { width: 100%%; border-bottom: 2px solid #ba963c; padding-bottom: 8px; margin-bottom: 10px; }
                    .top td { vertical-align: top; }
                    .company { width: 60%%; line-height: 1.4; }
                    .title { width: 40%%; text-align: right; }
                    .title h1 { font-size: 17pt; color: #44515d; }
                    .muted { color: #777f87; }
                    .info { width: 100%%; border: 1px solid #dfe2e3; border-collapse: collapse; margin-bottom: 12px; page-break-inside: avoid; }
                    .info td { width: 50%%; padding: 8px; vertical-align: top; line-height: 1.45; }
                    .currency { margin-bottom: 13px; }
                    .currency h2 { color: #9a771e; font-size: 10pt; margin: 8px 0 5px; page-break-after: avoid; }
                    table.lines { width: 100%%; border-collapse: collapse; -fs-table-paginate: paginate; }
                    .lines thead { display: table-header-group; }
                    .lines tr { page-break-inside: avoid; }
                    .lines th { background: #f2f3f1; color: #44515d; font-size: 7.2pt; padding: 5px 3px; border-bottom: 1px solid #cfd3d5; text-align: left; }
                    .lines td { padding: 5px 3px; border-bottom: 1px solid #e5e7e7; vertical-align: top; }
                    .lines .context th { background: #fff; color: #777f87; border-top: 1px solid #dfe2e3; }
                    .date { width: 11%%; white-space: nowrap; }
                    .document { width: 17%%; }
                    .amount { width: 13%%; text-align: right !important; white-space: nowrap; }
                    .empty { text-align: center; color: #777f87; padding: 12px !important; }
                    .total td { font-weight: bold; background: #fafaf8; }
                    .grand-total td { border-top: 2px solid #ba963c; color: #44515d; }
                  </style>
                </head>
                <body>
                  <table class="top"><tr>
                    <td class="company"><strong>%s</strong><br />NIF %s<br />%s<br />%s<br /><span class="muted">%s %s</span></td>
                    <td class="title"><h1>Extrato de Cliente</h1><span class="muted">Emitido em %s</span></td>
                  </tr></table>
                  <table class="info"><tr>
                    <td><strong>Cliente %s</strong><br />%s<br />NIF %s</td>
                    <td><strong>Periodo</strong><br />%s a %s<br /><span class="muted">Filtros: cliente e periodo; ordenacao cronologica</span></td>
                  </tr></table>
                  %s
                </body></html>
                """.formatted(
                esc(empresa.nome()), esc(empresa.nif()), address(empresa.morada(), empresa.morada1()),
                esc(joinPostal(empresa.codPostalId(), empresa.localidade())), esc(empresa.email()), esc(empresa.web()),
                DATE_TIME.format(extrato.geradoEm().toLocalDateTime()),
                extrato.clienteId(), esc(extrato.clienteNome()), esc(extrato.clienteNif()),
                date(extrato.dataInicial()), date(extrato.dataFinal()), sections
        );
    }

    private String movementRow(ExtratoClienteMovimentoDto movement) {
        return "<tr>" + td(date(movement.data()), "date")
                + td(documentReference(movement), "document")
                + td(movement.descricao(), "")
                + td(date(movement.dataVencimento()), "date")
                + td(money(movement.debito()), "amount")
                + td(money(movement.credito()), "amount")
                + td(money(movement.saldoAcumulado()), "amount") + "</tr>";
    }

    private String totalRow(String label, ExtratoClienteTotaisDto totals, String cssClass) {
        return "<tr class=\"total " + cssClass + "\"><td></td><td></td><td>" + esc(label)
                + "</td><td></td><td class=\"amount\">" + money(totals.debito())
                + "</td><td class=\"amount\">" + money(totals.credito())
                + "</td><td class=\"amount\">" + money(totals.saldo()) + "</td></tr>";
    }

    private String documentReference(ExtratoClienteMovimentoDto movement) {
        return "%s %s/%s".formatted(value(movement.tipoDocumentoId()), value(movement.serie()), value(movement.numeroDocumento()));
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

    private String value(Object raw) {
        return raw == null ? "" : String.valueOf(raw);
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    public record ExportedPdf(String filename, byte[] content) {
    }
}
