package com.ar2lda.fac;

import com.ar2lda.fac.model.Freguesia;
import com.ar2lda.fac.repository.FreguesiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FreguesiaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FreguesiaRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                new Freguesia("980101", "98", "01", "01", "CONCELHO ALFA TESTE", "FREGUESIA ALVOR TESTE", false),
                new Freguesia("980102", "98", "01", "02", "CONCELHO ALFA TESTE", "FREGUESIA POENTE TESTE", false),
                new Freguesia("980201", "98", "02", "01", "CONCELHO BETA TESTE", "FREGUESIA NORTE TESTE", true),
                new Freguesia("980301", "98", "03", "01", "CONCELHO DIACRÍTICO TESTE", "FREGUESIA DIACRÍTICA TESTE", false)));
    }

    @Test
    void listaSemPesquisaMantemContratoPaginado() throws Exception {
        mockMvc.perform(get("/freguesias?page=0&size=2&sort=codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    void pesquisaPorCodigoNomeEConcelhoSemDistinguirMaiusculas() throws Exception {
        mockMvc.perform(get("/freguesias?search=9801&page=0&size=25&sort=codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/freguesias?search=ALVOR&page=0&size=25&sort=codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].codigo").value("980101"));

        mockMvc.perform(get("/freguesias?search=concelho beta teste&page=0&size=25&sort=codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/freguesias?search=diacritica&page=0&size=25&sort=codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].codigo").value("980301"));
    }

    @Test
    void paginaOrdenaEDevolvePesquisaSemResultados() throws Exception {
        mockMvc.perform(get("/freguesias?search=CONCELHO ALFA TESTE&page=1&size=1&sort=nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].nome").value("FREGUESIA POENTE TESTE"));

        mockMvc.perform(get("/freguesias?search=sem-resultado-freguesia&page=0&size=25&sort=codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void crudFreguesia() throws Exception {
        mockMvc.perform(post("/freguesias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "990101",
                                  "codigoDistrito": "99",
                                  "codigoConcelho": "01",
                                  "codigoFreguesia": "01",
                                  "concelho": "CONCELHO TESTE",
                                  "nome": "FREGUESIA TESTE",
                                  "extinta": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("990101"))
                .andExpect(jsonPath("$.concelho").value("CONCELHO TESTE"))
                .andExpect(jsonPath("$.extinta").value(false));

        mockMvc.perform(get("/freguesias/990101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("FREGUESIA TESTE"));

        mockMvc.perform(put("/freguesias/990101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "concelho": "CONCELHO ATUALIZADO",
                                  "nome": "FREGUESIA ATUALIZADA",
                                  "extinta": true
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/freguesias/990101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concelho").value("CONCELHO ATUALIZADO"))
                .andExpect(jsonPath("$.extinta").value(true));

        mockMvc.perform(delete("/freguesias/990101"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejeitaCodigoIncoerente() throws Exception {
        mockMvc.perform(post("/freguesias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "990102",
                                  "codigoDistrito": "99",
                                  "codigoConcelho": "01",
                                  "codigoFreguesia": "01",
                                  "concelho": "CONCELHO TESTE",
                                  "nome": "FREGUESIA TESTE",
                                  "extinta": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("codigoCoerente"));
    }

    @Test
    void aceitaCodigoAlfanumericoDaFonteOficial() throws Exception {
        mockMvc.perform(post("/freguesias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "9803FA",
                                  "codigoDistrito": "98",
                                  "codigoConcelho": "03",
                                  "codigoFreguesia": "FA",
                                  "concelho": "CONCELHO TESTE",
                                  "nome": "FREGUESIA ALFANUMERICA TESTE",
                                  "extinta": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("9803FA"))
                .andExpect(jsonPath("$.extinta").value(true));
    }
}
