package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ClienteDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoFinanceiroDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentoFinanceiroPdfService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PT = Locale.forLanguageTag("pt-PT");

    private final DocumentoFinanceiroService documentoService;

    public PdfDocumento gerar(Long id) {
        DocumentoFinanceiroImpressaoDto impressao = documentoService.getImpressao(id);
        DocumentoFinanceiroDto documento = impressao.documento();

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(buildHtml(impressao), null);
            builder.toStream(output);
            builder.run();
            documentoService.marcarComoImpresso(id);
            return new PdfDocumento(nomeFicheiro(documento), output.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel gerar o PDF do documento financeiro", exception);
        }
    }

    private String buildHtml(DocumentoFinanceiroImpressaoDto impressao) {
        EmpresaDto empresa = impressao.empresa();
        ClienteDto cliente = impressao.cliente();
        DocumentoFinanceiroDto documento = impressao.documento();
        StringBuilder linhas = new StringBuilder();
        for (LinhaDocumentoFinanceiroDto linha : documento.linhas()) {
            linhas.append("<tr>")
                    .append(td(String.valueOf(linha.numeroLinha()), "center"))
                    .append(td(linha.tipoDocumentoId() + " " + linha.serieDocumento() + "/" + linha.numeroDocumento(), ""))
                    .append(td(date(linha.dataDocumento()), ""))
                    .append(td(date(linha.dataVencimento()), ""))
                    .append(td(money(linha.valorDocumento()), "number"))
                    .append(td(money(linha.valorPendenteAntes()), "number"))
                    .append(td(money(linha.valorALiquidar()), "number"))
                    .append(td(money(linha.descontoValor()), "number"))
                    .append(td(money(linha.valorPagamentoLiquido()), "number"))
                    .append(td(money(linha.novoValorPendente()), "number"))
                    .append("</tr>");
        }

        String anulada = documento.anulado() ? "<div class=\"watermark\">ANULADO</div>" : "";
        String observacoes = hasText(documento.observacoes())
                ? "<div class=\"box\"><div class=\"section-title\">Observacoes</div>" + esc(documento.observacoes()) + "</div>"
                : "";

        return """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml" lang="pt">
                <head><meta charset="UTF-8" /><style>
                  @page { size: A4; margin: 16mm 12mm 18mm; @bottom-center { content: "Pagina " counter(page) " de " counter(pages); font-size: 8pt; color: #737980; } }
                  * { box-sizing: border-box; }
                  body { font-family: Arial, sans-serif; font-size: 9pt; color: #38434d; margin: 0; }
                  h1, p { margin: 0; }
                  .top { width: 100%%; border-bottom: 2px solid #ba963c; padding-bottom: 8px; margin-bottom: 14px; }
                  .top td { vertical-align: top; }
                  .company { width: 62%%; line-height: 1.45; }
                  .doc-title { width: 38%%; text-align: right; }
                  .doc-title h1 { font-size: 18pt; color: #44515d; }
                  .doc-title strong { font-size: 12pt; }
                  .muted { color: #777f87; }
                  .box { border: 1px solid #dfe2e3; padding: 9px; margin-bottom: 12px; page-break-inside: avoid; }
                  .section-title { color: #9a771e; font-size: 8pt; text-transform: uppercase; letter-spacing: .8px; margin-bottom: 6px; }
                  .info { width: 100%%; }
                  .info td { width: 50%%; vertical-align: top; line-height: 1.45; padding-right: 10px; }
                  table.lines { width: 100%%; border-collapse: collapse; margin-bottom: 14px; -fs-table-paginate: paginate; }
                  .lines thead { display: table-header-group; }
                  .lines th { background: #f2f3f1; color: #44515d; font-size: 7pt; padding: 6px 3px; border-bottom: 1px solid #cfd3d5; text-align: left; }
                  .lines td { padding: 6px 3px; border-bottom: 1px solid #e5e7e7; vertical-align: top; }
                  .number { text-align: right; white-space: nowrap; }
                  .center { text-align: center; }
                  .totals { width: 40%%; margin-left: 60%%; border-collapse: collapse; page-break-inside: avoid; }
                  .totals td { padding: 6px 2px; border-bottom: 1px solid #e2e4e5; }
                  .totals td:last-child { text-align: right; white-space: nowrap; }
                  .grand td { font-size: 12pt; font-weight: bold; color: #44515d; border-top: 2px solid #ba963c; }
                  .footer { margin-top: 18px; padding-top: 8px; border-top: 1px solid #dfe2e3; font-size: 7.5pt; color: #777f87; line-height: 1.4; }
                  .watermark { position: fixed; top: 44%%; left: 18%%; width: 64%%; transform: rotate(-28deg); text-align: center; font-size: 58pt; font-weight: bold; color: #efdede; }
                </style></head><body>
                %s
                <table class="top"><tr>
                  <td class="company"><strong>%s</strong><br />NIF %s<br />%s<br />%s<br /><span class="muted">%s %s</span></td>
                  <td class="doc-title"><h1>%s</h1><strong>%s %s/%s</strong><br /><span class="muted">Emitido em %s</span></td>
                </tr></table>

                <div class="box"><div class="section-title">Cliente</div><table class="info"><tr>
                  <td><strong>%s</strong><br />NIF %s<br />%s<br />%s</td>
                  <td><strong>Recebimento</strong><br />Moeda: %s<br />Modo de pagamento: %s<br />Data/hora da operacao: %s</td>
                </tr></table></div>

                <table class="lines"><thead><tr><th>#</th><th>Documento liquidado</th><th>Emissao</th><th>Vencimento</th><th class="number">Documento</th><th class="number">Pendente</th><th class="number">Liquidado</th><th class="number">Desconto</th><th class="number">Recebido</th><th class="number">Novo pend.</th></tr></thead><tbody>%s</tbody></table>

                <table class="totals"><tr><td>Valor aplicado</td><td>%s %s</td></tr><tr><td>Desconto financeiro</td><td>%s %s</td></tr><tr class="grand"><td>Valor recebido</td><td>%s %s</td></tr></table>
                %s
                <div class="footer">Emitido por %s em %s. Capital social: %s EUR. Matricula comercial: %s. CAE: %s - %s.<br />FAC em desenvolvimento - documento ainda sem elementos de certificacao fiscal.</div>
                </body></html>
                """.formatted(
                anulada,
                esc(empresa.nome()), esc(empresa.nif()), address(empresa.morada(), empresa.morada1()), esc(joinPostal(empresa.codPostalId(), empresa.localidade())), esc(empresa.email()), esc(empresa.web()),
                esc(documento.tipoDocumentoId()), esc(documento.tipoDocumentoId()), esc(documento.serie()), documento.numeroDocumento(), date(documento.dataEmissao()),
                esc(cliente.nome()), esc(cliente.nif()), address(cliente.morada(), cliente.morada1()), esc(joinPostal(cliente.codPostalId(), cliente.localidade())),
                esc(documento.moedaId()), value(documento.mPagamentoId()), documento.dataHoraOperacao() == null ? "-" : esc(documento.dataHoraOperacao().toString()),
                linhas,
                money(documento.valorPagamentoBruto()), esc(documento.moedaId()), money(documento.valorDescontoFinanceiro()), esc(documento.moedaId()), money(documento.valorPagamentoLiquido()), esc(documento.moedaId()),
                observacoes,
                esc(documento.emissorId()), documento.momentoEmissao() == null ? "-" : esc(documento.momentoEmissao().toString()),
                money(empresa.capitalSocial()), esc(empresa.matriculaRegistoComercial()), esc(empresa.cae()), esc(empresa.descricaoCae())
        );
    }

    private String td(String text, String cssClass) {
        return "<td class=\"" + cssClass + "\">" + esc(text) + "</td>";
    }

    private String address(String first, String second) {
        return esc(first) + (hasText(second) ? "<br />" + esc(second) : "");
    }

    private String joinPostal(String postal, String localidade) {
        return (value(postal) + " " + value(localidade)).trim();
    }

    private String date(java.time.LocalDate value) {
        return value == null ? "-" : DATE.format(value);
    }

    private String money(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return String.format(PT, "%.2f", safe.setScale(2, RoundingMode.HALF_UP));
    }

    private String nomeFicheiro(DocumentoFinanceiroDto documento) {
        return "%s-%s-%s.pdf".formatted(documento.tipoDocumentoId(), documento.serie(), documento.numeroDocumento())
                .replaceAll("[^A-Za-z0-9._-]", "-");
    }

    private String esc(Object raw) {
        return value(raw).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record PdfDocumento(String filename, byte[] content) {
    }
}
