package com.ar2lda.fac;

import org.junit.jupiter.api.BeforeEach;
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
class ArmazemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void createCodPostal() throws Exception {
        mockMvc.perform(post("/codpostal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "3750-001",
                                  "nome": "Águeda"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void crudArmazem() throws Exception {
        String location = mockMvc.perform(post("/armazens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Armazém Principal",
                                  "morada": "Rua Principal",
                                  "morada1": "Zona Industrial",
                                  "codPostalId": "3750-001",
                                  "localidade": "Águeda",
                                  "paisId": "PT",
                                  "freguesiaId": "010103"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Armazém Principal"))
                .andExpect(jsonPath("$.codPostalId").value("3750-001"))
                .andExpect(jsonPath("$.paisId").value("PT"))
                .andExpect(jsonPath("$.freguesiaId").value("010103"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.morada").value("Rua Principal"));

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Armazém Secundário",
                                  "morada": "Rua Nova",
                                  "codPostalId": "3750-001",
                                  "localidade": "Águeda",
                                  "paisId": "PT"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Armazém Secundário"));

        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejeitaPaisInexistente() throws Exception {
        mockMvc.perform(post("/armazens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Armazém Teste",
                                  "morada": "Rua Principal",
                                  "codPostalId": "3750-001",
                                  "localidade": "Águeda",
                                  "paisId": "ZZ"
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
