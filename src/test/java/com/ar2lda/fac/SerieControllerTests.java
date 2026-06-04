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
class SerieControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void createTipoDocumento() throws Exception {
        mockMvc.perform(post("/tipos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "Z97",
                                  "descricao": "Tipo para séries",
                                  "areaGestao": 2,
                                  "entidade": 1,
                                  "sinalContabilistico": 1,
                                  "liquidacaoImediata": false
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void crudSerie() throws Exception {
        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "Z97A",
                                  "tipoDocumentoId": "Z97",
                                  "nome": "Série de teste",
                                  "codigoAt": "AT-123",
                                  "dataCodigoAt": "2026-06-04"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serie").value("Z97A"))
                .andExpect(jsonPath("$.tipoDocumentoId").value("Z97"));

        mockMvc.perform(get("/series/Z97A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Série de teste"))
                .andExpect(jsonPath("$.codigoAt").value("AT-123"));

        mockMvc.perform(put("/series/Z97A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoDocumentoId": "Z97",
                                  "nome": "Série atualizada",
                                  "codigoAt": "AT-456",
                                  "dataCodigoAt": "2026-06-05"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/series/Z97A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Série atualizada"))
                .andExpect(jsonPath("$.dataCodigoAt").value("2026-06-05"));

        mockMvc.perform(delete("/series/Z97A"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejeitaCodigoAtSemData() throws Exception {
        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "Z97B",
                                  "tipoDocumentoId": "Z97",
                                  "nome": "Série inválida",
                                  "codigoAt": "AT-SEM-DATA"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Código AT e data do código AT devem ser preenchidos em conjunto"));
    }
}
