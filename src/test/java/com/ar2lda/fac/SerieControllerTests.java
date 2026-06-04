package com.ar2lda.fac;

import com.ar2lda.fac.service.SerieService;
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
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SerieControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SerieService serieService;

    @BeforeEach
    void createTipoDocumento() throws Exception {
        createTipoDocumento("Z97", "Tipo para séries");
        createTipoDocumento("Z96", "Outro tipo para séries");
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
                .andExpect(jsonPath("$.tipoDocumentoId").value("Z97"))
                .andExpect(jsonPath("$.numerador").value(0));

        mockMvc.perform(get("/series/Z97/Z97A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Série de teste"))
                .andExpect(jsonPath("$.codigoAt").value("AT-123"));

        mockMvc.perform(put("/series/Z97/Z97A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Série atualizada",
                                  "codigoAt": "AT-456",
                                  "dataCodigoAt": "2026-06-05"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/series/Z97/Z97A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Série atualizada"))
                .andExpect(jsonPath("$.dataCodigoAt").value("2026-06-05"))
                .andExpect(jsonPath("$.numerador").value(0));

        mockMvc.perform(delete("/series/Z97/Z97A"))
                .andExpect(status().isNoContent());
    }

    @Test
    void permiteMesmoCodigoSerieEmTiposDocumentoDiferentes() throws Exception {
        createSerie("Z97", "FRC", "FRC para Z97");
        createSerie("Z96", "FRC", "FRC para Z96");

        mockMvc.perform(get("/series/Z97/FRC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoDocumentoId").value("Z97"))
                .andExpect(jsonPath("$.nome").value("FRC para Z97"));

        mockMvc.perform(get("/series/Z96/FRC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoDocumentoId").value("Z96"))
                .andExpect(jsonPath("$.nome").value("FRC para Z96"));
    }

    @Test
    void avancaNumeradorDaSerieSequencialmente() throws Exception {
        createSerie("Z97", "NUM", "Série numerada");

        assertThat(serieService.proximoNumero("Z97", "NUM")).isEqualTo(1L);
        assertThat(serieService.proximoNumero("Z97", "NUM")).isEqualTo(2L);

        mockMvc.perform(get("/series/Z97/NUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numerador").value(2));
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

    private void createTipoDocumento(String id, String descricao) throws Exception {
        mockMvc.perform(post("/tipos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "%s",
                                  "descricao": "%s",
                                  "areaGestao": 2,
                                  "entidade": 1,
                                  "sinalContabilistico": 1,
                                  "liquidacaoImediata": false
                                }
                                """.formatted(id, descricao)))
                .andExpect(status().isCreated());
    }

    private void createSerie(String tipoDocumentoId, String serie, String nome) throws Exception {
        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "%s",
                                  "tipoDocumentoId": "%s",
                                  "nome": "%s"
                                }
                                """.formatted(serie, tipoDocumentoId, nome)))
                .andExpect(status().isCreated());
    }
}
