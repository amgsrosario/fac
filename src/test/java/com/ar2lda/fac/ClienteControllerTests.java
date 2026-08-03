package com.ar2lda.fac;

import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.TransporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CodPostalRepository codPostalRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MoedaRepository moedaRepository;

    @Autowired
    private RIvaRepository rIvaRepository;

    @Autowired
    private TransporteRepository transporteRepository;

    private String transporteId;

    @BeforeEach
    void setup() {
        codPostalRepository.findById("3750-003")
                .orElseGet(() -> codPostalRepository.save(new CodPostal("3750-003", "Águeda")));
        moedaRepository.findById("EUR")
                .orElseGet(() -> moedaRepository.save(new Moeda("EUR", "Euro", BigDecimal.ONE, BigDecimal.ONE, "EUR", 2, "978")));
        rIvaRepository.findById("CON")
                .orElseGet(() -> rIvaRepository.save(new RIva("CON", "Continente")));
        transporteId = transporteRepository.save(new Transporte("CLI", "Transporte teste")).getId();
    }

    @Test
    void criaClienteComRegimeIvaConPorDefeito() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Cliente Teste",
                                  "morada": "Rua Principal",
                                  "localidade": "Águeda",
                                  "nif": "509123456",
                                  "email": "cliente@fac.test",
                                  "retencao": false,
                                  "inativo": false,
                                  "codPostalId": "3750-003",
                                  "paisId": "PT",
                                  "moedaId": "EUR",
                                  "transporteId": "%s"
                                }
                                """.formatted(transporteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rivaId").value("CON"))
                .andExpect(jsonPath("$.transporteId").value(transporteId))
                .andExpect(jsonPath("$.mPagamentoId").doesNotExist())
                .andExpect(jsonPath("$.pPagamentoId").doesNotExist());
    }

    @Test
    void rejeitaClienteSemTransporte() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Cliente Sem Transporte",
                                  "morada": "Rua Principal",
                                  "localidade": "Águeda",
                                  "nif": "509123457",
                                  "email": "cliente2@fac.test",
                                  "retencao": false,
                                  "inativo": false,
                                  "codPostalId": "3750-003",
                                  "paisId": "PT",
                                  "moedaId": "EUR"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pesquisaClienteForaDosPrimeirosCemResultados() throws Exception {
        for (int index = 0; index < 105; index++) {
            criarCliente("Cliente Pesquisa %03d".formatted(index), "8%08d".formatted(index),
                    "cliente%03d@pesquisa.test".formatted(index), false);
        }
        criarCliente("ZZZ Cliente Remoto", "599999999", "remoto@pesquisa.test", true);

        mockMvc.perform(get("/clientes")
                        .param("page", "0")
                        .param("size", "100")
                        .param("sort", "nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(100)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(106)))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(2)));

        long clienteId = clienteRepository.findAll().stream()
                .filter(cliente -> "599999999".equals(cliente.getNif()))
                .findFirst()
                .orElseThrow()
                .getId();

        assertPesquisaEncontra(String.valueOf(clienteId));
        assertPesquisaEncontra("ZZZ Cliente Remoto");
        assertPesquisaEncontra("  ZZZ Cliente Remoto  ");
        assertPesquisaEncontra("239999999");
        assertPesquisaEncontra("919999999");
        assertPesquisaEncontra("Localidade Remota");

        mockMvc.perform(get("/clientes").param("search", "remoto@pesquisa.test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome").value("ZZZ Cliente Remoto"));

        mockMvc.perform(get("/clientes").param("search", "599999999").param("inativo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].inativo").value(true));

        mockMvc.perform(get("/clientes").param("search", "cliente-inexistente-xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    private void assertPesquisaEncontra(String search) throws Exception {
        mockMvc.perform(get("/clientes")
                        .param("search", search)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("ZZZ Cliente Remoto"));
    }

    private void criarCliente(String nome, String nif, String email, boolean inativo) throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "morada": "Rua da Pesquisa",
                                  "localidade": "%s",
                                  "nif": "%s",
                                  "email": "%s",
                                  "tel": "%s",
                                  "tm": "%s",
                                  "retencao": false,
                                  "inativo": %s,
                                  "codPostalId": "3750-003",
                                  "paisId": "PT",
                                  "moedaId": "EUR",
                                  "transporteId": "%s"
                                }
                                """.formatted(nome,
                                "599999999".equals(nif) ? "Localidade Remota" : "Águeda",
                                nif, email,
                                "599999999".equals(nif) ? "239999999" : "210000000",
                                "599999999".equals(nif) ? "919999999" : "910000000",
                                inativo, transporteId)))
                .andExpect(status().isCreated());
    }
}
