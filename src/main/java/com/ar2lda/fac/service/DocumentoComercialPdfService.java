package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.EmitenteFiscalSnapshotDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class DocumentoComercialPdfService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale PT = Locale.forLanguageTag("pt-PT");

    private final DocumentoComercialService documentoService;
    private final QrCodeImageService qrCodeImageService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public PdfDocumento gerar(Long id) {
        return gerar(id, true);
    }

    @Transactional(readOnly = true)
    public PdfDocumento gerarParaValidacao(Long id) {
        return gerar(id, false);
    }

    private PdfDocumento gerar(Long id, boolean auditar) {
        DocumentoComercialImpressaoDto impressao = documentoService.getImpressao(id);
        DocumentoComercialDto documento = impressao.documento();
        if ((documento.estado() != EstadoDocumentoComercial.EMITIDO && documento.estado() != EstadoDocumentoComercial.ANULADO)
                || documento.numeroDocumento() == null) {
            throw new BadRequestException("Apenas documentos emitidos podem gerar o PDF definitivo");
        }
        validateQrConsolidado(documento);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(buildHtml(impressao), null);
            builder.toStream(output);
            builder.run();
            PdfDocumento pdf = new PdfDocumento(nomeFicheiro(documento), output.toByteArray());
            if (auditar) {
                auditoriaService.registar(TipoAuditoriaEvento.DOCUMENTO_PDF_GERADO, "DOCUMENTO_COMERCIAL", id,
                        "PDF fiscal gerado", "{\"versao\":1,\"estado\":\"" + documento.estado() + "\"}");
            }
            return pdf;
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel gerar o PDF do documento", exception);
        }
    }

    private String buildHtml(DocumentoComercialImpressaoDto impressao) {
        EmitenteFiscalSnapshotDto empresa = impressao.empresa();
        DocumentoComercialDto documento = impressao.documento();
        StringBuilder linhas = new StringBuilder();
        for (LinhaDocumentoComercialDto linha : impressao.linhas()) {
            linhas.append("<tr>")
                    .append(td(String.valueOf(linha.numeroLinha()), "center"))
                    .append(td(linha.artigoId(), ""))
                    .append(td(linha.descricao(), ""))
                    .append(td(decimal(linha.quantidade(), 6), "number"))
                    .append(td(money(linha.precoUnitario()), "number"))
                    .append(td(money(linha.valorDesconto()), "number"))
                    .append(td(decimal(linha.percentagemIva(), 2) + "%", "number"))
                    .append(td(money(linha.valorLinha()), "number"))
                    .append("</tr>");
        }

        String anulada = documento.estado() == EstadoDocumentoComercial.ANULADO
                ? "<div class=\"watermark\">ANULADO</div><div class=\"annulment\"><strong>DOCUMENTO ANULADO</strong><br />Motivo: "
                    + esc(documento.motivoAnulacao()) + "<br />Anulado em: "
                    + esc(documento.dataHoraAnulacao()) + "<br />Por: " + esc(documento.anuladoPorNome()) + "</div>"
                : "";
        String transporte = hasText(documento.cargaMorada()) || hasText(documento.descargaMorada())
                ? transporte(documento)
                : "";

        return """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml" lang="pt">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page { size: A4; margin: 29mm 13mm 18mm; @top-center { content: element(continuation-header); } @bottom-center { content: "Pagina " counter(page) " de " counter(pages); font-size: 8pt; color: #737980; } }
                    @page:first { margin-top: 16mm; @top-center { content: none; } }
                    * { box-sizing: border-box; }
                    body { font-family: Arial, sans-serif; font-size: 9pt; color: #38434d; margin: 0; }
                    h1, h2, p { margin: 0; }
                    .continuation-header { position: running(continuation-header); width: 100%%; border-bottom: 1.5px solid #ba963c; padding: 0 0 2.2mm; color: #44515d; font-size: 7.5pt; line-height: 1.35; }
                    .continuation-header table { width: 100%%; border-collapse: collapse; }
                    .continuation-header td { vertical-align: top; }
                    .continuation-company { width: 31%%; }
                    .continuation-document { width: 38%%; text-align: center; }
                    .continuation-client { width: 31%%; text-align: right; }
                    .continuation-label { color: #9a771e; font-size: 8.5pt; font-weight: bold; letter-spacing: .6px; text-transform: uppercase; }
                    .continuation-page::after { content: "Pagina " counter(page) " de " counter(pages); }
                    .top { width: 100%%; border-bottom: 2px solid #ba963c; padding-bottom: 8px; margin-bottom: 14px; }
                    .top td { vertical-align: top; }
                    .company { width: 62%%; line-height: 1.45; }
                    .logo { max-width: 28mm; max-height: 16mm; display: block; margin-bottom: 4px; }
                    .doc-title { width: 38%%; text-align: right; }
                    .doc-title h1 { font-size: 18pt; color: #44515d; }
                    .doc-title strong { font-size: 12pt; }
                    .muted { color: #777f87; }
                    .box { border: 1px solid #dfe2e3; padding: 9px; margin-bottom: 12px; page-break-inside: avoid; }
                    .section-title { color: #9a771e; font-size: 8pt; text-transform: uppercase; letter-spacing: .8px; margin-bottom: 6px; }
                    .info { width: 100%%; }
                    .info td { width: 50%%; vertical-align: top; line-height: 1.45; padding-right: 10px; }
                    table.lines { width: 100%%; border-collapse: collapse; margin-bottom: 12px; -fs-table-paginate: paginate; }
                    .lines thead { display: table-header-group; }
                    .lines tr { page-break-inside: avoid; }
                    .lines th { background: #f2f3f1; color: #44515d; font-size: 7.5pt; padding: 6px 4px; border-bottom: 1px solid #cfd3d5; text-align: left; }
                    .lines td { padding: 6px 4px; border-bottom: 1px solid #e5e7e7; vertical-align: top; }
                    .number { text-align: right; white-space: nowrap; }
                    .center { text-align: center; }
                    .summary { width: 100%%; page-break-inside: avoid; }
                    .tax { width: 61%%; border-collapse: collapse; vertical-align: top; }
                    .tax th, .tax td { padding: 5px; border-bottom: 1px solid #e2e4e5; }
                    .tax th { text-align: left; background: #f7f7f5; }
                    .totals { width: 36%%; margin-left: 3%%; border-collapse: collapse; vertical-align: top; }
                    .totals td { padding: 5px 2px; border-bottom: 1px solid #e2e4e5; }
                    .totals td:last-child { text-align: right; white-space: nowrap; }
                    .grand td { font-size: 12pt; font-weight: bold; color: #44515d; border-top: 2px solid #ba963c; }
                    .fiscal { width: 100%%; margin-top: 12px; padding-top: 8px; border-top: 1px solid #dfe2e3; page-break-inside: avoid; }
                    .fiscal-card { width: 42mm; margin-left: auto; text-align: center; }
                    .fiscal-atcud { font-size: 7.5pt; font-weight: bold; color: #111; margin-bottom: 3mm; }
                    .qr { width: 40mm; height: 40mm; }
                    .footer { margin-top: 18px; padding-top: 8px; border-top: 1px solid #dfe2e3; font-size: 7.5pt; color: #777f87; line-height: 1.4; }
                    .watermark { position: fixed; top: 44%%; left: 18%%; width: 64%%; transform: rotate(-28deg); text-align: center; font-size: 58pt; font-weight: bold; color: #efdede; }
                    .annulment { border: 2px solid #a51f1f; color: #7d1717; background: #fff4f4; padding: 8px; margin-bottom: 10px; line-height: 1.45; }
                  </style>
                </head>
                <body>
                <div class="continuation-header"><table><tr>
                  <td class="continuation-company"><strong>%s</strong><br />NIF %s</td>
                  <td class="continuation-document"><span class="continuation-label">Continuação</span><br /><strong>%s - %s %s/%s</strong><br />Data %s</td>
                  <td class="continuation-client"><strong>%s</strong><br />NIF %s<br /><span class="continuation-page"></span></td>
                </tr></table></div>
                %s
                <table class="top"><tr>
                  <td class="company">%s<strong>%s</strong><br />NIF %s<br />%s<br />%s<br /><span class="muted">%s %s %s</span></td>
                  <td class="doc-title"><h1>%s</h1><strong>%s %s/%s</strong><br /><span class="muted">Emitido em %s</span><br /><span class="muted">Vencimento %s</span></td>
                </tr></table>

                <div class="box"><div class="section-title">Cliente</div><table class="info"><tr>
                  <td><strong>%s</strong><br />NIF %s<br />%s<br />%s</td>
                  <td><strong>Condicoes</strong><br />Moeda: %s<br />Regime IVA: %s<br />Modo / prazo: %s / %s</td>
                </tr></table></div>

                <table class="lines">
                  <thead><tr><th>#</th><th>Artigo</th><th>Descricao</th><th class="number">Qtd.</th><th class="number">Preco</th><th class="number">Desc.</th><th class="number">IVA</th><th class="number">Liquido</th></tr></thead>
                  <tbody>%s</tbody>
                </table>

                <table class="summary"><tr><td class="tax">
                  <table class="tax"><tr><th>Resumo IVA</th><th class="number">Base</th><th class="number">IVA</th></tr>
                    <tr><td>Isento</td><td class="number">%s</td><td class="number">-</td></tr>
                    <tr><td>Taxa reduzida</td><td class="number">%s</td><td class="number">%s</td></tr>
                    <tr><td>Taxa intermedia</td><td class="number">%s</td><td class="number">%s</td></tr>
                    <tr><td>Taxa normal</td><td class="number">%s</td><td class="number">%s</td></tr>
                  </table>
                </td><td class="totals">
                  <table class="totals"><tr><td>Valor bruto</td><td>%s %s</td></tr><tr><td>Desconto</td><td>%s %s</td></tr><tr><td>IVA</td><td>%s %s</td></tr><tr><td>Retencao</td><td>%s %s</td></tr><tr class="grand"><td>Total</td><td>%s %s</td></tr></table>
                </td></tr></table>

                %s
                %s
                %s
                <div class="footer">%s</div>
                </body></html>
                """.formatted(
                esc(empresa.nome()), esc(empresa.nif()), esc(documento.tipoDocumentoDescricao()),
                esc(documento.tipoDocumentoId()), esc(documento.serie()), documento.numeroDocumento(), date(documento.dataEmissao()),
                esc(documento.clienteNome()), esc(documento.clienteNif()),
                anulada,
                logo(empresa), esc(empresa.nome()), esc(empresa.nif()), address(empresa.morada(), empresa.morada1()),
                esc(joinPostal(empresa.codPostal(), empresa.localidade())), esc(empresa.email()), esc(empresa.web()),
                hasText(empresa.telefone()) ? " · Tel. " + esc(empresa.telefone()) : "",
                esc(documento.tipoDocumentoDescricao()), esc(documento.tipoDocumentoId()), esc(documento.serie()), documento.numeroDocumento(),
                date(documento.dataEmissao()), date(documento.dataVencimento()),
                esc(documento.clienteNome()), esc(documento.clienteNif()), address(documento.clienteMorada(), documento.clienteMorada1()),
                esc(joinPostal(documento.clienteCodPostal(), documento.clienteLocalidade())),
                esc(documento.moedaCodigo()), esc(documento.regimeIvaCodigo()), value(documento.mPagamentoId()), esc(documento.pPagamentoId()),
                linhas,
                money(documento.valorIsento()), money(documento.valorSujeitoReduzida()), money(documento.valorIvaReduzida()),
                money(documento.valorSujeitoIntermedia()), money(documento.valorIvaIntermedia()), money(documento.valorSujeitoNormal()), money(documento.valorIvaNormal()),
                money(documento.valorBruto()), esc(documento.moedaSimbolo()), money(documento.valorDesconto()), esc(documento.moedaSimbolo()),
                money(documento.valorIvaTotal()), esc(documento.moedaSimbolo()), money(documento.valorRetencao()), esc(documento.moedaSimbolo()),
                money(documento.valorTotal()), esc(documento.moedaSimbolo()),
                transporte,
                hasText(documento.observacoes()) ? "<div class=\"box\"><div class=\"section-title\">Observacoes</div>" + esc(documento.observacoes()) + "</div>" : "",
                fiscal(documento.atcud(), documento.qrPayload()),
                footer(empresa, documento)
        );
    }

    private String logo(EmitenteFiscalSnapshotDto empresa) {
        if (empresa == null || empresa.logo() == null || empresa.logo().length == 0 || !hasText(empresa.logoMediaType())) {
            return "";
        }
        return "<img class=\"logo\" src=\"data:" + esc(empresa.logoMediaType()) + ";base64,"
                + Base64.getEncoder().encodeToString(empresa.logo()) + "\" alt=\"Logotipo\" />";
    }

    private String footer(EmitenteFiscalSnapshotDto empresa, DocumentoComercialDto documento) {
        String base = "Emitido por " + esc(documento.emissorId()) + " em "
                + (documento.momentoEmissao() == null ? "-" : esc(documento.momentoEmissao().toString()))
                + ". Capital social: " + money(empresa.capitalSocial()) + " EUR. Matricula comercial: "
                + esc(empresa.matriculaRegistoComercial()) + ". CAE: " + esc(empresa.cae()) + " - "
                + esc(empresa.descricaoCae()) + ".";
        String iban = hasText(empresa.iban()) ? " IBAN: " + esc(empresa.iban()) + "." : "";
        String bic = hasText(empresa.bicSwift()) ? " BIC/SWIFT: " + esc(empresa.bicSwift()) + "." : "";
        String legal = hasText(empresa.observacoesLegais()) ? " " + esc(empresa.observacoesLegais()) : "";
        String custom = hasText(empresa.textoRodape()) ? " " + esc(empresa.textoRodape()) : "";
        return base + iban + bic + legal + custom;
    }

    private String transporte(DocumentoComercialDto d) {
        return "<div class=\"box\"><div class=\"section-title\">Transporte</div><table class=\"info\"><tr><td><strong>Carga</strong><br />"
                + date(d.dataCarga()) + " " + time(d.horaCarga()) + "<br />" + esc(d.cargaNome()) + "<br />"
                + address(d.cargaMorada(), d.cargaMorada1()) + "<br />" + esc(joinPostal(d.cargaCodPostal(), d.cargaLocalidade()))
                + "</td><td><strong>Descarga</strong><br />" + date(d.dataDescarga()) + " " + time(d.horaDescarga()) + "<br />"
                + address(d.descargaMorada(), d.descargaMorada1()) + "<br />" + esc(joinPostal(d.descargaCodPostal(), d.descargaLocalidade()))
                + "<br />Matricula: " + esc(d.matricula()) + "</td></tr></table></div>";
    }

    private String fiscal(String atcud, String qrPayload) {
        if (!hasText(atcud) && !hasText(qrPayload)) {
            return "";
        }
        String qr = hasText(qrPayload)
                ? "<img class=\"qr\" src=\"" + qrCodeImageService.toPngDataUri(qrPayload) + "\" alt=\"QR Code fiscal\" />"
                : "";
        return "<div class=\"fiscal\"><div class=\"fiscal-card\"><div class=\"fiscal-atcud\">ATCUD: "
                + esc(atcud) + "</div>" + qr + "</div></div>";
    }

    private void validateQrConsolidado(DocumentoComercialDto documento) {
        if (hasText(documento.qrPayload())) {
            return;
        }
        if (!hasText(documento.numeroDocumentoCompleto())) {
            throw new ConflictException(
                    "Documento legado sem payload QR fiscal consolidado; o PDF fiscal não pode ser gerado"
            );
        }
        throw new ConflictException(
                "Documento emitido inconsistente: payload QR fiscal consolidado em falta"
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

    private String time(java.time.LocalTime value) {
        return value == null ? "" : TIME.format(value);
    }

    private String money(BigDecimal value) {
        return decimal(value, 2);
    }

    private String decimal(BigDecimal value, int scale) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return String.format(PT, "%." + scale + "f", safe.setScale(scale, RoundingMode.HALF_UP));
    }

    private String nomeFicheiro(DocumentoComercialDto documento) {
        return "%s-%s-%s.pdf".formatted(documento.tipoDocumentoId(), documento.serie(), documento.numeroDocumento())
                .replaceAll("[^A-Za-z0-9._-]", "-");
    }

    private String esc(Object raw) {
        String text = value(raw);
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
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
