package com.ar2lda.fac;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
}
