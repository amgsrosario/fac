package com.ar2lda.fac;

import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import com.ar2lda.fac.service.FiscalQrService;
import com.ar2lda.fac.financial.FinancialCore;
import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.boot.test.mock.mockito.SpyBean;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(com.ar2lda.fac.testinfra.FiscalSignatureTestConfiguration.class)
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
    @Autowired private com.ar2lda.fac.testinfra.FiscalSignatureTestConfiguration.TestSource signatureSource;
    @Autowired private DocumentoComercialService documentoService;
    @Autowired private JdbcTemplate jdbcTemplate;
    @SpyBean private FinancialCore financialCore;
    @Autowired private org.springframework.core.env.Environment testEnvironment;

    private String primaryType;
    private String secondaryType;
    private final List<String> fixtureTypes = new ArrayList<>();

    private String unusedType() {
        String code;
        do { code=java.util.UUID.randomUUID().toString().substring(0,3).toUpperCase(); } while(tipoDocumentoRepository.existsById(code) || fixtureTypes.contains(code));
        fixtureTypes.add(code);
        return code;
    }

    private Cliente cliente;
    private Armazem armazem;

    @BeforeEach
    void setup() {
        cleanupFixture();
        primaryType=unusedType(); secondaryType=unusedType();

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
        modo.setId("INT");
        modo.setNome("Transferência integridade");
        modo = mPagamentoRepository.save(modo);
        Transporte transporte = transporteRepository.save(new Transporte("INT", "Transporte integridade"));

        TipoDocumento tipo = new TipoDocumento(
                primaryType, "Integridade emissão", null, null, null, null, 1, 1, 1, false);
        tipo.setCodigoFiscal("FT");
        tipo = com.ar2lda.fac.testinfra.GovernedTypeFixtures.bind(jdbcTemplate, tipo);
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

        armazem = new Armazem("T02", "Armazém Integridade", "Rua Armazém", null, "Águeda");
        armazem.setCodPostal(codPostal);
        armazem.setPais(pais);
        armazem = armazemRepository.save(armazem);

        Familia familia = familiaRepository.save(new Familia("Integridade"));
        Artigo artigo = new Artigo("ARTINT");
        artigo.setDescricao("Artigo Integridade");
        artigo.setTipoArtigo(TipoArtigo.ARTIGO);
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
        Serie serie = serieRepository.findById(new SerieId(primaryType, "LOCK")).orElseThrow();
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

        assertThat(emitido.getFiscalSnapshotVersion()).isEqualTo(3);
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
                .andExpect(jsonPath("$.numeroDocumentoCompleto").value(primaryType+" LOCK/1"))
                .andExpect(jsonPath("$.atcud").value("IC12026-1"))
                .andExpect(jsonPath("$.fiscalmenteConsolidado").value(true))
                .andExpect(jsonPath("$.temQrFiscal").value(true))
                .andExpect(jsonPath("$.qrPayload").isNotEmpty())
                .andExpect(jsonPath("$.valorBruto").value(10.000000))
                .andExpect(jsonPath("$.valorTotal").value(12.300000));
        assertThat(originCount(id)).isEqualTo(1L);
        var position=jdbcTemplate.queryForMap("select p.*,f.actor from financial_document_origin o join financial_position p on p.id=o.position_id join financial_operation f on f.id=p.creation_id where o.document_id=?",id);
        assertThat(position.get("nature")).isEqualTo("DEBT");
        assertThat((BigDecimal)position.get("original")).isEqualByComparingTo("12.30");
        assertThat((BigDecimal)position.get("available")).isEqualByComparingTo("12.30");
        assertThat(position.get("cliente_id")).isEqualTo(cliente.getId());
        assertThat(position.get("actor")).isEqualTo("EMITINT");
        assertThat(jdbcTemplate.queryForObject("select valor_pendente from pendente where id_documento_comercial=?",BigDecimal.class,id)).isEqualByComparingTo("12.30");
    }

    @Test
    void falhaNoAtcudReverteDocumentoENumerador() throws Exception {
        TipoDocumento tipo = tipoDocumentoRepository.findById(primaryType).orElseThrow();
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
        TipoDocumento tipo = tipoDocumentoRepository.findById(primaryType).orElseThrow();
        serieRepository.save(new Serie(tipo, "BADQR", "Série QR inválido", "IC1QR", LocalDate.of(2026, 1, 1)));
        Long id = criarRascunho("BADQR");
        boolean originalInvalid = signatureSource.invalidSignature;
        try {
            signatureSource.invalidSignature = true;
            mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"emissorId\":\"EMITINT\"}"))
                    .andExpect(status().isBadRequest());
        } finally {
            signatureSource.invalidSignature = originalInvalid;
        }

        assertRascunhoSemDadosFiscais(id, "BADQR");
    }

    @Test
    void ausenciaDeAssinaturaFiscalRecusaEmissaoSemConsumirNumero() throws Exception {
        TipoDocumento tipo = tipoDocumentoRepository.findById(primaryType).orElseThrow();
        serieRepository.save(new Serie(tipo, "NOSIG", "Fonte ausente", "IC1NOSIG", LocalDate.of(2026, 1, 1)));
        Long id = criarRascunho("NOSIG");
        try {
            signatureSource.unavailable = true;
            mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"emissorId\":\"EMITINT\"}"))
                    .andExpect(status().isConflict());
        } finally { signatureSource.unavailable = false; }
        assertRascunhoSemDadosFiscais(id, "NOSIG");
    }

    @Test
    void repeticaoSequencialDevolveConflitoSemConsumirNumero() throws Exception {
        Long id = criarRascunho("LOCK");
        emitir(id).call();

        mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emissorId\":\"EMITINT\"}"))
                .andExpect(status().isConflict());

        assertThat(originCount(id)).isEqualTo(1L);
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
        assertThat(documento.getNumeroDocumentoCompleto()).isEqualTo(primaryType+" LOCK/1");
        assertThat(documento.getAtcud()).isEqualTo("IC12026-1");
        assertThat(documento.getQrPayload()).isNotBlank();
        assertThat(originCount(id)).isEqualTo(1L);
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
        inconsistente.atribuirNumeroDocumentoCompleto(primaryType+" LOCK/88");
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
                secondaryType, "Documento alheio à fixture", null, null, null, null, 1, 1, 1, false);
        outroTipo.setCodigoFiscal("FT");
        outroTipo = com.ar2lda.fac.testinfra.GovernedTypeFixtures.bind(jdbcTemplate, outroTipo);
        tipoDocumentoRepository.save(outroTipo);
        serieRepository.save(new Serie(
                outroTipo, "SAFE", "Série alheia", "IC22026", LocalDate.of(2026, 1, 1)));
        Long documentoAlheio = criarRascunho(secondaryType, "SAFE");

        cleanupDocumentsForType(primaryType);

        assertThat(documentoRepository.existsById(documentoAlheio)).isTrue();
    }

    @Test
    void fundacaoNaoLiquidaPreservaTaxaImpostoFundamentoPdfQrEHistorico() throws Exception {
        Long id = criarRascunho("LOCK");
        Long linhaId = linhaRepository.findByDocumentoComercialIdOrderByNumeroLinha(id).getFirst().getId();
        mockMvc.perform(put("/documentos-comerciais/{id}/linhas/{linha}", id, linhaId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"artigoId\":\"ARTINT\",\"quantidade\":5,\"precoUnitario\":10,\"tipoTaxaIvaId\":\"REDUZIDA\"}"))
                .andExpect(status().isNoContent());
        configurarNaoLiquidacao();
        mudarRegime(id, "FNL");
        mockMvc.perform(get("/documentos-comerciais/{id}/linhas", id))
                .andExpect(jsonPath("$[0].percentagemIva").value(6))
                .andExpect(jsonPath("$[0].resultadoFiscal.ivaCalculado").value(3))
                .andExpect(jsonPath("$[0].resultadoFiscal.ivaLiquidado").value(0))
                .andExpect(jsonPath("$[0].resultadoFiscal.totalLinha").value(50))
                .andExpect(jsonPath("$[0].resultadoFiscal.mIsencaoCodigo").value("M16"));
        mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"emissorId\":\"EMITINT\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(50));
        var antes = documentoService.getById(id);
        assertThat(antes.qrPayload()).contains("I2:50.00", "N:0.00", "O:50.00");
        assertThat(pdfText(mockMvc.perform(get("/documentos-comerciais/{id}/pdf", id)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray())).contains("IVA calculado", "IVA liquidado", "M16");
        jdbcTemplate.update("update riva set fundamento_fiscal='ALTERADO', tratamento_liquidacao='NORMAL' where id='FNL'");
        jdbcTemplate.update("update misencao set nome='ALTERADO' where id='M16'");
        jdbcTemplate.update("update riva_taxa set valor=8 where id_riva='FNL' and id_tipo_taxa_iva='REDUZIDA'");
        jdbcTemplate.update("update artigo set id_iva_venda='NORMAL' where codigo='ARTINT'");
        jdbcTemplate.update("update tipotaxaiva set descricao='ALTERADA' where id='REDUZIDA'");
        assertThat(documentoService.getById(id).qrPayload()).isEqualTo(antes.qrPayload());
        mockMvc.perform(get("/documentos-comerciais/{id}/linhas", id))
                .andExpect(jsonPath("$[0].resultadoFiscal.ivaCalculado").value(3))
                .andExpect(jsonPath("$[0].resultadoFiscal.fundamentoFiscal").value("IVA Isento Art.º 14.º do RITI"));
        assertThat(pdfText(mockMvc.perform(get("/documentos-comerciais/{id}/pdf", id)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray())).contains("IVA calculado", "M16", "IVA Isento");
        mockMvc.perform(post("/documentos-comerciais/{id}/recalcular-fiscal", id)).andExpect(status().isConflict());
    }

    @Test
    void rascunhoLegacyRecalculaAvisaEExigeConfirmacaoSemAlterarEmitidos() throws Exception {
        Long id = criarRascunho("LOCK");
        jdbcTemplate.update("update linha_documento_comercial set preco_unitario=.335,valor_bruto=.335,valor_desconto=0,valor_linha=.335 where id_documento_comercial=?", id);
        jdbcTemplate.update("update documento_comercial set fiscal_motor_version=null,valor_total=.41205 where id=?", id);
        mockMvc.perform(post("/documentos-comerciais/{id}/recalcular-fiscal", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(.42))
                .andExpect(jsonPath("$.resultadoFiscal.recalculoAviso").value(true));
        mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"emissorId\":\"EMITINT\"}")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"emissorId\":\"EMITINT\",\"recalculoConfirmado\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(.42));
    }

    @Test
    void emissaoNaoEurRecusaQrSemConversaoMasPreservaMoedaDoRascunho() throws Exception {
        moedaRepository.saveAndFlush(new Moeda("USD", "US Dollar", BigDecimal.ONE, BigDecimal.ONE, "$", 2, "USD"));
        Long id = criarRascunho("LOCK");
        jdbcTemplate.update("update documento_comercial set id_moeda='USD' where id=?", id);
        mockMvc.perform(post("/documentos-comerciais/{id}/emitir", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"emissorId\":\"EMITINT\"}")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/documentos-comerciais/{id}", id)).andExpect(jsonPath("$.moedaId").value("USD"))
                .andExpect(jsonPath("$.estado").value("RASCUNHO"));
    }

    @Test
    void retencaoNaoSuportadaRecusaRecalculoSemModificarValorLegacy() throws Exception {
        Long id = criarRascunho("LOCK");
        jdbcTemplate.update("update documento_comercial set valor_retencao=1 where id=?", id);
        mockMvc.perform(post("/documentos-comerciais/{id}/recalcular-fiscal", id))
                .andExpect(status().isBadRequest());
        assertThat(jdbcTemplate.queryForObject("select valor_retencao from documento_comercial where id=?", BigDecimal.class, id))
                .isEqualByComparingTo("1");
    }

    @Test
    void previewEGravacaoRejeitamPrecisaoSuperiorASeisCasas() throws Exception {
        Long id = criarRascunho("LOCK");
        String linha = "{\"artigoId\":\"ARTINT\",\"quantidade\":1,\"precoUnitario\":0.3350001}";
        mockMvc.perform(post("/documentos-comerciais/preview-fiscal").contentType(MediaType.APPLICATION_JSON)
                .content("{\"rivaId\":\"CON\",\"dataEmissao\":\"2026-06-22\",\"linhas\":[" + linha + "]}"))
                .andExpect(status().isBadRequest());
        long antes = linhaRepository.count();
        mockMvc.perform(post("/documentos-comerciais/{id}/linhas", id).contentType(MediaType.APPLICATION_JSON)
                .content(linha)).andExpect(status().isBadRequest());
        assertThat(linhaRepository.count()).isEqualTo(antes);
    }

    @Test
    void previewFiscalNaoPersisteEFechaTresLinhasAoCentimo() throws Exception {
        String linha = "{\"artigoId\":\"ARTINT\",\"quantidade\":1,\"precoUnitario\":0.01}";
        long antes = documentoRepository.count();
        mockMvc.perform(post("/documentos-comerciais/preview-fiscal").contentType(MediaType.APPLICATION_JSON)
                .content("{\"rivaId\":\"CON\",\"dataEmissao\":\"2026-06-22\",\"linhas\":["+linha+","+linha+","+linha+"]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totais.valorTotal").value(.03))
                .andExpect(jsonPath("$.totais.valorIvaTotal").value(0));
        assertThat(documentoRepository.count()).isEqualTo(antes);
    }

    @Test
    void alterarRivaReavaliaTodasLinhasSemDirtyEIsencaoPropriaPersisteFundamento() throws Exception {
        Long id = criarRascunho("LOCK");
        mockMvc.perform(post("/documentos-comerciais/{id}/linhas",id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"artigoId\":\"ARTINT\",\"quantidade\":5,\"precoUnitario\":10,\"tipoTaxaIvaId\":\"REDUZIDA\"}"))
                .andExpect(status().isCreated());
        configurarNaoLiquidacao();
        jdbcTemplate.update("insert into riva_taxa(id_riva,id_tipo_taxa_iva,valor) values('FNL','NORMAL',23) on conflict(id_riva,id_tipo_taxa_iva) do update set valor=23");
        mudarRegime(id,"FNL");
        mockMvc.perform(get("/documentos-comerciais/{id}/linhas",id))
                .andExpect(jsonPath("$[0].resultadoFiscal.ivaCalculado").value(2.3))
                .andExpect(jsonPath("$[0].resultadoFiscal.ivaLiquidado").value(0))
                .andExpect(jsonPath("$[1].resultadoFiscal.ivaCalculado").value(3))
                .andExpect(jsonPath("$[1].resultadoFiscal.ivaLiquidado").value(0));
        mockMvc.perform(get("/documentos-comerciais/{id}",id)).andExpect(jsonPath("$.valorTotal").value(60));
        jdbcTemplate.update("update artigo set id_iva_venda='ISENTA',id_misencao='M07',fundamento_fiscal='Isento artigo 9.º do CIVA' where codigo='ARTINT'");
        Long isento=criarRascunho("LOCK");
        mockMvc.perform(post("/documentos-comerciais/{id}/emitir",isento).contentType(MediaType.APPLICATION_JSON)
                .content("{\"emissorId\":\"EMITINT\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorIvaTotal").value(0));
        mockMvc.perform(get("/documentos-comerciais/{id}/linhas",isento))
                .andExpect(jsonPath("$[0].resultadoFiscal.mIsencaoCodigo").value("M07"))
                .andExpect(jsonPath("$[0].resultadoFiscal.taxaAplicavel").value(0));
    }

    @Test
    void territorioMadeiraEClienteEstrangeiroMantemMoedaIndependente() throws Exception {
        Pais estrangeiro=paisRepository.findById("ES").orElseGet(()->paisRepository.save(new Pais("ES","Espanha")));
        cliente.setPais(estrangeiro);cliente.setNif("ESB123456789");cliente.setCodPostal(null);clienteRepository.save(cliente);
        jdbcTemplate.update("insert into riva(id,nome,territorio_fiscal) values('FMD','Madeira teste','MADEIRA') on conflict(id) do nothing");
        Long id=criarRascunho("LOCK");
        Long linha=linhaRepository.findByDocumentoComercialIdOrderByNumeroLinha(id).getFirst().getId();
        mockMvc.perform(put("/documentos-comerciais/{id}/linhas/{linha}",id,linha).contentType(MediaType.APPLICATION_JSON)
                .content("{\"artigoId\":\"ARTINT\",\"quantidade\":5,\"precoUnitario\":10,\"tipoTaxaIvaId\":\"REDUZIDA\"}"))
                .andExpect(status().isNoContent());
        mudarRegime(id,"FMD");
        mockMvc.perform(post("/documentos-comerciais/{id}/emitir",id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"emissorId\":\"EMITINT\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(52))
                .andExpect(jsonPath("$.moedaId").value("EUR")).andExpect(jsonPath("$.fiscalmenteConsolidado").value(true));
        assertThat(documentoService.getById(id).qrPayload()).contains("I1:PT-MA", "B:ESB123456789", "C:ES", "I4:2.00");
    }

    private long originCount(Long id) {
        return jdbcTemplate.queryForObject("select count(*) from financial_document_origin where document_id=?",Long.class,id);
    }

    @Test
    void financialOriginFailureRollsBackInvoicePendingNumberAndCreatedHistory() throws Exception {
        Long id=criarRascunho("LOCK");
        doAnswer(invocation -> { invocation.callRealMethod(); throw new BadRequestException("OP21 simulated origin failure"); })
                .when(financialCore).create(any(),anyString(),any(),anyString(),any(),any(),anyString());
        mockMvc.perform(post("/documentos-comerciais/{id}/emitir",id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"emissorId\":\"EMITINT\"}")).andExpect(status().isBadRequest());
        assertRascunhoSemDadosFiscais(id,"LOCK");
        assertThat(jdbcTemplate.queryForObject("select count(*) from financial_operation",Long.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("select count(*) from financial_position",Long.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("select count(*) from pendente where id_documento_comercial=?",Long.class,id)).isZero();
    }

    @Test
    void cancellationReversesOriginOnceAndPreservesSnapshot() throws Exception {
        Long id=criarRascunho("LOCK");assertThat(emitir(id).call()).isEqualTo(200);
        var before=documentoRepository.findById(id).orElseThrow();String qr=before.getQrPayload();
        assertThat(originCount(id)).isEqualTo(1L);
        mockMvc.perform(post("/documentos-comerciais/{id}/anular",id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Anulação OP21 teste\"}")).andExpect(status().isOk());
        assertThat(jdbcTemplate.queryForObject("select count(*) from financial_operation where kind='REVERSE'",Long.class)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("select cancelled from financial_position p join financial_document_origin o on o.position_id=p.id where o.document_id=?",Boolean.class,id)).isTrue();
        assertThat(documentoRepository.findById(id).orElseThrow().getQrPayload()).isEqualTo(qr);
        mockMvc.perform(post("/documentos-comerciais/{id}/anular",id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Anulação OP21 teste\"}")).andExpect(status().isConflict());
        assertThat(jdbcTemplate.queryForObject("select count(*) from financial_operation where kind='REVERSE'",Long.class)).isEqualTo(1L);
    }

    @Test
    void excludedDocumentTypesDoNotOriginateAndDraftRemainsUnlinked() throws Exception {
        for(String code:List.of("FR","NC","ND")) {
            String idType=unusedType();
            var type=new TipoDocumento(idType,"Tipo excluído",code,null,null,null,null,1,1,1,code.equals("FR"));
            tipoDocumentoRepository.saveAndFlush(type);
            serieRepository.save(new Serie(type,"LOCK","Série excluída","EXC2026",LocalDate.of(2026,1,1)));
            Long id=criarRascunho(idType,"LOCK");assertThat(originCount(id)).isZero();
            assertThat(emitir(id).call()).isEqualTo(200);assertThat(originCount(id)).isZero();
        }
    }

    @Test
    void reversalFailureRollsBackCommercialAndFinancialCancellation() throws Exception {
        Long id=criarRascunho("LOCK");assertThat(emitir(id).call()).isEqualTo(200);
        doAnswer(invocation -> { invocation.callRealMethod(); throw new BadRequestException("OP21 simulated reversal failure"); })
                .when(financialCore).reverse(any(),anyString(),anyLong(),anyString(),anyString());
        mockMvc.perform(post("/documentos-comerciais/{id}/anular",id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Anulação OP21 teste\"}")).andExpect(status().isBadRequest());
        assertThat(documentoRepository.findById(id).orElseThrow().getEstado()).isEqualTo(EstadoDocumentoComercial.EMITIDO);
        assertThat(jdbcTemplate.queryForObject("select cancelled from financial_position p join financial_document_origin o on o.position_id=p.id where o.document_id=?",Boolean.class,id)).isFalse();
        assertThat(jdbcTemplate.queryForObject("select valor_pendente from pendente where id_documento_comercial=?",BigDecimal.class,id)).isEqualByComparingTo("12.30");
        assertThat(jdbcTemplate.queryForObject("select count(*) from financial_operation where kind='REVERSE'",Long.class)).isZero();
    }

    @Test
    void historicalEmissionWithoutOriginCanBeCancelledWithoutBackfill() throws Exception {
        Long id=criarRascunho("LOCK");
        var legacy=documentoRepository.findById(id).orElseThrow();
        legacy.setNumeroDocumento(91L);legacy.atribuirNumeroDocumentoCompleto(primaryType+" LOCK/91");
        legacy.setEmissor(utilizadorRepository.findById("EMITINT").orElseThrow());
        legacy.setEstado(EstadoDocumentoComercial.EMITIDO);documentoRepository.saveAndFlush(legacy);
        assertThat(originCount(id)).isZero();
        mockMvc.perform(post("/documentos-comerciais/{id}/anular",id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Anulação histórica teste\"}")).andExpect(status().isOk());
        assertThat(originCount(id)).isZero();
        assertThat(jdbcTemplate.queryForObject("select count(*) from financial_operation",Long.class)).isZero();
    }

    private void configurarNaoLiquidacao() {
        jdbcTemplate.update("insert into riva(id,nome,mercado,tratamento_liquidacao,fundamento_fiscal,id_misencao) values('FNL','Não liquidação teste','INTRACOMUNITARIO','NAO_LIQUIDAR','IVA Isento Art.º 14.º do RITI','M16') on conflict(id) do update set tratamento_liquidacao='NAO_LIQUIDAR',fundamento_fiscal='IVA Isento Art.º 14.º do RITI'");
        jdbcTemplate.update("insert into riva_taxa(id_riva,id_tipo_taxa_iva,valor) values('FNL','REDUZIDA',6) on conflict(id_riva,id_tipo_taxa_iva) do update set valor=6");
    }

    private void mudarRegime(Long id, String regime) throws Exception {
        mockMvc.perform(put("/documentos-comerciais/{id}",id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"dataEmissao\":\"2026-06-22\",\"armazemCargaId\":\""+armazem.getId()+"\",\"rivaId\":\""+regime+"\"}"))
                .andExpect(status().isNoContent());
    }

    private Long criarRascunho(String serie) throws Exception {
        return criarRascunho(primaryType, serie);
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
                                    "armazemCargaId": "%s",
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
        assertThat(originCount(id)).isZero();
        DocumentoComercial documento = documentoRepository.findById(id).orElseThrow();
        assertThat(documento.getEstado()).isEqualTo(EstadoDocumentoComercial.RASCUNHO);
        assertThat(documento.getNumeroDocumento()).isNull();
        assertThat(documento.getNumeroDocumentoCompleto()).isNull();
        assertThat(documento.getAtcud()).isNull();
        assertThat(documento.getQrPayload()).isNull();
        assertThat(serie(serie).getNumerador()).isZero();
    }

    private Serie serie(String codigo) {
        return serieRepository.findById(new SerieId(primaryType, codigo)).orElseThrow();
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
        // Immutable histories cannot be DELETEd. Reset only this isolated test database fixture.
        com.ar2lda.fac.testinfra.TestDatabaseSafetyValidator.validate(testEnvironment.getActiveProfiles(),
                jdbcTemplate.queryForObject("select current_database()", String.class),
                testEnvironment.getProperty("fac.test-database.destructive-operations-enabled", Boolean.class, false));
        jdbcTemplate.execute("truncate financial_receipt_reversal,financial_receipt_line,financial_receipt,financial_document_origin,financial_allocation,financial_component,financial_position,financial_operation");
        reset(financialCore);
        for (String type : fixtureTypes) cleanupDocumentsForType(type);
        fixtureTypes.clear();
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
        // Emission evidence is permanent: retain used masters, even in fixture cleanup.
        jdbcTemplate.update("delete from tipodocumento where id = ? and not configuracao_bloqueada", tipoDocumentoId);
    }
}
