package com.ar2lda.fac;

import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.Armazem;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Familia;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.model.PPagamento;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Serie;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.ArtigoRepository;
import com.ar2lda.fac.repository.ArmazemRepository;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.EmpresaRepository;
import com.ar2lda.fac.repository.FamiliaRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.PaisRepository;
import com.ar2lda.fac.repository.PendenteRepository;
import com.ar2lda.fac.repository.PPagamentoRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.SerieRepository;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import com.ar2lda.fac.repository.TransporteRepository;
import com.ar2lda.fac.repository.UtilizadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DocumentoComercialControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentoComercialRepository documentoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private SerieRepository serieRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ArmazemRepository armazemRepository;

    @Autowired
    private CodPostalRepository codPostalRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private MoedaRepository moedaRepository;

    @Autowired
    private MPagamentoRepository mPagamentoRepository;

    @Autowired
    private RIvaRepository rIvaRepository;

    @Autowired
    private PPagamentoRepository pPagamentoRepository;

    @Autowired
    private TransporteRepository transporteRepository;

    @Autowired
    private FamiliaRepository familiaRepository;

    @Autowired
    private TipoTaxaIvaRepository tipoTaxaIvaRepository;

    @Autowired
    private ArtigoRepository artigoRepository;

    @Autowired
    private PendenteRepository pendenteRepository;

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    private Cliente cliente;
    private Armazem armazem;
    private Artigo artigo;
    private PPagamento pPagamento;
    private MPagamento mPagamento;

    @BeforeEach
    void setup() {
        documentoRepository.deleteAll();

        CodPostal codPostal = codPostalRepository.findById("3750-004")
                .orElseGet(() -> codPostalRepository.save(new CodPostal("3750-004", "Águeda")));
        Pais pais = paisRepository.findById("PT").orElseThrow();
        Moeda moeda = moedaRepository.findById("EUR")
                .orElseGet(() -> moedaRepository.save(new Moeda("EUR", "Euro", BigDecimal.ONE, BigDecimal.ONE, "EUR", 2, "978")));
        RIva riva = rIvaRepository.findById("CON")
                .orElseGet(() -> rIvaRepository.save(new RIva("CON", "Continente")));
        TipoTaxaIva taxaNormal = tipoTaxaIvaRepository.findById("NORMAL").orElseThrow();
        pPagamento = pPagamentoRepository.findById("P30").orElseGet(() -> {
            PPagamento prazo = new PPagamento();
            prazo.setId("P30");
            prazo.setNome("30 dias");
            prazo.setDias(30);
            return pPagamentoRepository.save(prazo);
        });
        Transporte transporte = transporteRepository.save(new Transporte("Transporte documento"));
        mPagamento = new MPagamento();
        mPagamento.setNome("Transferencia");
        mPagamento = mPagamentoRepository.save(mPagamento);

        Empresa empresa = empresaRepository.findById(Empresa.EMPRESA_ID).orElseGet(Empresa::new);
        empresa.setNome("Empresa FAC");
        empresa.setNif("500000000");
        empresa.setMorada("Rua Empresa");
        empresa.setMorada1(null);
        empresa.setCodPostal(codPostal);
        empresa.setLocalidade("Agueda");
        empresa.setPais(pais);
        empresa.setCapitalSocial(BigDecimal.ZERO);
        empresa.setMatriculaRegistoComercial("CRC 1");
        empresa.setCae("62010");
        empresa.setDescricaoCae("Atividades informaticas");
        empresa.setEmail("empresa@fac.test");
        empresa.setWeb("https://fac.test");
        empresaRepository.save(empresa);

        TipoDocumento tipoDocumento = new TipoDocumento("DCT", "Documento teste", null, null, null, null, 1, 1, 1, false);
        tipoDocumentoRepository.save(tipoDocumento);
        TipoDocumento tipoDocumentoFinanceiro = new TipoDocumento("RCB", "Recibo teste", null, null, null, null, 3, 1, 2, true);
        tipoDocumentoRepository.save(tipoDocumentoFinanceiro);
        serieRepository.save(new Serie(tipoDocumentoFinanceiro, "A", "Serie recibo", null, null));
        serieRepository.save(new Serie(tipoDocumento, "A", "Série A", null, null));

        cliente = new Cliente();
        cliente.setNome("Cliente Documento");
        cliente.setMorada("Rua Cliente");
        cliente.setLocalidade("Águeda");
        cliente.setCodPostal(codPostal);
        cliente.setPais(pais);
        cliente.setNif("509654321");
        cliente.setMoeda(moeda);
        cliente.setEmail("cliente.documento@fac.test");
        cliente.setRiva(riva);
        cliente.setMPagamento(mPagamento);
        cliente.setPPagamento(pPagamento);
        cliente.setTransporte(transporte);
        cliente = clienteRepository.save(cliente);

        armazem = new Armazem("Armazém Documento", "Rua Armazém", null, "Águeda");
        armazem.setCodPostal(codPostal);
        armazem.setPais(pais);
        armazem = armazemRepository.save(armazem);

        Familia familia = familiaRepository.save(new Familia("Teste"));
        artigo = new Artigo("ARTLINHA");
        artigo.setDescricao("Artigo Linha");
        artigo.setUnidade("UN");
        artigo.setFamilia(familia);
        artigo.setPeso(new BigDecimal("1.250"));
        artigo.setIvaCompra(taxaNormal);
        artigo.setIvaVenda(taxaNormal);
        artigo.setPvp(new BigDecimal("10.000000"));
        artigo = artigoRepository.save(artigo);

        utilizadorRepository.save(new Utilizador(
                "EMISSOR",
                "Utilizador Emissor",
                "emissor@fac.test",
                "$2a$10$123456789012345678901uZVXKMcIlbsK8PL5kN1l4I2zxIlW85ee",
                false
        ));
    }

    @Test
    void criaAtualizaEApagaRascunhoDocumentoComercial() throws Exception {
        String location = mockMvc.perform(post("/documentos-comerciais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoDocumentoId": "DCT",
                                  "serie": "A",
                                  "dataEmissao": "2026-06-06",
                                  "clienteId": %d,
                                  "armazemCargaId": %d,
                                  "pPagamentoId": "P30",
                                  "matricula": "AA-00-AA",
                                  "peso": 12.345,
                                  "observacoes": "Rascunho inicial"
                                }
                                """.formatted(cliente.getId(), armazem.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RASCUNHO"))
                .andExpect(jsonPath("$.numeroDocumento").doesNotExist())
                .andExpect(jsonPath("$.dataVencimento").value("2026-07-06"))
                .andExpect(jsonPath("$.clienteNome").value("Cliente Documento"))
                .andExpect(jsonPath("$.clientePais").value("PT"))
                .andExpect(jsonPath("$.cargaNome").value("Armazém Documento"))
                .andExpect(jsonPath("$.dataCarga").value("2026-06-06"))
                .andExpect(jsonPath("$.anulado").value(false))
                .andExpect(jsonPath("$.impresso").value(false))
                .andExpect(jsonPath("$.liquidado").value(false))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value("AA-00-AA"));

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dataEmissao": "2026-06-07",
                                  "armazemCargaId": %d,
                                  "pPagamentoId": "P30",
                                  "matricula": "BB-00-BB",
                                  "observacoes": "Rascunho atualizado"
                                }
                                """.formatted(armazem.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataEmissao").value("2026-06-07"))
                .andExpect(jsonPath("$.dataVencimento").value("2026-07-07"))
                .andExpect(jsonPath("$.dataCarga").value("2026-06-07"))
                .andExpect(jsonPath("$.matricula").value("BB-00-BB"));

        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());
    }

    @Test
    void criaLinhaERecalculaTotaisDoCabecalho() throws Exception {
        String documentoLocation = mockMvc.perform(post("/documentos-comerciais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoDocumentoId": "DCT",
                                  "serie": "A",
                                  "dataEmissao": "2026-06-06",
                                  "clienteId": %d,
                                  "armazemCargaId": %d,
                                  "pPagamentoId": "P30"
                                }
                                """.formatted(cliente.getId(), armazem.getId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(post(documentoLocation + "/linhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "artigoId": "ARTLINHA",
                                  "quantidade": 2,
                                  "precoUnitario": 10,
                                  "tipoDesconto": "PERCENTAGEM",
                                  "desconto": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroLinha").value(1))
                .andExpect(jsonPath("$.descricao").value("Artigo Linha"))
                .andExpect(jsonPath("$.valorBruto").value(20.000000))
                .andExpect(jsonPath("$.valorDesconto").value(2.000000))
                .andExpect(jsonPath("$.valorLinha").value(18.000000))
                .andExpect(jsonPath("$.tipoTaxaIvaId").value("NORMAL"))
                .andExpect(jsonPath("$.percentagemIva").value(23.00))
                .andExpect(jsonPath("$.peso").value(2.500));

        mockMvc.perform(get(documentoLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorBruto").value(20.000000))
                .andExpect(jsonPath("$.valorDesconto").value(2.000000))
                .andExpect(jsonPath("$.valorSujeitoNormal").value(18.000000))
                .andExpect(jsonPath("$.valorIvaNormal").value(4.140000))
                .andExpect(jsonPath("$.valorIvaTotal").value(4.140000))
                .andExpect(jsonPath("$.valorTotal").value(22.140000))
                .andExpect(jsonPath("$.peso").value(2.500));
    }

    @Test
    void documentoEmitidoGeraPendenteConsultavel() throws Exception {
        String documentoLocation = mockMvc.perform(post("/documentos-comerciais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoDocumentoId": "DCT",
                                  "serie": "A",
                                  "dataEmissao": "2026-06-06",
                                  "clienteId": %d,
                                  "armazemCargaId": %d,
                                  "pPagamentoId": "P30"
                                }
                                """.formatted(cliente.getId(), armazem.getId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(post(documentoLocation + "/linhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "artigoId": "ARTLINHA",
                                  "quantidade": 2,
                                  "precoUnitario": 10
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(documentoLocation + "/emitir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emissorId": "EMISSOR"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EMITIDO"))
                .andExpect(jsonPath("$.numeroDocumento").value(1))
                .andExpect(jsonPath("$.emissorId").value("EMISSOR"));

        DocumentoComercial documento = documentoRepository.findAll().get(0);
        Pendente pendente = pendenteRepository.findByDocumentoComercialId(documento.getId()).orElseThrow();

        mockMvc.perform(get("/pendentes/" + pendente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentoComercialId").value(documento.getId()))
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.tipoDocumentoId").value("DCT"))
                .andExpect(jsonPath("$.numeroDocumento").value(1))
                .andExpect(jsonPath("$.serieDocumento").value("A"))
                .andExpect(jsonPath("$.valorDocumento").value(24.600000))
                .andExpect(jsonPath("$.valorPendente").value(24.600000))
                .andExpect(jsonPath("$.dataDocumento").value("2026-06-06"))
                .andExpect(jsonPath("$.dataVencimento").value("2026-07-06"))
                .andExpect(jsonPath("$.moedaId").value("EUR"));
    }

    @Test
    void consultaImpressaoDocumentoComercialSemMarcarComoImpresso() throws Exception {
        String documentoLocation = mockMvc.perform(post("/documentos-comerciais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoDocumentoId": "DCT",
                                  "serie": "A",
                                  "dataEmissao": "2026-06-06",
                                  "clienteId": %d,
                                  "armazemCargaId": %d,
                                  "pPagamentoId": "P30"
                                }
                                """.formatted(cliente.getId(), armazem.getId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(post(documentoLocation + "/linhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "artigoId": "ARTLINHA",
                                  "quantidade": 2,
                                  "precoUnitario": 10
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(documentoLocation + "/emitir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emissorId": "EMISSOR"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get(documentoLocation + "/impressao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresa.nome").value("Empresa FAC"))
                .andExpect(jsonPath("$.empresa.nif").value("500000000"))
                .andExpect(jsonPath("$.documento.tipoDocumentoId").value("DCT"))
                .andExpect(jsonPath("$.documento.serie").value("A"))
                .andExpect(jsonPath("$.documento.numeroDocumento").value(1))
                .andExpect(jsonPath("$.documento.clienteNome").value("Cliente Documento"))
                .andExpect(jsonPath("$.documento.valorTotal").value(24.600000))
                .andExpect(jsonPath("$.documento.impresso").value(false))
                .andExpect(jsonPath("$.linhas[0].numeroLinha").value(1))
                .andExpect(jsonPath("$.linhas[0].artigoId").value("ARTLINHA"))
                .andExpect(jsonPath("$.linhas[0].valorLinha").value(20.000000));

        DocumentoComercial documento = documentoRepository.findAll().get(0);
        org.assertj.core.api.Assertions.assertThat(documento.isImpresso()).isFalse();
    }

    @Test
    void documentoEmitidoPodeSerAnuladoSeNaoTiverLiquidacao() throws Exception {
        String documentoLocation = mockMvc.perform(post("/documentos-comerciais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoDocumentoId": "DCT",
                                  "serie": "A",
                                  "dataEmissao": "2026-06-06",
                                  "clienteId": %d,
                                  "armazemCargaId": %d,
                                  "pPagamentoId": "P30"
                                }
                                """.formatted(cliente.getId(), armazem.getId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(post(documentoLocation + "/linhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "artigoId": "ARTLINHA",
                                  "quantidade": 2,
                                  "precoUnitario": 10
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(documentoLocation + "/emitir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emissorId": "EMISSOR"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anulado").value(false));

        DocumentoComercial documento = documentoRepository.findAll().get(0);
        org.assertj.core.api.Assertions.assertThat(pendenteRepository.findByDocumentoComercialId(documento.getId())).isPresent();

        mockMvc.perform(post(documentoLocation + "/anular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EMITIDO"))
                .andExpect(jsonPath("$.anulado").value(true))
                .andExpect(jsonPath("$.liquidado").value(false));

        org.assertj.core.api.Assertions.assertThat(pendenteRepository.findByDocumentoComercialId(documento.getId())).isEmpty();

        mockMvc.perform(post(documentoLocation + "/anular"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void documentoFinanceiroLiquidaParcialmentePendente() throws Exception {
        String documentoLocation = mockMvc.perform(post("/documentos-comerciais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoDocumentoId": "DCT",
                                  "serie": "A",
                                  "dataEmissao": "2026-06-06",
                                  "clienteId": %d,
                                  "armazemCargaId": %d,
                                  "pPagamentoId": "P30"
                                }
                                """.formatted(cliente.getId(), armazem.getId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(post(documentoLocation + "/linhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "artigoId": "ARTLINHA",
                                  "quantidade": 2,
                                  "precoUnitario": 10
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(documentoLocation + "/emitir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emissorId": "EMISSOR"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EMITIDO"))
                .andExpect(jsonPath("$.numeroDocumento").value(1));

        DocumentoComercial documento = documentoRepository.findAll().get(0);
        Pendente pendente = pendenteRepository.findByDocumentoComercialId(documento.getId()).orElseThrow();

        String financeiroLocation = mockMvc.perform(post("/documentos-financeiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoDocumentoId": "RCB",
                                  "serie": "A",
                                  "dataEmissao": "2026-06-06",
                                  "clienteId": %d,
                                  "moedaId": "EUR",
                                  "mPagamentoId": %d,
                                  "dataHoraOperacao": "2026-06-06T12:00:00+01:00",
                                  "emissorId": "EMISSOR",
                                  "linhas": [
                                    {
                                      "pendenteId": %d,
                                      "valorALiquidar": 10,
                                      "descontoValor": 1
                                    }
                                  ]
                                }
                                """.formatted(cliente.getId(), mPagamento.getId(), pendente.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoDocumentoId").value("RCB"))
                .andExpect(jsonPath("$.numeroDocumento").value(1))
                .andExpect(jsonPath("$.valorPagamentoBruto").value(10.000000))
                .andExpect(jsonPath("$.valorDescontoFinanceiro").value(1.000000))
                .andExpect(jsonPath("$.valorPagamentoLiquido").value(9.000000))
                .andExpect(jsonPath("$.mPagamentoId").value(mPagamento.getId()))
                .andExpect(jsonPath("$.emissorId").value("EMISSOR"))
                .andExpect(jsonPath("$.linhas[0].dataDocumento").value("2026-06-06"))
                .andExpect(jsonPath("$.linhas[0].dataVencimento").value("2026-07-06"))
                .andExpect(jsonPath("$.linhas[0].tipoDocumentoId").value("DCT"))
                .andExpect(jsonPath("$.linhas[0].numeroDocumento").value(1))
                .andExpect(jsonPath("$.linhas[0].serieDocumento").value("A"))
                .andExpect(jsonPath("$.linhas[0].valorDocumento").value(24.600000))
                .andExpect(jsonPath("$.linhas[0].valorPendenteAntes").value(24.600000))
                .andExpect(jsonPath("$.linhas[0].valorALiquidar").value(10.000000))
                .andExpect(jsonPath("$.linhas[0].descontoValor").value(1.000000))
                .andExpect(jsonPath("$.linhas[0].valorPagamentoLiquido").value(9.000000))
                .andExpect(jsonPath("$.linhas[0].novoValorPendente").value(14.600000))
                .andExpect(jsonPath("$.linhas[0].moedaId").value("EUR"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(financeiroLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDocumento").value(1));

        mockMvc.perform(get(financeiroLocation + "/impressao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresa.nome").value("Empresa FAC"))
                .andExpect(jsonPath("$.empresa.nif").value("500000000"))
                .andExpect(jsonPath("$.documento.tipoDocumentoId").value("RCB"))
                .andExpect(jsonPath("$.documento.numeroDocumento").value(1))
                .andExpect(jsonPath("$.documento.valorPagamentoBruto").value(10.000000))
                .andExpect(jsonPath("$.documento.valorPagamentoLiquido").value(9.000000))
                .andExpect(jsonPath("$.documento.impresso").value(false))
                .andExpect(jsonPath("$.documento.linhas[0].tipoDocumentoId").value("DCT"))
                .andExpect(jsonPath("$.documento.linhas[0].numeroDocumento").value(1))
                .andExpect(jsonPath("$.documento.linhas[0].valorALiquidar").value(10.000000));

        Pendente pendenteAtualizado = pendenteRepository.findById(pendente.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(pendenteAtualizado.getValorPendente()).isEqualByComparingTo("14.600000");
        org.assertj.core.api.Assertions.assertThat(documentoRepository.findById(documento.getId()).orElseThrow().isLiquidado()).isTrue();

        mockMvc.perform(post(documentoLocation + "/anular"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(financeiroLocation + "/anular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anulado").value(true));

        Pendente pendenteReposto = pendenteRepository.findById(pendente.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(pendenteReposto.getValorPendente()).isEqualByComparingTo("24.600000");
        org.assertj.core.api.Assertions.assertThat(documentoRepository.findById(documento.getId()).orElseThrow().isLiquidado()).isFalse();

        mockMvc.perform(post(financeiroLocation + "/anular"))
                .andExpect(status().isBadRequest());
    }
}
