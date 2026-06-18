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
import static org.hamcrest.Matchers.hasSize;

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
                .andExpect(jsonPath("$.numerador").value(0))
                .andExpect(jsonPath("$.temCodigoAt").value(true));

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
    void aceitaTipoDocumentoSaftComDoisCaracteres() throws Exception {
        createTipoDocumento("QZ", "Tipo de dois caracteres");

        createSerie("QZ", "AZ26", "Série dois caracteres");

        mockMvc.perform(get("/series/QZ/AZ26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoDocumentoId").value("QZ"))
                .andExpect(jsonPath("$.serie").value("AZ26"));
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

    @Test
    void normalizaDadosTextuaisAntesDePersistirEProcurar() throws Exception {
        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "  NORM  ",
                                  "tipoDocumentoId": " Z97 ",
                                  "nome": "  Série normalizada  ",
                                  "codigoAt": "  AT-NORM  ",
                                  "dataCodigoAt": "2026-06-06"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serie").value("NORM"))
                .andExpect(jsonPath("$.tipoDocumentoId").value("Z97"))
                .andExpect(jsonPath("$.nome").value("Série normalizada"))
                .andExpect(jsonPath("$.codigoAt").value("AT-NORM"));

        assertThat(serieService.getById(" Z97 ", " NORM ").serie()).isEqualTo("NORM");
    }

    @Test
    void rejeitaSerieDuplicadaDepoisDeNormalizar() throws Exception {
        createSerie("Z97", "DUP", "Primeira série");

        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": " DUP ",
                                  "tipoDocumentoId": " Z97 ",
                                  "nome": "Duplicada"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejeitaTipoDocumentoInexistenteEComprimentosInvalidos() throws Exception {
        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "A",
                                  "tipoDocumentoId": "X99",
                                  "nome": "Tipo inexistente"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "12345678901",
                                  "tipoDocumentoId": "Z97",
                                  "nome": "Série longa"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "CURTA",
                                  "tipoDocumentoId": "Z",
                                  "nome": "Tipo curto"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tipo de documento deve ter pelo menos 2 caracteres"));
    }

    @Test
    void permiteAtualizarCodigoAtAntesDaUtilizacaoENormalizaValores() throws Exception {
        createSerie("Z97", "EDIT", "Série editável");

        mockMvc.perform(put("/series/Z97/EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "  Nome atualizado  ",
                                  "codigoAt": "  AT-EDIT  ",
                                  "dataCodigoAt": "2026-06-07"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/series/Z97/EDIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome atualizado"))
                .andExpect(jsonPath("$.codigoAt").value("AT-EDIT"))
                .andExpect(jsonPath("$.numerador").value(0));
    }

    @Test
    void impedeAlterarCodigoAtOuEliminarSerieUtilizadaMasPermiteAlterarNome() throws Exception {
        createSerieWithCodigoAt("Z97", "USED", "Série utilizada", "AT-USED", "2026-06-08");
        assertThat(serieService.proximoNumero("Z97", "USED")).isEqualTo(1L);

        mockMvc.perform(put("/series/Z97/USED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Outro nome",
                                  "codigoAt": "AT-NEW",
                                  "dataCodigoAt": "2026-06-09"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Não é possível alterar o código AT de uma série já utilizada"));

        mockMvc.perform(put("/series/Z97/USED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "  Nome permitido  ",
                                  "codigoAt": " AT-USED ",
                                  "dataCodigoAt": "2026-06-08"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/series/Z97/USED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Não é possível eliminar uma série já utilizada"));
    }

    @Test
    void atualizacaoNaoAceitaCamposDaIdentidadeOuNumerador() throws Exception {
        createSerie("Z97", "IMM", "Série imutável");

        mockMvc.perform(put("/series/Z97/IMM")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Tentativa",
                                  "tipoDocumentoId": "Z96",
                                  "serie": "OUTRA",
                                  "numerador": 99
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/series/Z97/IMM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoDocumentoId").value("Z97"))
                .andExpect(jsonPath("$.serie").value("IMM"))
                .andExpect(jsonPath("$.numerador").value(0));
    }

    @Test
    void listaSeriesEDevolveNotFoundParaChaveInexistente() throws Exception {
        createSerie("Z97", "LISTA", "Série listada");

        mockMvc.perform(get("/series?size=1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/series/Z97/NAOEXISTE"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/series/Z97/NAOEXISTE"))
                .andExpect(status().isNotFound());
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

    private void createSerieWithCodigoAt(
            String tipoDocumentoId,
            String serie,
            String nome,
            String codigoAt,
            String dataCodigoAt
    ) throws Exception {
        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serie": "%s",
                                  "tipoDocumentoId": "%s",
                                  "nome": "%s",
                                  "codigoAt": "%s",
                                  "dataCodigoAt": "%s"
                                }
                                """.formatted(serie, tipoDocumentoId, nome, codigoAt, dataCodigoAt)))
                .andExpect(status().isCreated());
    }
}
