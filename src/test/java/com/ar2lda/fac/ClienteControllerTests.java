package com.ar2lda.fac;

import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.CodPostalRepository;
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
    private ClienteRepository clienteRepository;

    @Autowired
    private CodPostalRepository codPostalRepository;

    @Autowired
    private MoedaRepository moedaRepository;

    @Autowired
    private RIvaRepository rIvaRepository;

    @Autowired
    private TransporteRepository transporteRepository;

    private Integer transporteId;

    @BeforeEach
    void setup() {
        clienteRepository.deleteAll();
        codPostalRepository.findById("3750-003")
                .orElseGet(() -> codPostalRepository.save(new CodPostal("3750-003", "Águeda")));
        moedaRepository.findById("EUR")
                .orElseGet(() -> moedaRepository.save(new Moeda("EUR", "Euro", BigDecimal.ONE, BigDecimal.ONE, "EUR", 2, "978")));
        rIvaRepository.findById("CON")
                .orElseGet(() -> rIvaRepository.save(new RIva("CON", "Continente")));
        transporteId = transporteRepository.save(new Transporte("Transporte teste")).getId();
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
                                  "transporteId": %d
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
}
