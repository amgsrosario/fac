package com.ar2lda.fac;

import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.EmitenteFiscalSnapshotDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialDto;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.TipoLinhaDocumento;
import com.ar2lda.fac.service.AuditoriaService;
import com.ar2lda.fac.service.DocumentoComercialPdfService;
import com.ar2lda.fac.service.DocumentoComercialService;
import com.ar2lda.fac.service.QrCodeImageService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentoComercialPdfServiceTests {

    private static final String DOCUMENT_NUMBER = "FT DEMO26/6";
    private static final String COMPANY = "FAC Demonstração, Lda.";
    private static final String CLIENT = "Cliente Demonstração Norte";
    private static final String ATCUD = "DEMO2026-6";
    private static final DateTimeFormatter ARTIFACT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private DocumentoComercialService documentoService;
    private DocumentoComercialPdfService pdfService;

    @BeforeEach
    void setUp() {
        documentoService = mock(DocumentoComercialService.class);
        pdfService = new DocumentoComercialPdfService(
                documentoService,
                new QrCodeImageService(),
                mock(AuditoriaService.class)
        );
    }

    @Test
    void documentoDeUmaLinhaMantemUmaPaginaSemCabecalhoDeContinuacao() throws Exception {
        byte[] pdf = gerar(1, Scenario.standard());

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            String page = pageText(document, 1);
            assertThat(page).contains(DOCUMENT_NUMBER, COMPANY, CLIENT, "Descricao", "Valor bruto", "ATCUD: " + ATCUD);
            assertThat(page).doesNotContain("CONTINUAÇÃO");
            assertThat(countImages(document.getPage(0))).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void documentoDe35LinhasRepeteIdentificacaoEColunasEmDuasPaginas() throws Exception {
        byte[] pdf = gerar(35, Scenario.standard());

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(2);
            assertMultipageStructure(document);
            assertThat(pageText(document, 1)).doesNotContain("CONTINUAÇÃO");
            assertThat(pageText(document, 2)).contains("CONTINUAÇÃO", "Pagina 2 de 2");
            assertFinalArea(document);
        }
    }

    @Test
    void documentoLongoGeraTresOuMaisPaginasSemPaginaVazia() throws Exception {
        byte[] pdf = gerar(90, Scenario.longContent());

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(3);
            assertMultipageStructure(document);
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                assertThat(pageText(document, page)).isNotBlank();
            }
            assertFinalArea(document);
        }
    }

    @Test
    void variantesFuncionaisGeramPdfSemExcecao() throws Exception {
        List<byte[]> variants = List.of(
                gerar(18, Scenario.standard()),
                gerar(22, Scenario.longContent()),
                gerar(8, Scenario.longClientScenario()),
                gerar(20, Scenario.withObservations()),
                gerar(20, Scenario.differentVatRates()),
                gerar(20, Scenario.cancelledScenario())
        );

        assertThat(variants).allSatisfy(bytes -> {
            assertThat(bytes).startsWith("%PDF-".getBytes());
            assertThat(bytes.length).isGreaterThan(2_000);
        });
        try (PDDocument cancelled = Loader.loadPDF(variants.get(5))) {
            assertThat(new PDFTextStripper().getText(cancelled)).contains("DOCUMENTO ANULADO", "Motivo: Erro de demonstração");
        }
    }

    @Test
    void pdfRenderizaLinhaTextoSemValoresComerciais() throws Exception {
        byte[] pdf = gerarComLinhaTexto();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text)
                    .contains("Nota documental")
                    .contains("segunda linha")
                    .doesNotContain("null")
                    .doesNotContain("0.00 0.00 0.00");
        }
    }

    @Test
    void htmlEPdfRenderizamLinhaTextoVaziaSemValoresComerciais() throws Exception {
        List<LinhaDocumentoComercialDto> linhas = List.of(
                linhaComercial(1, "ART-1", "Artigo de demonstracao"),
                linhaTexto(2, "")
        );
        DocumentoComercialImpressaoDto base = criarImpressao(1, Scenario.standard());
        String html = renderHtml(new DocumentoComercialImpressaoDto(base.empresa(), base.documento(), linhas));

        assertThat(html)
                .contains("class=\"text-row\"")
                .contains("<td class=\"text-body\" colspan=\"7\"></td>")
                .doesNotContain("null")
                .doesNotContain("0.00 0.00 0.00");

        byte[] pdf = gerarComLinhasPersonalizadas(linhas);
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text)
                    .contains("ART-1")
                    .doesNotContain("null")
                    .doesNotContain("0.00 0.00 0.00");
        }
    }

    @Test
    void pdfRespeitaOrdemDeLinhasTextoEComerciais() throws Exception {
        byte[] pdf = gerarComLinhasPersonalizadas(List.of(
                linhaTexto(1, "Introducao documental"),
                linhaTexto(2, "Nota consecutiva"),
                linhaComercial(3, "ART-3", "Artigo intermedio"),
                linhaTexto(4, "Nota entre artigos"),
                linhaComercial(5, "ART-5", "Artigo final"),
                linhaTexto(6, "Nota final")
        ));

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).containsSubsequence(
                    "Introducao documental",
                    "Nota consecutiva",
                    "ART-3",
                    "Nota entre artigos",
                    "ART-5",
                    "Nota final"
            );
            assertThat(text).doesNotContain("Introducao documental 0.00");
        }
    }

    @Test
    void pdfPaginaLinhasTextoSemOcultarConteudoNemTotais() throws Exception {
        List<LinhaDocumentoComercialDto> linhas = new ArrayList<>();
        for (int number = 1; number <= 48; number++) {
            linhas.add(linhaTexto(number * 2 - 1,
                    "Texto documental paginado " + number + " com conteudo visivel integralmente"));
            linhas.add(linhaComercial(number * 2, "ART-P" + number, "Artigo paginado " + number));
        }

        byte[] pdf = gerarComLinhasPersonalizadas(linhas);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(3);
            assertMultipageStructure(document);
            String allText = new PDFTextStripper().getText(document);
            assertThat(allText)
                    .contains("Texto documental paginado 1")
                    .contains("Texto documental paginado 24")
                    .contains("Texto documental paginado 48")
                    .contains("ART-P48");
            assertFinalArea(document);
        }
    }

    @Test
    void produzArtefactosParaValidacaoVisual() throws Exception {
        Path output = Path.of("target", "pdf-validation");
        Files.createDirectories(output);
        String runId = ARTIFACT_TIMESTAMP.format(LocalDateTime.now());
        DocumentoComercialImpressaoDto impressao = criarImpressao(18, Scenario.standard());
        writeArtifact(output, "documento-comercial-" + runId + ".html", "documento-comercial.html", renderHtml(impressao));
        writeArtifact(output, "documento-comercial-" + runId + ".pdf", "documento-comercial.pdf", gerar(impressao));
        writeArtifact(output, "fac-documento-1-pagina-" + runId + ".pdf", "fac-documento-1-pagina.pdf", gerar(1, Scenario.standard()));
        writeArtifact(output, "fac-documento-2-paginas-" + runId + ".pdf", "fac-documento-2-paginas.pdf", gerar(35, Scenario.standard()));
        writeArtifact(output, "fac-documento-3-ou-mais-paginas-" + runId + ".pdf", "fac-documento-3-ou-mais-paginas.pdf", gerar(90, Scenario.longContent()));
        writeArtifact(output, "fac-documento-linhas-texto-" + runId + ".pdf", "fac-documento-linhas-texto.pdf", gerarComLinhasPersonalizadas(List.of(
                linhaTexto(1, "Introducao documental"),
                linhaTexto(2, "Nota consecutiva"),
                linhaComercial(3, "ART-3", "Artigo intermedio"),
                linhaTexto(4, "Nota final")
        )));
    }

    private void writeArtifact(Path output, String uniqueName, String latestName, String content) throws IOException {
        Files.writeString(output.resolve(uniqueName), content, StandardCharsets.UTF_8);
        writeLatest(output.resolve(latestName), content);
    }

    private void writeArtifact(Path output, String uniqueName, String latestName, byte[] content) throws IOException {
        Files.write(output.resolve(uniqueName), content);
        writeLatest(output.resolve(latestName), content);
    }

    private void writeLatest(Path latest, String content) {
        try {
            Files.writeString(latest, content, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // O ficheiro latest pode estar aberto no visualizador; o artefacto com nome unico ja foi gerado.
        }
    }

    private void writeLatest(Path latest, byte[] content) {
        try {
            Files.write(latest, content);
        } catch (IOException ignored) {
            // O ficheiro latest pode estar aberto no visualizador; o artefacto com nome unico ja foi gerado.
        }
    }

    private byte[] gerar(int lineCount, Scenario scenario) {
        return gerar(criarImpressao(lineCount, scenario));
    }

    private byte[] gerar(DocumentoComercialImpressaoDto impressao) {
        when(documentoService.getImpressao(6L)).thenReturn(impressao);
        return pdfService.gerarParaValidacao(6L).content();
    }

    private DocumentoComercialImpressaoDto criarImpressao(int lineCount, Scenario scenario) {
        DocumentoComercialDto documento = mock(DocumentoComercialDto.class);
        when(documento.estado()).thenReturn(scenario.cancelled ? EstadoDocumentoComercial.ANULADO : EstadoDocumentoComercial.EMITIDO);
        when(documento.numeroDocumento()).thenReturn(6L);
        when(documento.numeroDocumentoCompleto()).thenReturn(DOCUMENT_NUMBER);
        when(documento.tipoDocumentoId()).thenReturn("FT");
        when(documento.tipoDocumentoDescricao()).thenReturn("Fatura");
        when(documento.serie()).thenReturn("DEMO26");
        when(documento.dataEmissao()).thenReturn(LocalDate.of(2026, 6, 24));
        when(documento.dataVencimento()).thenReturn(LocalDate.of(2026, 7, 24));
        when(documento.clienteNome()).thenReturn(scenario.longClient ? CLIENT + " - Departamento Internacional com Designação Muito Extensa" : CLIENT);
        when(documento.clienteNif()).thenReturn("509999990");
        when(documento.clienteMorada()).thenReturn(scenario.longClient ? "Avenida Empresarial com uma designação particularmente extensa, número 150" : "Rua da Demonstração, 10");
        when(documento.clienteMorada1()).thenReturn(scenario.longClient ? "Edifício Norte, piso 12, escritório 1204" : null);
        when(documento.clienteCodPostal()).thenReturn("4000-001");
        when(documento.clienteLocalidade()).thenReturn("Porto");
        when(documento.moedaCodigo()).thenReturn("EUR");
        when(documento.moedaSimbolo()).thenReturn("€");
        when(documento.regimeIvaCodigo()).thenReturn("PT");
        when(documento.pPagamentoId()).thenReturn("30D");
        when(documento.mPagamentoId()).thenReturn("NUM");
        when(documento.valorBruto()).thenReturn(new BigDecimal("1234.50"));
        when(documento.valorDesconto()).thenReturn(new BigDecimal("12.50"));
        when(documento.valorIsento()).thenReturn(new BigDecimal("10.00"));
        when(documento.valorSujeitoReduzida()).thenReturn(new BigDecimal("100.00"));
        when(documento.valorSujeitoIntermedia()).thenReturn(new BigDecimal("200.00"));
        when(documento.valorSujeitoNormal()).thenReturn(new BigDecimal("912.00"));
        when(documento.valorIvaReduzida()).thenReturn(new BigDecimal("6.00"));
        when(documento.valorIvaIntermedia()).thenReturn(new BigDecimal("26.00"));
        when(documento.valorIvaNormal()).thenReturn(new BigDecimal("209.76"));
        when(documento.valorIvaTotal()).thenReturn(new BigDecimal("241.76"));
        when(documento.valorRetencao()).thenReturn(BigDecimal.ZERO);
        when(documento.valorTotal()).thenReturn(new BigDecimal("1463.76"));
        when(documento.observacoes()).thenReturn(scenario.observations ? "Observação extensa para confirmar que o bloco final permanece unido e legível sem sobreposição." : null);
        when(documento.atcud()).thenReturn(ATCUD);
        when(documento.qrPayload()).thenReturn("A:509999999*B:509999990*C:PT*D:FT*E:N*F:20260624*G:FT DEMO26/6*H:DEMO2026-6*I1:PT*I7:1222.00*I8:241.76*N:241.76*O:1463.76*Q:TESTE");
        when(documento.emissorId()).thenReturn("ADMIN");
        when(documento.momentoEmissao()).thenReturn(OffsetDateTime.parse("2026-06-24T10:30:00+01:00"));
        when(documento.motivoAnulacao()).thenReturn(scenario.cancelled ? "Erro de demonstração" : null);
        when(documento.dataHoraAnulacao()).thenReturn(scenario.cancelled ? OffsetDateTime.parse("2026-06-24T11:00:00+01:00") : null);
        when(documento.anuladoPorNome()).thenReturn(scenario.cancelled ? "Administrador Demo" : null);

        EmitenteFiscalSnapshotDto empresa = new EmitenteFiscalSnapshotDto(
                COMPANY, "509999999", "Avenida Central, 100", null, "1000-001", "Lisboa", "PT",
                "geral@fac.demo", "www.fac.demo", new BigDecimal("50000.00"), "CRC Lisboa 99999",
                "62010", "Atividades de programação informática"
        );
        List<LinhaDocumentoComercialDto> linhas = new ArrayList<>();
        for (int number = 1; number <= lineCount; number++) {
            LinhaDocumentoComercialDto linha = mock(LinhaDocumentoComercialDto.class);
            when(linha.numeroLinha()).thenReturn(number);
            when(linha.tipoLinha()).thenReturn(TipoLinhaDocumento.COMERCIAL);
            when(linha.artigoId()).thenReturn("ART-" + number);
            String description = scenario.longDescription
                    ? "Artigo com descrição longa para testar quebra controlada de texto, continuidade da tabela e ausência de sobreposição na página " + number
                    : "Artigo de demonstração " + number;
            when(linha.descricao()).thenReturn(description);
            when(linha.quantidade()).thenReturn(new BigDecimal("1.000000"));
            when(linha.precoUnitario()).thenReturn(new BigDecimal("10.00"));
            when(linha.valorDesconto()).thenReturn(BigDecimal.ZERO);
            when(linha.percentagemIva()).thenReturn(scenario.differentVat && number % 3 == 0 ? new BigDecimal("6.00") : new BigDecimal("23.00"));
            when(linha.valorLinha()).thenReturn(new BigDecimal("10.00"));
            linhas.add(linha);
        }
        return new DocumentoComercialImpressaoDto(empresa, documento, linhas);
    }

    private byte[] gerarComLinhaTexto() {
        return gerarComLinhasPersonalizadas(List.of(
                linhaComercial(1, "ART-1", "Artigo de demonstracao"),
                linhaTexto(2, "Nota documental\nsegunda linha")
        ));
    }

    private String renderHtml(DocumentoComercialImpressaoDto impressao) throws Exception {
        Method buildHtml = DocumentoComercialPdfService.class
                .getDeclaredMethod("buildHtml", DocumentoComercialImpressaoDto.class);
        buildHtml.setAccessible(true);
        return (String) buildHtml.invoke(pdfService, impressao);
    }

    private byte[] gerarComLinhasPersonalizadas(List<LinhaDocumentoComercialDto> linhas) {
        DocumentoComercialDto documento = mock(DocumentoComercialDto.class);
        when(documento.estado()).thenReturn(EstadoDocumentoComercial.EMITIDO);
        when(documento.numeroDocumento()).thenReturn(6L);
        when(documento.numeroDocumentoCompleto()).thenReturn(DOCUMENT_NUMBER);
        when(documento.tipoDocumentoId()).thenReturn("FT");
        when(documento.tipoDocumentoDescricao()).thenReturn("Fatura");
        when(documento.serie()).thenReturn("DEMO26");
        when(documento.dataEmissao()).thenReturn(LocalDate.of(2026, 6, 24));
        when(documento.dataVencimento()).thenReturn(LocalDate.of(2026, 7, 24));
        when(documento.clienteNome()).thenReturn(CLIENT);
        when(documento.clienteNif()).thenReturn("509999990");
        when(documento.clienteMorada()).thenReturn("Rua da Demonstracao, 10");
        when(documento.clienteCodPostal()).thenReturn("4000-001");
        when(documento.clienteLocalidade()).thenReturn("Porto");
        when(documento.moedaCodigo()).thenReturn("EUR");
        when(documento.moedaSimbolo()).thenReturn("EUR");
        when(documento.regimeIvaCodigo()).thenReturn("PT");
        when(documento.pPagamentoId()).thenReturn("30D");
        when(documento.mPagamentoId()).thenReturn("NUM");
        when(documento.valorBruto()).thenReturn(new BigDecimal("10.00"));
        when(documento.valorDesconto()).thenReturn(BigDecimal.ZERO);
        when(documento.valorIsento()).thenReturn(BigDecimal.ZERO);
        when(documento.valorSujeitoReduzida()).thenReturn(BigDecimal.ZERO);
        when(documento.valorSujeitoIntermedia()).thenReturn(BigDecimal.ZERO);
        when(documento.valorSujeitoNormal()).thenReturn(new BigDecimal("10.00"));
        when(documento.valorIvaReduzida()).thenReturn(BigDecimal.ZERO);
        when(documento.valorIvaIntermedia()).thenReturn(BigDecimal.ZERO);
        when(documento.valorIvaNormal()).thenReturn(new BigDecimal("2.30"));
        when(documento.valorIvaTotal()).thenReturn(new BigDecimal("2.30"));
        when(documento.valorRetencao()).thenReturn(BigDecimal.ZERO);
        when(documento.valorTotal()).thenReturn(new BigDecimal("12.30"));
        when(documento.atcud()).thenReturn(ATCUD);
        when(documento.qrPayload()).thenReturn("A:509999999*B:509999990*C:PT*D:FT*E:N*F:20260624*G:FT DEMO26/6*H:DEMO2026-6*I1:PT*I7:10.00*I8:2.30*N:2.30*O:12.30*Q:TESTE");
        when(documento.emissorId()).thenReturn("ADMIN");
        when(documento.momentoEmissao()).thenReturn(OffsetDateTime.parse("2026-06-24T10:30:00+01:00"));

        EmitenteFiscalSnapshotDto empresa = new EmitenteFiscalSnapshotDto(
                COMPANY, "509999999", "Avenida Central, 100", null, "1000-001", "Lisboa", "PT",
                "geral@fac.demo", "www.fac.demo", new BigDecimal("50000.00"), "CRC Lisboa 99999",
                "62010", "Atividades de programacao informatica"
        );
        when(documentoService.getImpressao(6L)).thenReturn(new DocumentoComercialImpressaoDto(empresa, documento, linhas));
        return pdfService.gerarParaValidacao(6L).content();
    }

    private LinhaDocumentoComercialDto linhaComercial(int numeroLinha, String artigoId, String descricao) {
        LinhaDocumentoComercialDto linha = mock(LinhaDocumentoComercialDto.class);
        when(linha.numeroLinha()).thenReturn(numeroLinha);
        when(linha.tipoLinha()).thenReturn(TipoLinhaDocumento.COMERCIAL);
        when(linha.artigoId()).thenReturn(artigoId);
        when(linha.descricao()).thenReturn(descricao);
        when(linha.quantidade()).thenReturn(new BigDecimal("1.000000"));
        when(linha.precoUnitario()).thenReturn(new BigDecimal("10.00"));
        when(linha.valorDesconto()).thenReturn(BigDecimal.ZERO);
        when(linha.percentagemIva()).thenReturn(new BigDecimal("23.00"));
        when(linha.valorLinha()).thenReturn(new BigDecimal("10.00"));
        return linha;
    }

    private LinhaDocumentoComercialDto linhaTexto(int numeroLinha, String descricao) {
        LinhaDocumentoComercialDto linha = mock(LinhaDocumentoComercialDto.class);
        when(linha.numeroLinha()).thenReturn(numeroLinha);
        when(linha.tipoLinha()).thenReturn(TipoLinhaDocumento.TEXTO);
        when(linha.descricao()).thenReturn(descricao);
        return linha;
    }

    private void assertMultipageStructure(PDDocument document) throws IOException {
        for (int page = 1; page <= document.getNumberOfPages(); page++) {
            String text = pageText(document, page);
            assertThat(text).contains(DOCUMENT_NUMBER, COMPANY, CLIENT);
            if (text.contains("ART-")) {
                assertThat(text).contains("Descricao", "Qtd.", "Liquido");
            }
            if (page > 1) {
                assertThat(text).contains("CONTINUAÇÃO", "Pagina " + page + " de " + document.getNumberOfPages());
            }
        }
    }

    private void assertFinalArea(PDDocument document) throws IOException {
        for (int page = 1; page < document.getNumberOfPages(); page++) {
            assertThat(pageText(document, page)).doesNotContain("Valor bruto", "ATCUD: " + ATCUD);
        }
        String lastPage = pageText(document, document.getNumberOfPages());
        assertThat(lastPage).contains("Resumo IVA", "Valor bruto", "Total", "ATCUD: " + ATCUD);
        assertThat(countImages(document.getPage(document.getNumberOfPages() - 1))).isGreaterThanOrEqualTo(1);
    }

    private String pageText(PDDocument document, int page) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        return stripper.getText(document).replaceAll("\\s+", " ").trim();
    }

    private int countImages(PDPage page) throws IOException {
        int count = 0;
        for (COSName name : page.getResources().getXObjectNames()) {
            PDXObject object = page.getResources().getXObject(name);
            if (object instanceof PDImageXObject) {
                count++;
            }
        }
        return count;
    }

    private record Scenario(boolean longDescription, boolean longClient, boolean observations,
                            boolean differentVat, boolean cancelled) {
        static Scenario standard() {
            return new Scenario(false, false, false, false, false);
        }

        static Scenario longContent() {
            return new Scenario(true, false, true, true, false);
        }

        static Scenario longClientScenario() {
            return new Scenario(false, true, false, false, false);
        }

        static Scenario withObservations() {
            return new Scenario(false, false, true, false, false);
        }

        static Scenario differentVatRates() {
            return new Scenario(false, false, false, true, false);
        }

        static Scenario cancelledScenario() {
            return new Scenario(false, false, false, false, true);
        }
    }
}
