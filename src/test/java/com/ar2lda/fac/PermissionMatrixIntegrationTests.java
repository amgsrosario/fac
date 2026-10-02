package com.ar2lda.fac;

import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.Armazem;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Familia;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.DocumentoFinanceiro;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.model.PPagamento;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Serie;
import com.ar2lda.fac.model.TipoArtigo;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.ArtigoRepository;
import com.ar2lda.fac.repository.ArmazemRepository;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.EmpresaRepository;
import com.ar2lda.fac.repository.FamiliaRepository;
import com.ar2lda.fac.repository.LinhaDocumentoComercialRepository;
import com.ar2lda.fac.repository.LinhaDocumentoFinanceiroRepository;
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
import com.ar2lda.fac.repository.AuditoriaEventoRepository;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = "fac.security.enabled=true")
@Transactional
class PermissionMatrixIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentoComercialRepository documentoRepository;

    @Autowired
    private LinhaDocumentoComercialRepository linhaDocumentoComercialRepository;

    @Autowired
    private DocumentoFinanceiroRepository documentoFinanceiroRepository;

    @Autowired
    private LinhaDocumentoFinanceiroRepository linhaDocumentoFinanceiroRepository;

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

    @Autowired
    private AuditoriaEventoRepository auditoriaEventoRepository;

    private Cliente cliente;
    private Armazem armazem;
    private Artigo artigo;
    private PPagamento pPagamento;
    private MPagamento mPagamento;

    @BeforeEach
    void setup() {
        linhaDocumentoFinanceiroRepository.deleteAll();
        documentoFinanceiroRepository.deleteAll();
        pendenteRepository.deleteAll();
        linhaDocumentoComercialRepository.deleteAll();
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
        Transporte transporte = transporteRepository.save(new Transporte("DCT", "Transporte documento"));
        mPagamento = new MPagamento();
        mPagamento.setId("MDC");
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
        tipoDocumento.setCodigoFiscal("FT");
        tipoDocumentoRepository.save(tipoDocumento);
        TipoDocumento tipoDocumentoFinanceiro = new TipoDocumento("RCB", "Recibo teste", null, null, null, null, 3, 1, 2, true);
        tipoDocumentoFinanceiro.setCodigoFiscal("RC");
        tipoDocumentoRepository.save(tipoDocumentoFinanceiro);
        serieRepository.save(new Serie(tipoDocumentoFinanceiro, "A", "Serie recibo", "RCB2026", java.time.LocalDate.of(2026, 1, 1)));
        serieRepository.save(new Serie(tipoDocumento, "A", "Série A", "DCT2026", java.time.LocalDate.of(2026, 1, 1)));

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

        armazem = new Armazem("T01", "Armazém Documento", "Rua Armazém", null, "Águeda");
        armazem.setCodPostal(codPostal);
        armazem.setPais(pais);
        armazem = armazemRepository.save(armazem);

        Familia familia = familiaRepository.save(new Familia("Teste"));
        artigo = new Artigo("ARTLINHA");
        artigo.setDescricao("Artigo Linha");
        artigo.setTipoArtigo(TipoArtigo.ARTIGO);
        artigo.setUnidade("UN");
        artigo.setFamilia(familia);
        artigo.setPeso(new BigDecimal("1.250"));
        artigo.setIvaCompra(taxaNormal);
        artigo.setIvaVenda(taxaNormal);
        artigo.setPvp(new BigDecimal("10.000000"));
        artigo = artigoRepository.save(artigo);


    }


    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String login(com.ar2lda.fac.model.PapelUtilizador perfil) throws Exception {
        String codigo = "AUTH_" + perfil.name();
        if (!utilizadorRepository.existsById(codigo)) {
            Utilizador user = new Utilizador(codigo, codigo, codigo + "@fac.test",
                    passwordEncoder.encode("FacTest1!"), false);
            user.setPapel(perfil);
            utilizadorRepository.save(user);
        }
        String body = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + codigo + "\",\"password\":\"FacTest1!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("token").asText();
    }

    private String draft(String token) throws Exception {
        return mockMvc.perform(post("/documentos-comerciais").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"documento":{"tipoDocumentoId":"DCT","serie":"A","dataEmissao":"%s",
                "clienteId":%d,"armazemCargaId":"T01","pPagamentoId":"P30"},
                "linha":{"artigoId":"ARTLINHA","quantidade":1,"precoUnitario":10}}
                """.formatted(LocalDate.now(), cliente.getId())))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(com.ar2lda.fac.model.PapelUtilizador.class)
    void documentosETesourariaComLoginReal(com.ar2lda.fac.model.PapelUtilizador perfil) throws Exception {
        String admin = login(com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR);
        String token = login(perfil);
        String doc = draft(perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ? admin : token);
        String update = "{\"dataEmissao\":\"" + LocalDate.now() + "\",\"armazemCargaId\":\"T01\"}";
        if (perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA) {
            mockMvc.perform(post("/documentos-comerciais").header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content("""
                    {"documento":{"tipoDocumentoId":"DCT","serie":"A","dataEmissao":"%s",
                    "clienteId":%d,"armazemCargaId":"T01"},"linha":{"artigoId":"ARTLINHA","quantidade":1}}
                    """.formatted(LocalDate.now(), cliente.getId()))).andExpect(status().isForbidden());
            mockMvc.perform(put(doc).header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isForbidden());
            mockMvc.perform(delete(doc).header("Authorization","Bearer " + token)).andExpect(status().isForbidden());
            mockMvc.perform(post(doc + "/emitir").header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"emissorId\":\"AUTH_CONSULTA\"}"))
                    .andExpect(status().isForbidden());
            mockMvc.perform(post(doc + "/linhas").header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"tipoLinha\":\"TEXTO\",\"descricao\":\"Nota\"}"))
                    .andExpect(status().isForbidden());
        } else {
            mockMvc.perform(put(doc).header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isNoContent());
            mockMvc.perform(post(doc + "/linhas").header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"tipoLinha\":\"TEXTO\",\"descricao\":\"Nota\"}"))
                    .andExpect(status().isCreated());
            String disposable = draft(token);
            mockMvc.perform(delete(disposable).header("Authorization","Bearer " + token))
                    .andExpect(status().isNoContent());
        }
        mockMvc.perform(get(doc).header("Authorization","Bearer " + token)).andExpect(status().isOk());
        mockMvc.perform(post(doc + "/emitir").header("Authorization","Bearer " +
                        (perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ? admin : token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"emissorId\":\"AUTH_ADMINISTRADOR\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get(doc + "/pdf").header("Authorization","Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF))
                ;
        Long id = Long.valueOf(doc.substring(doc.lastIndexOf('/') + 1));
        Pendente pendente = pendenteRepository.findByDocumentoComercialId(id).orElseThrow();
        String receiptBody = """
                {"tipoDocumentoId":"RCB","serie":"A","dataEmissao":"%s","clienteId":%d,"moedaId":"EUR",
                "mPagamentoId":"MDC","emissorId":"AUTH_ADMINISTRADOR",
                "linhas":[{"pendenteId":%d,"valorALiquidar":%s,"descontoValor":0}]}
                """.formatted(LocalDate.now(), cliente.getId(), pendente.getId(), pendente.getValorPendente());
        if (perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA) {
            mockMvc.perform(post("/documentos-financeiros").header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content(receiptBody)).andExpect(status().isForbidden());
        }
        String receipt = mockMvc.perform(post("/documentos-financeiros").header("Authorization","Bearer " +
                        (perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ? admin : token))
                .contentType(MediaType.APPLICATION_JSON).content(receiptBody))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
        mockMvc.perform(get(receipt + "/pdf").header("Authorization","Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF));
        mockMvc.perform(post(receipt + "/anular").header("Authorization","Bearer " + token))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR ?
                        status().isOk() : status().isForbidden());
        if (perfil != com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR) {
            mockMvc.perform(post(receipt + "/anular").header("Authorization","Bearer " + admin))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post(doc + "/anular").header("Authorization","Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Teste de permissoes\"}"))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR ?
                        status().isOk() : status().isForbidden());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(com.ar2lda.fac.model.PapelUtilizador.class)
    void consultasAutenticadasMantemAcessoParaTresPerfis(com.ar2lda.fac.model.PapelUtilizador perfil) throws Exception {
        String token = login(perfil);
        for (String url : List.of("/clientes", "/artigos", "/series", "/documentos-comerciais",
                "/documentos-financeiros", "/pendentes", "/listagens/pendentes",
                "/listagens/documentos-comerciais?dataInicial=2026-01-01&dataFinal=2026-12-31",
                "/dashboard/comercial?dataInicio=2026-01-01&dataFim=2026-12-31",
                "/extratos/clientes?dataInicial=2026-01-01&dataFinal=2026-12-31",
                "/exportacoes/clientes", "/exportacoes/artigos")) {
            mockMvc.perform(get(url).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
            mockMvc.perform(get(url)).andExpect(status().isUnauthorized());
        }
        for (String url : List.of("/utilizadores", "/empresa", "/auditoria")) {
            mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                    .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR ?
                            status().isOk() : status().isForbidden());
        }
        mockMvc.perform(post("/series").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"serie":"AUTH","tipoDocumentoId":"DCT","nome":"Autorizacao",
                "codigoAt":"AUTHTEST","dataCodigoAt":"2026-01-01"}
                """)).andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR ?
                        status().isCreated() : status().isForbidden());
        String clientBody = """
                {"nome":"Permissoes","morada":"Rua Teste","nif":"123456789","email":"perm@fac.test",
                "codPostalId":"3750-004","paisId":"PT","moedaId":"EUR","transporteId":"DCT"}
                """;
        String createdClient = mockMvc.perform(post("/clientes").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(clientBody))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ?
                        status().isForbidden() : status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        String clientUrl = createdClient == null ? "/clientes/" + cliente.getId() : createdClient;
        mockMvc.perform(put(clientUrl).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(clientBody))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ?
                        status().isForbidden() : status().isNoContent());
        mockMvc.perform(delete(clientUrl).header("Authorization", "Bearer " + token))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ?
                        status().isForbidden() : status().isNoContent());
        String articleBody = """
                {"codigo":"AUTHART","descricao":"Permissoes","tipoArtigo":"ARTIGO","unidade":"UN",
                "familiaId":%d,"ivaCompraId":"NORMAL","ivaVendaId":"NORMAL","pvp":10}
                """.formatted(artigo.getFamilia().getId());
        mockMvc.perform(post("/artigos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(articleBody))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ?
                        status().isForbidden() : status().isCreated());
        mockMvc.perform(put("/artigos/AUTHART").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(articleBody.replace("\"codigo\":\"AUTHART\",", "")))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ?
                        status().isForbidden() : status().isNoContent());
        mockMvc.perform(delete("/artigos/AUTHART").header("Authorization", "Bearer " + token))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.CONSULTA ?
                        status().isForbidden() : status().isNoContent());
        mockMvc.perform(put("/series/DCT/AUTH").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Alterada","codigoAt":"AUTHTEST","dataCodigoAt":"2026-01-01"}
                """)).andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR ?
                        status().isNoContent() : status().isForbidden());
        mockMvc.perform(delete("/series/DCT/AUTH").header("Authorization", "Bearer " + token))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR ?
                        status().isNoContent() : status().isForbidden());
        var file = new org.springframework.mock.web.MockMultipartFile("file", "clientes.csv", "text/csv",
                ("nome;morada;morada1;localidade;nif;tel;tm;email;email1;tspiva;iban;retencao;inativo;observacoes;codPostalId;paisId;moedaId;mPagamentoId;pPagamentoId;rivaId;transporteId\n"
                + "Importado;Rua;;Agueda;234567890;;;import@fac.test;;;;false;false;;3750-004;PT;EUR;;;CON;DCT\n")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .multipart("/importacoes/clientes/validar").file(file)
                .header("Authorization", "Bearer " + token))
                .andExpect(perfil == com.ar2lda.fac.model.PapelUtilizador.ADMINISTRADOR ?
                        status().isOk() : status().isForbidden());
    }
}
