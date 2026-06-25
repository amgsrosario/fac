package com.ar2lda.fac;

import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import com.ar2lda.fac.service.FiscalQrService;
import com.ar2lda.fac.service.DocumentoComercialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentoComercialEmissaoIntegridadeTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private DocumentoComercialRepository documentoRepository;
    @Autowired private LinhaDocumentoComercialRepository linhaRepository;
    @Autowired private SerieRepository serieRepository;
    @Autowired private TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ArmazemRepository armazemRepository;
    @Autowired private CodPostalRepository codPostalRepository;
    @Autowired private PaisRepository paisRepository;
    @Autowired private MoedaRepository moedaRepository;
    @Autowired private RIvaRepository rIvaRepository;
    @Autowired private TipoTaxaIvaRepository tipoTaxaIvaRepository;
    @Autowired private PPagamentoRepository pPagamentoRepository;
    @Autowired private MPagamentoRepository mPagamentoRepository;
    @Autowired private TransporteRepository transporteRepository;
    @Autowired private FamiliaRepository familiaRepository;
    @Autowired private ArtigoRepository artigoRepository;
    @Autowired private UtilizadorRepository utilizadorRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private FiscalQrService fiscalQrService;
    @Autowired private DocumentoComercialService documentoService;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Cliente cliente;
    private Armazem armazem;

    @BeforeEach
    void setup() {
        cleanupFixture();

        CodPostal codPostal = codPostalRepository.findById("3750-004")
                .orElseGet(() -> codPostalRepository.save(new CodPostal("3750-004", "Águeda")));
        Pais pais = paisRepository.findById("PT").orElseThrow();
        Moeda moeda = moedaRepository.findById("EUR")
                .orElseGet(() -> moedaRepository.save(new Moeda(
                        "EUR", "Euro", BigDecimal.ONE, BigDecimal.ONE, "EUR", 2, "978")));
        RIva riva = rIvaRepository.findById("CON")
                .orElseGet(() -> rIvaRepository.save(new RIva("CON", "Continente")));
        TipoTaxaIva taxaNormal = tipoTaxaIvaRepository.findById("NORMAL").orElseThrow();
        prepararEmpresa(codPostal, pais);
        PPagamento prazo = pPagamentoRepository.findById("P30").orElseGet(() -> {
            PPagamento entity = new PPagamento();
            entity.setId("P30");
            entity.setNome("30 dias");
            entity.setDias(30);
            return pPagamentoRepository.save(entity);
        });
        MPagamento modo = new MPagamento();
        modo.setNome("Transferência integridade");
        modo = mPagamentoRepository.save(modo);
        Transporte transporte = transporteRepository.save(new Transporte("Transporte integridade"));

        TipoDocumento tipo = new TipoDocumento(
                "IC1", "Integridade emissão", null, null, null, null, 1, 1, 1, false);
        tipo.setCodigoFiscal("FT");
        tipoDocumentoRepository.save(tipo);
        serieRepository.save(new Serie(tipo, "LOCK", "Série concorrente", "IC12026", LocalDate.of(2026, 1, 1)));

        cliente = new Cliente();
        cliente.setNome("Cliente Integridade");
        cliente.setMorada("Rua Cliente");
        cliente.setLocalidade("Águeda");
        cliente.setCodPostal(codPostal);
        cliente.setPais(pais);
        cliente.setNif("509654321");
        cliente.setMoeda(moeda);
        cliente.setEmail("cliente.integridade@fac.test");
        cliente.setRiva(riva);
        cliente.setMPagamento(modo);
        cliente.setPPagamento(prazo);
        cliente.setTransporte(transporte);
        cliente = clienteRepository.save(cliente);

        armazem = new Armazem("Armazém Integridade", "Rua Armazém", null, "Águeda");
        armazem.setCodPostal(codPostal);
        armazem.setPais(pais);
        armazem = armazemRepository.save(armazem);

        Familia familia = familiaRepository.save(new Familia("Integridade"));
        Artigo artigo = new Artigo("ARTINT");
        artigo.setDescricao("Artigo Integridade");
        artigo.setUnidade("UN");
        artigo.setFamilia(familia);
        artigo.setIvaCompra(taxaNormal);
        artigo.setIvaVenda(taxaNormal);
        artigo.setPvp(new BigDecimal("10.000000"));
        artigoRepository.save(artigo);

        utilizadorRepository.save(new Utilizador(
                "EMITINT", "Utilizador Emissor Integridade", "emissor.integridade@fac.test",
                "$2a$10$123456789012345678901uZVXKMcIlbsK8PL5kN1l4I2zxIlW85ee", false));
    }

    @AfterEach
    void cleanup() {
        cleanupFixture();
        codPostalRepository.findById("3750-004").ifPresent(codPostal ->
                paisRepository.findById("PT").ifPresent(pais -> prepararEmpresa(codPostal, pais)));
        moedaRepository.findById("EUR").ifPresent(moeda -> {
            moeda.setNome("Euro");
            moeda.setSimbolo("EUR");
            moeda.setNdecimais(2);
            moeda.setVvenda(BigDecimal.ONE);
            moedaRepository.save(moeda);
        });
    }

    @Test
    void documentoEmitidoMantemSnapshotsDepoisDeAlterarMestres() throws Exception {
        Long id = criarRascunho("LOCK");
        emitir(id).call();

        Empresa empresa = empresaRepository.findById(Empresa.EMPRESA_ID).orElseThrow();
        empresa.setNome("Empresa alterada");
        empresa.setNif("599999999");
        empresa.setMorada("Morada nova");
        empresaRepository.saveAndFlush(empresa);
        cliente.setNome("Cliente alterado");
        cliente.setNif("599999998");
        cliente.setMorada("Morada cliente nova");
        clienteRepository.saveAndFlush(cliente);
        Artigo artigo = artigoRepository.findById("ARTINT").orElseThrow();
        artigo.setDescricao("Descrição alterada");
        artigo.setUnidade("KG");
        artigo.setIvaVenda(tipoTaxaIvaRepository.findById("REDUZIDA").orElseThrow());
        artigoRepository.saveAndFlush(artigo);
        Moeda moeda = moedaRepository.findById("EUR").orElseThrow();
        moeda.setSimbolo("€");
        moeda.setNdecimais(3);
        moedaRepository.saveAndFlush(moeda);
        Serie serie = serieRepository.findById(new SerieId("IC1", "LOCK")).orElseThrow();
        serie.setNome("Série alterada");
        serieRepository.saveAndFlush(serie);

        var impressao = documentoService.getImpressao(id);
        assertThat(impressao.empresa().nome()).isEqualTo("FAC Integridade, Lda.");
        assertThat(impressao.empresa().nif()).isEqualTo("509999999");
        assertThat(impressao.empresa().iban()).isEqualTo("PT50000201231234567890154");
        assertThat(impressao.documento().clienteNome()).isEqualTo("Cliente Integridade");
        assertThat(impressao.documento().clienteNif()).isEqualTo("509654321");
        assertThat(impressao.documento().serieDescricao()).isEqualTo("Série concorrente");
        assertThat(impressao.documento().moedaSimbolo()).isEqualTo("EUR");
        assertThat(impressao.documento().moedaCasasDecimais()).isEqualTo(2);
        assertThat(impressao.linhas().getFirst().artigoId()).isEqualTo("ARTINT");
        assertThat(impressao.linhas().getFirst().descricao()).isEqualTo("Artigo Integridade");
        assertThat(impressao.linhas().getFirst().unidade()).isEqualTo("UN");
        assertThat(impressao.linhas().getFirst().tipoTaxaIvaId()).isEqualTo("NORMAL");

        byte[] pdfAnterior = mockMvc.perform(get("/documentos-comerciais/{id}/pdf", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(pdfText(pdfAnterior))
                .contains("FAC Integridade, Lda.", "Rodapé histórico A", "PT50000201231234567890154")
                .doesNotContain("Empresa alterada");

        Long novoId = criarRascunho("LOCK");
        emitir(novoId).call();
        byte[] pdfNovo = mockMvc.perform(get("/documentos-comerciais/{id}/pdf", novoId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(pdfText(pdfNovo)).contains("Empresa alterada").doesNotContain("FAC Integridade, Lda.");
    }

    @Test
    void snapshotCompletoConsolidaNovoDocumentoELegadoPermaneceIncompleto() throws Exception {
        Long id = criarRascunho("LOCK");
        emitir(id).call();
        DocumentoComercial emitido = documentoRepository.findById(id).orElseThrow();
        LinhaDocumentoComercial linha = linhaRepository
                .findByDocumentoComercialIdOrderByNumeroLinha(id).getFirst();

        assertThat(emitido.getFiscalSnapshotVersion()).isEqualTo(2);
        assertThat(emitido.isFiscalmenteConsolidado()).isTrue();
        assertThat(linha.getArtigoCodigo()).isEqualTo("ARTINT");
        assertThat(linha.getBaseTributavel()).isEqualByComparingTo("10.000000");
        assertThat(linha.getValorImposto()).isEqualByComparingTo("2.300000");
        assertThat(linha.getTotalLinha()).isEqualByComparingTo("12.300000");

        ReflectionTestUtils.setField(emitido, "emitenteNome", null);
        assertThat(emitido.isFiscalmenteConsolidado()).isFalse();

        Long legadoId = criarRascunho("LOCK");
        DocumentoComercial legado = documentoRepository.findById(legadoId).orElseThrow();
        legado.setNumeroDocumento(999L);
        legado.setEstado(EstadoDocumentoComercial.EMITIDO);
        documentoRepository.saveAndFlush(legado);
        assertThat(legado.getFiscalSnapshotVersion()).isNull();
        assertThat(legado.isFiscalmenteConsolidado()).isFalse();
    }

    @Test
    void emissaoRecalculaTotaisEPersisteIdentidadeAtcudQr() throws Exception {
        Long id = criarRascunho("LOCK");
        DocumentoComercial corrompido = documentoRepository.findById(id).orElseThrow();
        corrompido.setValorBruto(BigDecimal.ZERO);
        corrompido.setValorTotal(BigDecimal.ZERO);
        documentoRepository.saveAndFlush(corrompido);

        mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emissorId\":\"EMITINT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EMITIDO"))
                .andExpect(jsonPath("$.numeroDocumento").value(1))
                .andExpect(jsonPath("$.numeroDocumentoCompleto").value("IC1 LOCK/1"))
                .andExpect(jsonPath("$.atcud").value("IC12026-1"))
                .andExpect(jsonPath("$.fiscalmenteConsolidado").value(true))
                .andExpect(jsonPath("$.temQrFiscal").value(true))
                .andExpect(jsonPath("$.qrPayload").isNotEmpty())
                .andExpect(jsonPath("$.valorBruto").value(10.000000))
                .andExpect(jsonPath("$.valorTotal").value(12.300000));
    }

    @Test
    void falhaNoAtcudReverteDocumentoENumerador() throws Exception {
        TipoDocumento tipo = tipoDocumentoRepository.findById("IC1").orElseThrow();
        serieRepository.save(new Serie(tipo, "BADAT", "Série ATCUD inválido", "ATCUD:INVALID", LocalDate.of(2026, 1, 1)));
        Long id = criarRascunho("BADAT");

        mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emissorId\":\"EMITINT\"}"))
                .andExpect(status().isBadRequest());

        assertRascunhoSemDadosFiscais(id, "BADAT");
    }

    @Test
    void falhaNoPayloadQrReverteDocumentoENumerador() throws Exception {
        TipoDocumento tipo = tipoDocumentoRepository.findById("IC1").orElseThrow();
        serieRepository.save(new Serie(tipo, "BADQR", "Série QR inválido", "IC1QR", LocalDate.of(2026, 1, 1)));
        Long id = criarRascunho("BADQR");
        Object originalHash = ReflectionTestUtils.getField(fiscalQrService, "hashCharacters");
        try {
            ReflectionTestUtils.setField(fiscalQrService, "hashCharacters", "BAD");
            mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"emissorId\":\"EMITINT\"}"))
                    .andExpect(status().isBadRequest());
        } finally {
            ReflectionTestUtils.setField(fiscalQrService, "hashCharacters", originalHash);
        }

        assertRascunhoSemDadosFiscais(id, "BADQR");
    }

    @Test
    void repeticaoSequencialDevolveConflitoSemConsumirNumero() throws Exception {
        Long id = criarRascunho("LOCK");
        emitir(id).call();

        mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emissorId\":\"EMITINT\"}"))
                .andExpect(status().isConflict());

        assertThat(serie("LOCK").getNumerador()).isEqualTo(1L);
        assertThat(documentoRepository.findById(id).orElseThrow().getNumeroDocumento()).isEqualTo(1L);
    }

    @Test
    void duasEmissoesConcorrentesDoMesmoRascunhoConsomemUmNumero() throws Exception {
        Long id = criarRascunho("LOCK");
        List<Integer> statuses = executarConcorrentemente(List.of(emitir(id), emitir(id)));

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        DocumentoComercial documento = documentoRepository.findById(id).orElseThrow();
        assertThat(serie("LOCK").getNumerador()).isEqualTo(1L);
        assertThat(documento.getNumeroDocumento()).isEqualTo(1L);
        assertThat(documento.getNumeroDocumentoCompleto()).isEqualTo("IC1 LOCK/1");
        assertThat(documento.getAtcud()).isEqualTo("IC12026-1");
        assertThat(documento.getQrPayload()).isNotBlank();
    }

    @Test
    void documentosDiferentesConcorrentesRecebemNumerosSequenciais() throws Exception {
        Long primeiro = criarRascunho("LOCK");
        Long segundo = criarRascunho("LOCK");
        List<Integer> statuses = executarConcorrentemente(List.of(emitir(primeiro), emitir(segundo)));

        assertThat(statuses).containsExactlyInAnyOrder(200, 200);
        assertThat(serie("LOCK").getNumerador()).isEqualTo(2L);
        assertThat(List.of(
                documentoRepository.findById(primeiro).orElseThrow().getNumeroDocumento(),
                documentoRepository.findById(segundo).orElseThrow().getNumeroDocumento()
        )).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void pdfUsaApenasPayloadPersistidoENaoAlteraDocumento() throws Exception {
        Long id = criarRascunho("LOCK");
        emitir(id).call();
        DocumentoComercial antes = documentoRepository.findById(id).orElseThrow();
        String payload = antes.getQrPayload();

        mockMvc.perform(get("/documentos-comerciais/{id}/pdf", id))
                .andExpect(status().isOk());

        DocumentoComercial depois = documentoRepository.findById(id).orElseThrow();
        assertThat(depois.getQrPayload()).isEqualTo(payload);
        assertThat(depois.getAtcud()).isEqualTo(antes.getAtcud());
        assertThat(depois.isImpresso()).isFalse();
    }

    @Test
    void pdfDeLegadoSemQrFalhaSemFabricarDados() throws Exception {
        Long id = criarRascunho("LOCK");
        DocumentoComercial legado = documentoRepository.findById(id).orElseThrow();
        legado.setNumeroDocumento(77L);
        legado.setEstado(EstadoDocumentoComercial.EMITIDO);
        documentoRepository.saveAndFlush(legado);

        mockMvc.perform(get("/documentos-comerciais/{id}/pdf", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Documento legado sem payload QR fiscal consolidado; o PDF fiscal não pode ser gerado"));

        DocumentoComercial depois = documentoRepository.findById(id).orElseThrow();
        assertThat(depois.getNumeroDocumento()).isEqualTo(77L);
        assertThat(depois.getNumeroDocumentoCompleto()).isNull();
        assertThat(depois.getAtcud()).isNull();
        assertThat(depois.getQrPayload()).isNull();
    }

    @Test
    void pdfDeNovoDocumentoInconsistenteSemQrFalhaSemOReparar() throws Exception {
        Long id = criarRascunho("LOCK");
        DocumentoComercial inconsistente = documentoRepository.findById(id).orElseThrow();
        inconsistente.setNumeroDocumento(88L);
        inconsistente.atribuirNumeroDocumentoCompleto("IC1 LOCK/88");
        inconsistente.atribuirAtcud("IC12026", "IC12026-88");
        inconsistente.setEstado(EstadoDocumentoComercial.EMITIDO);
        documentoRepository.saveAndFlush(inconsistente);

        mockMvc.perform(get("/documentos-comerciais/{id}/pdf", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Documento emitido inconsistente: payload QR fiscal consolidado em falta"));

        DocumentoComercial depois = documentoRepository.findById(id).orElseThrow();
        assertThat(depois.getNumeroDocumento()).isEqualTo(88L);
        assertThat(depois.getAtcud()).isEqualTo("IC12026-88");
        assertThat(depois.getQrPayload()).isNull();
    }

    @Test
    void limpezaDaFixtureConcorrentePreservaDocumentoDeOutroTipo() throws Exception {
        TipoDocumento outroTipo = new TipoDocumento(
                "IC2", "Documento alheio à fixture", null, null, null, null, 1, 1, 1, false);
        outroTipo.setCodigoFiscal("FT");
        tipoDocumentoRepository.save(outroTipo);
        serieRepository.save(new Serie(
                outroTipo, "SAFE", "Série alheia", "IC22026", LocalDate.of(2026, 1, 1)));
        Long documentoAlheio = criarRascunho("IC2", "SAFE");

        cleanupDocumentsForType("IC1");

        assertThat(documentoRepository.existsById(documentoAlheio)).isTrue();
    }

    private Long criarRascunho(String serie) throws Exception {
        return criarRascunho("IC1", serie);
    }

    private Long criarRascunho(String tipoDocumentoId, String serie) throws Exception {
        String location = mockMvc.perform(post("/documentos-comerciais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "documento": {
                                    "tipoDocumentoId": "%s",
                                    "serie": "%s",
                                    "dataEmissao": "2026-06-22",
                                    "clienteId": %d,
                                    "armazemCargaId": %d,
                                    "pPagamentoId": "P30"
                                  },
                                  "linha": {
                                    "artigoId": "ARTINT",
                                    "quantidade": 1,
                                    "precoUnitario": 10
                                  }
                                }
                                """.formatted(tipoDocumentoId, serie, cliente.getId(), armazem.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private Callable<Integer> emitir(Long id) {
        return () -> mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emissorId\":\"EMITINT\"}"))
                .andReturn().getResponse().getStatus();
    }

    private List<Integer> executarConcorrentemente(List<Callable<Integer>> operacoes) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(operacoes.size());
        CountDownLatch preparados = new CountDownLatch(operacoes.size());
        CountDownLatch iniciar = new CountDownLatch(1);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (Callable<Integer> operacao : operacoes) {
                futures.add(executor.submit(() -> {
                    preparados.countDown();
                    iniciar.await();
                    return operacao.call();
                }));
            }
            preparados.await();
            iniciar.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statuses.add(future.get());
            }
            return statuses;
        } finally {
            executor.shutdownNow();
        }
    }

    private void assertRascunhoSemDadosFiscais(Long id, String serie) {
        DocumentoComercial documento = documentoRepository.findById(id).orElseThrow();
        assertThat(documento.getEstado()).isEqualTo(EstadoDocumentoComercial.RASCUNHO);
        assertThat(documento.getNumeroDocumento()).isNull();
        assertThat(documento.getNumeroDocumentoCompleto()).isNull();
        assertThat(documento.getAtcud()).isNull();
        assertThat(documento.getQrPayload()).isNull();
        assertThat(serie(serie).getNumerador()).isZero();
    }

    private Serie serie(String codigo) {
        return serieRepository.findById(new SerieId("IC1", codigo)).orElseThrow();
    }

    private void prepararEmpresa(CodPostal codPostal, Pais pais) {
        Empresa empresa = empresaRepository.findById(Empresa.EMPRESA_ID).orElseGet(Empresa::new);
        empresa.setNome("FAC Integridade, Lda.");
        empresa.setNif("509999999");
        empresa.setMorada("Rua da Empresa");
        empresa.setCodPostal(codPostal);
        empresa.setLocalidade("Águeda");
        empresa.setPais(pais);
        empresa.setCapitalSocial(new BigDecimal("10000.00"));
        empresa.setMatriculaRegistoComercial("CRC Águeda 1");
        empresa.setCae("62010");
        empresa.setDescricaoCae("Programação");
        empresa.setEmail("fac.integridade@fac.test");
        empresa.setWeb("https://fac.test");
        empresa.setIban("PT50000201231234567890154");
        empresa.setBicSwift("CGDIPTPL");
        empresa.setTextoRodape("Rodapé histórico A");
        empresa.setObservacoesLegais("Observação legal A");
        empresaRepository.save(empresa);
    }

    private String pdfText(byte[] content) throws Exception {
        try (var document = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private void cleanupFixture() {
        cleanupDocumentsForType("IC1");
        cleanupDocumentsForType("IC2");
        jdbcTemplate.update("delete from artigo where codigo = 'ARTINT'");
        jdbcTemplate.update("delete from cliente where email = 'cliente.integridade@fac.test'");
        jdbcTemplate.update("delete from armazem where nome = 'Armazém Integridade'");
        jdbcTemplate.update("delete from mpagamento where nome = 'Transferência integridade'");
        jdbcTemplate.update("delete from transporte where nome = 'Transporte integridade'");
        jdbcTemplate.update("delete from familia where descricao = 'Integridade'");
        jdbcTemplate.update("delete from utilizador where codigo = 'EMITINT' or email = 'emissor.integridade@fac.test'");
    }

    private void cleanupDocumentsForType(String tipoDocumentoId) {
        jdbcTemplate.update("""
                delete from pendente
                where id_documento_comercial in (
                    select id from documento_comercial where id_tipo_documento = ?
                )
                """, tipoDocumentoId);
        jdbcTemplate.update("""
                delete from linha_documento_comercial
                where id_documento_comercial in (
                    select id from documento_comercial where id_tipo_documento = ?
                )
                """, tipoDocumentoId);
        jdbcTemplate.update("delete from documento_comercial where id_tipo_documento = ?", tipoDocumentoId);
        jdbcTemplate.update("delete from serie where id_tipo_documento = ?", tipoDocumentoId);
        jdbcTemplate.update("delete from tipodocumento where id = ?", tipoDocumentoId);
    }
}
