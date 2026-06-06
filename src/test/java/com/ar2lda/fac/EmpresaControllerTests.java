package com.ar2lda.fac;

import com.ar2lda.fac.repository.EmpresaRepository;
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
class EmpresaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpresaRepository empresaRepository;

    @BeforeEach
    void setup() throws Exception {
        empresaRepository.deleteAll();

        mockMvc.perform(post("/codpostal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "3750-002",
                                  "nome": "Águeda"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void criaConsultaEAtualizaEmpresa() throws Exception {
        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demonstração, Lda",
                                  "nif": "509999999",
                                  "morada": "Rua Principal",
                                  "morada1": "Zona Industrial",
                                  "codPostalId": "3750-002",
                                  "localidade": "Águeda",
                                  "paisId": "PT",
                                  "freguesiaId": "010103",
                                  "capitalSocial": 0,
                                  "matriculaRegistoComercial": "CRC Águeda 509999999",
                                  "cae": "62010",
                                  "descricaoCae": "Atividades de programação informática",
                                  "email": "geral@fac.test",
                                  "web": "https://fac.test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("FAC Demonstração, Lda"))
                .andExpect(jsonPath("$.capitalSocial").value(0))
                .andExpect(jsonPath("$.concelho").exists());

        mockMvc.perform(get("/empresa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nif").value("509999999"))
                .andExpect(jsonPath("$.freguesiaId").value("010103"));

        mockMvc.perform(put("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demonstração Atualizada, Lda",
                                  "nif": "509999999",
                                  "morada": "Rua Nova",
                                  "codPostalId": "3750-002",
                                  "localidade": "Águeda",
                                  "paisId": "PT",
                                  "capitalSocial": 0,
                                  "matriculaRegistoComercial": "CRC Águeda 509999999",
                                  "cae": "62010",
                                  "descricaoCae": "Atividades de programação informática",
                                  "email": "geral@fac.test"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/empresa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("FAC Demonstração Atualizada, Lda"))
                .andExpect(jsonPath("$.freguesiaId").doesNotExist());
    }

    @Test
    void rejeitaSegundaEmpresa() throws Exception {
        String empresaJson = """
                {
                  "nome": "FAC Demonstração, Lda",
                  "nif": "509999999",
                  "morada": "Rua Principal",
                  "codPostalId": "3750-002",
                  "localidade": "Águeda",
                  "paisId": "PT",
                  "capitalSocial": 0,
                  "matriculaRegistoComercial": "CRC Águeda 509999999",
                  "cae": "62010",
                  "descricaoCae": "Atividades de programação informática",
                  "email": "geral@fac.test"
                }
                """;

        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(empresaJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(empresaJson))
                .andExpect(status().isConflict());
    }

    @Test
    void rejeitaCapitalSocialNegativo() throws Exception {
        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demonstração, Lda",
                                  "nif": "509999999",
                                  "morada": "Rua Principal",
                                  "codPostalId": "3750-002",
                                  "localidade": "Águeda",
                                  "paisId": "PT",
                                  "capitalSocial": -1,
                                  "matriculaRegistoComercial": "CRC Águeda 509999999",
                                  "cae": "62010",
                                  "descricaoCae": "Atividades de programação informática",
                                  "email": "geral@fac.test"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
