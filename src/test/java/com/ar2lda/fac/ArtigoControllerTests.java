package com.ar2lda.fac;

import com.ar2lda.fac.model.Familia;
import com.ar2lda.fac.repository.FamiliaRepository;
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
class ArtigoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FamiliaRepository familiaRepository;

    private Long familiaId;

    @BeforeEach
    void createFamilia() {
        familiaId = familiaRepository.save(new Familia("Família de teste")).getId();
    }

    @Test
    void crudArtigo() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ART001", "5601234567890")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("ART001"))
                .andExpect(jsonPath("$.familiaId").value(familiaId))
                .andExpect(jsonPath("$.ivaCompraId").value("REDUZIDA"))
                .andExpect(jsonPath("$.ivaVendaId").value("NORMAL"))
                .andExpect(jsonPath("$.pvp").value(12.345678));

        mockMvc.perform(get("/artigos/ART001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Artigo de teste"))
                .andExpect(jsonPath("$.codigoIdentificacao").value("5601234567890"));

        mockMvc.perform(put("/artigos/ART001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "abreviatura": "Art. atualizado",
                                  "codigoIdentificacao": "5601234567890",
                                  "descricao": "Artigo atualizado",
                                  "unidade": "UN",
                                  "familiaId": %d,
                                  "peso": 2.500,
                                  "ivaCompraId": "NORMAL",
                                  "ivaVendaId": "NORMAL",
                                  "pvp": 15.000000,
                                  "inativo": true,
                                  "retencao": false,
                                  "observacoes": "Atualizado"
                                }
                                """.formatted(familiaId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/artigos/ART001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Artigo atualizado"))
                .andExpect(jsonPath("$.inativo").value(true));

        mockMvc.perform(delete("/artigos/ART001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejeitaCodigoForaDoFormato() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("art-001", "5601234567891")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("codigo"));
    }

    @Test
    void rejeitaCodigoIdentificacaoDuplicado() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ART002", "5601234567892")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ART003", "5601234567892")))
                .andExpect(status().isConflict());
    }

    private String createJson(String codigo, String codigoIdentificacao) {
        return """
                {
                  "codigo": "%s",
                  "abreviatura": "Art. teste",
                  "codigoIdentificacao": "%s",
                  "descricao": "Artigo de teste",
                  "unidade": "UN",
                  "familiaId": %d,
                  "peso": 1.250,
                  "ivaCompraId": "REDUZIDA",
                  "ivaVendaId": "NORMAL",
                  "pvp": 12.345678,
                  "inativo": false,
                  "retencao": false,
                  "observacoes": "Observação de teste"
                }
                """.formatted(codigo, codigoIdentificacao, familiaId);
    }
}
