package com.ar2lda.fac;

import com.ar2lda.fac.repository.ParametrosAplicacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParametrosAplicacaoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParametrosAplicacaoRepository parametrosAplicacaoRepository;

    @BeforeEach
    void setup() {
        parametrosAplicacaoRepository.deleteAll();
    }

    @Test
    void criaConsultaEAtualizaParametros() throws Exception {
        mockMvc.perform(post("/parametros-aplicacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atrasoCargaMinutos": 10,
                                  "decimaisQuantidade": 3,
                                  "decimaisValor": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.atrasoCargaMinutos").value(10))
                .andExpect(jsonPath("$.decimaisQuantidade").value(3))
                .andExpect(jsonPath("$.decimaisValor").value(2));

        mockMvc.perform(get("/parametros-aplicacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atrasoCargaMinutos").value(10));

        mockMvc.perform(put("/parametros-aplicacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atrasoCargaMinutos": 5,
                                  "decimaisQuantidade": 6,
                                  "decimaisValor": 6
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/parametros-aplicacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atrasoCargaMinutos").value(5))
                .andExpect(jsonPath("$.decimaisQuantidade").value(6))
                .andExpect(jsonPath("$.decimaisValor").value(6));
    }

    @Test
    void rejeitaSegundoRegistoDeParametros() throws Exception {
        String parametrosJson = """
                {
                  "atrasoCargaMinutos": 10,
                  "decimaisQuantidade": 3,
                  "decimaisValor": 2
                }
                """;

        mockMvc.perform(post("/parametros-aplicacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parametrosJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/parametros-aplicacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parametrosJson))
                .andExpect(status().isConflict());
    }

    @Test
    void rejeitaValoresForaDosLimites() throws Exception {
        mockMvc.perform(post("/parametros-aplicacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atrasoCargaMinutos": 121,
                                  "decimaisQuantidade": 7,
                                  "decimaisValor": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
