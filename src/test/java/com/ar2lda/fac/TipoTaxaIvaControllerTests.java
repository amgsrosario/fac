package com.ar2lda.fac;

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
class TipoTaxaIvaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void criaEInativaTipoTaxaIva() throws Exception {
        mockMvc.perform(post("/tipos-taxa-iva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "FUTURA",
                                  "descricao": "Taxa futura",
                                  "inativo": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("FUTURA"))
                .andExpect(jsonPath("$.inativo").value(false));

        mockMvc.perform(put("/tipos-taxa-iva/FUTURA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Taxa futura",
                                  "inativo": true
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tipos-taxa-iva/FUTURA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inativo").value(true));
    }
}
