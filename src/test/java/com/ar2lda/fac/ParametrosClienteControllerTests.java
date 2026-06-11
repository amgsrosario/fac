package com.ar2lda.fac;

import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.PPagamento;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.PPagamentoRepository;
import com.ar2lda.fac.repository.PaisRepository;
import com.ar2lda.fac.repository.ParametrosClienteRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParametrosClienteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParametrosClienteRepository parametrosClienteRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private MoedaRepository moedaRepository;

    @Autowired
    private RIvaRepository rIvaRepository;

    @Autowired
    private MPagamentoRepository mPagamentoRepository;

    @Autowired
    private PPagamentoRepository pPagamentoRepository;

    @Autowired
    private TransporteRepository transporteRepository;

    private Integer mPagamentoId;
    private Integer transporteId;

    @BeforeEach
    void setup() {
        parametrosClienteRepository.deleteAll();
        paisRepository.findById("TST").orElseGet(() -> paisRepository.save(new Pais("TST", "País teste")));
        moedaRepository.findById("TST").orElseGet(() -> moedaRepository.save(
                new Moeda("TST", "Moeda teste", BigDecimal.ONE, BigDecimal.ONE, "T", 2, "TST")));
        rIvaRepository.findById("TST").orElseGet(() -> rIvaRepository.save(new RIva("TST", "Regime teste")));

        PPagamento prazo = pPagamentoRepository.findById("TST").orElseGet(() -> {
            PPagamento entity = new PPagamento();
            entity.setId("TST");
            entity.setNome("Prazo teste");
            entity.setDias(15);
            return pPagamentoRepository.save(entity);
        });

        MPagamento modo = new MPagamento();
        modo.setNome("Modo teste");
        mPagamentoId = mPagamentoRepository.save(modo).getId();
        transporteId = transporteRepository.save(new Transporte("Transporte teste")).getId();
        prazo.getId();
    }

    @Test
    void criaConsultaEAtualizaMatrizDeCliente() throws Exception {
        mockMvc.perform(post("/parametros-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCompleto(true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paisId").value("TST"))
                .andExpect(jsonPath("$.moedaId").value("TST"))
                .andExpect(jsonPath("$.rivaId").value("TST"))
                .andExpect(jsonPath("$.mPagamentoId").value(mPagamentoId))
                .andExpect(jsonPath("$.pPagamentoId").value("TST"))
                .andExpect(jsonPath("$.transporteId").value(transporteId))
                .andExpect(jsonPath("$.retencao").value(true));

        mockMvc.perform(get("/parametros-cliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paisId").value("TST"));

        mockMvc.perform(put("/parametros-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paisId": "TST",
                                  "moedaId": "TST",
                                  "rivaId": "TST",
                                  "mPagamentoId": null,
                                  "pPagamentoId": null,
                                  "transporteId": null,
                                  "retencao": false
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/parametros-cliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mPagamentoId").doesNotExist())
                .andExpect(jsonPath("$.pPagamentoId").doesNotExist())
                .andExpect(jsonPath("$.transporteId").doesNotExist())
                .andExpect(jsonPath("$.retencao").value(false));
    }

    @Test
    void permiteMatrizSemValoresPreDefinidos() throws Exception {
        mockMvc.perform(post("/parametros-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paisId").doesNotExist())
                .andExpect(jsonPath("$.retencao").doesNotExist());
    }

    @Test
    void rejeitaSegundoRegistoEReferenciaInexistente() throws Exception {
        mockMvc.perform(post("/parametros-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCompleto(true)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/parametros-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCompleto(false)))
                .andExpect(status().isConflict());

        parametrosClienteRepository.deleteAll();

        mockMvc.perform(post("/parametros-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paisId\":\"XXX\"}"))
                .andExpect(status().isNotFound());
    }

    private String jsonCompleto(boolean retencao) {
        return """
                {
                  "paisId": "TST",
                  "moedaId": "TST",
                  "rivaId": "TST",
                  "mPagamentoId": %d,
                  "pPagamentoId": "TST",
                  "transporteId": %d,
                  "retencao": %s
                }
                """.formatted(mPagamentoId, transporteId, retencao);
    }
}
