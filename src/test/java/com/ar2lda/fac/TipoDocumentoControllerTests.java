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
class TipoDocumentoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void crudTipoDocumento() throws Exception {
        mockMvc.perform(post("/tipos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "Z99",
                                  "descricao": "Documento de teste",
                                  "modeloEmissao1": "Modelo 1",
                                  "areaGestao": 2,
                                  "entidade": 1,
                                  "sinalContabilistico": 1,
                                  "liquidacaoImediata": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("Z99"))
                .andExpect(jsonPath("$.descricao").value("Documento de teste"));

        mockMvc.perform(get("/tipos-documento/Z99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.areaGestao").value(2))
                .andExpect(jsonPath("$.liquidacaoImediata").value(false));

        mockMvc.perform(put("/tipos-documento/Z99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Documento atualizado",
                                  "modeloEmissao1": "Modelo A",
                                  "modeloEmissao2": "Modelo B",
                                  "areaGestao": 3,
                                  "entidade": 1,
                                  "sinalContabilistico": 2,
                                  "liquidacaoImediata": true
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tipos-documento/Z99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Documento atualizado"))
                .andExpect(jsonPath("$.sinalContabilistico").value(2))
                .andExpect(jsonPath("$.liquidacaoImediata").value(true));

        mockMvc.perform(delete("/tipos-documento/Z99"))
                .andExpect(status().isNoContent());
    }

    @Test
    void criaTipoDocumentoComDoisCaracteres() throws Exception {
        mockMvc.perform(post("/tipos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "QY",
                                  "descricao": "Tipo de dois caracteres",
                                  "areaGestao": 2,
                                  "entidade": 1,
                                  "sinalContabilistico": 1,
                                  "liquidacaoImediata": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("QY"));
    }

    @Test
    void rejeitaSinalContabilisticoInvalido() throws Exception {
        mockMvc.perform(post("/tipos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "Z98",
                                  "descricao": "Documento inválido",
                                  "areaGestao": 2,
                                  "entidade": 1,
                                  "sinalContabilistico": 3,
                                  "liquidacaoImediata": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("sinalContabilistico"));
    }
}
