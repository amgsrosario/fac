package com.ar2lda.fac;

import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Familia;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.TipoArtigo;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.repository.ArtigoRepository;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.FamiliaRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.TransporteRepository;
import com.ar2lda.fac.repository.PaisRepository;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DadosMestresImportExportTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ArtigoRepository artigoRepository;
    @Autowired
    private CodPostalRepository codPostalRepository;
    @Autowired
    private MoedaRepository moedaRepository;
    @Autowired
    private RIvaRepository rIvaRepository;
    @Autowired
    private TransporteRepository transporteRepository;
    @Autowired
    private FamiliaRepository familiaRepository;
    @Autowired private PaisRepository paisRepository;
    @Autowired private TipoTaxaIvaRepository tipoTaxaIvaRepository;

    private String transporteId;
    private Long familiaId;

    @BeforeEach
    void setup() {
        codPostalRepository.findById("3750-029")
                .orElseGet(() -> codPostalRepository.save(new CodPostal("3750-029", "Agueda")));
        moedaRepository.findById("EUR")
                .orElseGet(() -> moedaRepository.save(new Moeda("EUR", "Euro", BigDecimal.ONE, BigDecimal.ONE, "EUR", 2, "978")));
        rIvaRepository.findById("CON")
                .orElseGet(() -> rIvaRepository.save(new RIva("CON", "Continente")));
        transporteId = transporteRepository.save(new Transporte("IMP", "Transporte importacao")).getId();
        familiaId = familiaRepository.save(new Familia("Familia importacao")).getId();
        paisRepository.findById("PT").orElseGet(() -> paisRepository.save(new Pais("PT", "Portugal")));
        tipoTaxaIvaRepository.findById("NORMAL").orElseGet(() -> tipoTaxaIvaRepository.save(new TipoTaxaIva("NORMAL", "Normal", false)));
        tipoTaxaIvaRepository.findById("REDUZIDA").orElseGet(() -> tipoTaxaIvaRepository.save(new TipoTaxaIva("REDUZIDA", "Reduzida", false)));
    }

    @Test
    void exportaClientesEArtigosEmMaisDeUmaPagina() throws Exception {
        Pais pais = paisRepository.getReferenceById("PT"); Moeda moeda = moedaRepository.getReferenceById("EUR"); RIva riva = rIvaRepository.getReferenceById("CON"); Transporte transporte = transporteRepository.getReferenceById(transporteId);
        var clientes = new ArrayList<Cliente>(); var artigos = new ArrayList<Artigo>();
        for (int i = 0; i < 501; i++) {
            Cliente c = new Cliente(); c.setNome("Cliente " + i); c.setMorada("Rua"); c.setNif(String.format("5%08d", i)); c.setCodPostal(codPostalRepository.getReferenceById("3750-029")); c.setPais(pais); c.setMoeda(moeda); c.setRiva(riva); c.setTransporte(transporte); clientes.add(c);
            Artigo a = new Artigo(String.format("A%03d", i)); a.setDescricao("Artigo " + i); a.setTipoArtigo(TipoArtigo.ARTIGO); a.setUnidade("UN"); a.setFamilia(familiaRepository.getReferenceById(familiaId)); a.setIvaCompra(tipoTaxaIvaRepository.getReferenceById("REDUZIDA")); a.setIvaVenda(tipoTaxaIvaRepository.getReferenceById("NORMAL")); a.setPvp(BigDecimal.ONE); artigos.add(a);
        }
        clienteRepository.saveAll(clientes); artigoRepository.saveAll(artigos);
        clienteRepository.flush(); artigoRepository.flush();
        var clientesCsv = mockMvc.perform(get("/exportacoes/clientes?formato=csv")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(clientesCsv.lines().count()).isGreaterThanOrEqualTo(502);
        var artigosCsv = mockMvc.perform(get("/exportacoes/artigos?formato=csv")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(artigosCsv.lines().count()).isGreaterThanOrEqualTo(502);
        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(mockMvc.perform(get("/exportacoes/clientes?formato=xlsx")).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray()))) {
            assertThat(workbook.getSheet("dados").getLastRowNum()).isGreaterThanOrEqualTo(501);
        }
        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(mockMvc.perform(get("/exportacoes/artigos?formato=xlsx")).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray()))) {
            assertThat(workbook.getSheet("dados").getLastRowNum()).isGreaterThanOrEqualTo(501);
        }
    }

    @Test
    void validaEConfirmaImportacaoCsvDeClientesSemGravarNaPreValidacao() throws Exception {
        long before = clienteRepository.count();

        String response = mockMvc.perform(multipart("/importacoes/clientes/validar")
                        .file(csv("clientes.csv", clientesCsv("509999999"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.totalLinhas").value(1))
                .andExpect(jsonPath("$.resumo.linhasValidas").value(1))
                .andExpect(jsonPath("$.erros").isEmpty())
                .andReturn().getResponse().getContentAsString();

        assertThat(clienteRepository.count()).isEqualTo(before);
        String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(response).get("id").asText();

        mockMvc.perform(post("/importacoes/clientes/{id}/confirmar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criados").value(1))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        assertThat(clienteRepository.existsByNif("509999999")).isTrue();

        mockMvc.perform(post("/importacoes/clientes/{id}/confirmar", id))
                .andExpect(status().isConflict());
    }

    @Test
    void validaDuplicadosDeClientesECancelaSessao() throws Exception {
        mockMvc.perform(multipart("/importacoes/clientes/validar")
                        .file(csv("clientes.csv", clientesCsv("599000001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.linhasComErro").value(1))
                .andExpect(jsonPath("$.erros[0].codigo").value("CLIENTE_NIF_INVALIDO"));

        String duplicate = clientesCsv("509999999") + clienteLine("Cliente Dois", "509999999");
        String response = mockMvc.perform(multipart("/importacoes/clientes/validar")
                        .file(csv("clientes.csv", duplicate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.duplicados").value(1))
                .andReturn().getResponse().getContentAsString();
        String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(response).get("id").asText();

        mockMvc.perform(delete("/importacoes/clientes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/importacoes/clientes/{id}/confirmar", id))
                .andExpect(status().isConflict());
    }

    @Test
    void validaEConfirmaImportacaoCsvDeArtigos() throws Exception {
        String response = mockMvc.perform(multipart("/importacoes/artigos/validar")
                        .file(csv("artigos.csv", artigosCsv("IMP001", "5600000000001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.linhasValidas").value(1))
                .andReturn().getResponse().getContentAsString();
        String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(response).get("id").asText();

        mockMvc.perform(post("/importacoes/artigos/{id}/confirmar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criados").value(1));

        assertThat(artigoRepository.existsById("IMP001")).isTrue();
        assertThat(artigoRepository.findById("IMP001").orElseThrow().getTipoArtigo().name()).isEqualTo("SERVICO");
    }

    @Test
    void suportaXlsxERejeitaFormula() throws Exception {
        mockMvc.perform(multipart("/importacoes/artigos/validar")
                        .file(new MockMultipartFile("file", "artigos.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                artigosXlsx(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.linhasValidas").value(1));

        mockMvc.perform(multipart("/importacoes/artigos/validar")
                        .file(new MockMultipartFile("file", "artigos.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                artigosXlsx(true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportaModelosEExportacoesComProtecaoCsv() throws Exception {
        mockMvc.perform(get("/importacoes/clientes/modelo?formato=csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("modelo-importacao-clientes.csv")))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("\"nome\";"));

        mockMvc.perform(get("/exportacoes/artigos?formato=xlsx"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".xlsx")));
    }

    @Test
    void rejeitaExtensaoPerigosaECabecalhoInvalido() throws Exception {
        mockMvc.perform(multipart("/importacoes/clientes/validar")
                        .file(new MockMultipartFile("file", "clientes.exe", MediaType.APPLICATION_OCTET_STREAM_VALUE, "x".getBytes())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(multipart("/importacoes/clientes/validar")
                        .file(csv("clientes.csv", "nome;nif\nCliente;509999999\n")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Coluna obrigatória ausente")));
    }

    private MockMultipartFile csv(String filename, String content) {
        return new MockMultipartFile("file", filename, "text/csv", ("\uFEFF" + content).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String clientesCsv(String nif) {
        return "nome;morada;morada1;localidade;nif;tel;tm;email;email1;tspiva;iban;retencao;inativo;observacoes;codPostalId;paisId;moedaId;mPagamentoId;pPagamentoId;rivaId;transporteId\n"
                + clienteLine("Cliente Acao", nif);
    }

    private String clienteLine(String nome, String nif) {
        return "%s;Rua A;;Agueda;%s;;;cliente.%s@fac.test;;;;false;false;Observacao;3750-029;PT;EUR;;;CON;%s\n"
                .formatted(nome, nif, nif, transporteId);
    }

    private String artigosCsv(String codigo, String identificacao) {
        return "codigo;abreviatura;codigoIdentificacao;descricao;tipoArtigo;unidade;familiaId;peso;ivaCompraId;ivaVendaId;pvp;inativo;retencao;observacoes\n"
                + "%s;Imp;%s;Artigo importado;SERVICO;UN;%d;1.000;REDUZIDA;NORMAL;12.500000;false;false;Obs\n"
                .formatted(codigo, identificacao, familiaId);
    }

    private byte[] artigosXlsx(boolean formula) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("dados");
            Row h = sheet.createRow(0);
            String[] headers = {"codigo", "abreviatura", "codigoIdentificacao", "descricao", "tipoArtigo", "unidade", "familiaId", "peso", "ivaCompraId", "ivaVendaId", "pvp", "inativo", "retencao", "observacoes"};
            for (int i = 0; i < headers.length; i++) h.createCell(i).setCellValue(headers[i]);
            Row r = sheet.createRow(1);
            String[] values = {"IMPXLSX", "Imp", "5600000000002", "Artigo XLSX", "ARTIGO", "UN", String.valueOf(familiaId), "1.000", "REDUZIDA", "NORMAL", "9.990000", "false", "false", "Obs"};
            for (int i = 0; i < values.length; i++) r.createCell(i).setCellValue(values[i]);
            if (formula) r.getCell(3).setCellFormula("1+1");
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
