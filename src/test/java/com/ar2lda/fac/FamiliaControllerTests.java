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
class FamiliaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void crudFamilia() throws Exception {
        String location = mockMvc.perform(post("/familias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Serviços"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.descricao").value("Serviços"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Serviços"));

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Consultoria"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Consultoria"));

        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejeitaDescricaoComMaisDeTrintaCaracteres() throws Exception {
        mockMvc.perform(post("/familias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Descrição com mais de trinta caracteres"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("descricao"));
    }
}
