package com.ar2lda.fac;

import com.ar2lda.fac.repository.ParametrosDocumentoComercialRepository;
import jakarta.persistence.EntityManager;
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
class ParametrosDocumentoComercialControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParametrosDocumentoComercialRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Long armazemId;

    @BeforeEach
    void setup() throws Exception {
        repository.deleteAll();
        createTipoDocumento("Z95", 2);
        createTipoDocumento("Z94", 3);
        createSerie("Z95", "BASE");
        createSerie("Z94", "FIN");

        mockMvc.perform(post("/codpostal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"3750-995\",\"nome\":\"Águeda\"}"))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/armazens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Armazém parâmetros documento",
                                  "morada": "Rua de teste",
                                  "codPostalId": "3750-995",
                                  "localidade": "Águeda",
                                  "paisId": "PT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        armazemId = Long.valueOf(response.replaceAll(".*\\\"id\\\":(\\d+).*", "$1"));
    }

    @Test
    void criaConsultaEAtualizaParametros() throws Exception {
        mockMvc.perform(post("/parametros-documento-comercial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Z95", "BASE", armazemId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoDocumentoId").value("Z95"))
                .andExpect(jsonPath("$.serie").value("BASE"))
                .andExpect(jsonPath("$.armazemCargaId").value(armazemId));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/parametros-documento-comercial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serie").value("BASE"));

        mockMvc.perform(put("/parametros-documento-comercial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/parametros-documento-comercial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoDocumentoId").doesNotExist())
                .andExpect(jsonPath("$.serie").doesNotExist())
                .andExpect(jsonPath("$.armazemCargaId").doesNotExist());
    }

    @Test
    void rejeitaSegundoRegistoEConfiguracoesInvalidas() throws Exception {
        mockMvc.perform(post("/parametros-documento-comercial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Z95", "BASE", armazemId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/parametros-documento-comercial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Z95", "BASE", armazemId)))
                .andExpect(status().isConflict());

        repository.deleteAll();

        mockMvc.perform(post("/parametros-documento-comercial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoDocumentoId\":\"Z95\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/parametros-documento-comercial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Z94", "FIN", armazemId)))
                .andExpect(status().isBadRequest());
    }

    private void createTipoDocumento(String id, int areaGestao) throws Exception {
        mockMvc.perform(post("/tipos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "%s",
                                  "descricao": "Tipo %s",
                                  "areaGestao": %d,
                                  "entidade": 1,
                                  "sinalContabilistico": 1,
                                  "liquidacaoImediata": false
                                }
                                """.formatted(id, id, areaGestao)))
                .andExpect(status().isCreated());
    }

    private void createSerie(String tipoDocumentoId, String serie) throws Exception {
        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "%s",
                                  "tipoDocumentoId": "%s",
                                  "nome": "Série %s"
                                }
                                """.formatted(serie, tipoDocumentoId, serie)))
                .andExpect(status().isCreated());
    }

    private String json(String tipoDocumentoId, String serie, Long armazemId) {
        return """
                {
                  "tipoDocumentoId": "%s",
                  "serie": "%s",
                  "armazemCargaId": %d
                }
                """.formatted(tipoDocumentoId, serie, armazemId);
    }
}
