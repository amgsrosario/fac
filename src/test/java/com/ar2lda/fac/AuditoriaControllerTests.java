package com.ar2lda.fac;

import com.ar2lda.fac.model.AuditoriaEvento;
import com.ar2lda.fac.model.ResultadoAuditoria;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.AuditoriaEventoRepository;
import com.ar2lda.fac.repository.UtilizadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "fac.security.enabled=true")
@Transactional
class AuditoriaControllerTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private AuditoriaEventoRepository repository;
    @Autowired private UtilizadorRepository utilizadorRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setup() throws Exception {
        utilizadorRepository.save(new Utilizador("AUDITADMIN", "Audit Admin", "audit@fac.test",
                passwordEncoder.encode("FacTest1!"), false));
        String response = mockMvc.perform(post("/auth/login").contentType("application/json").content("""
                {"username":"audit@fac.test","password":"FacTest1!"}
                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("token").asText();
    }

    @Test
    void paginaVolumesSemLimiteDeCem() throws Exception {
        int[] volumes = {0, 1, 20, 21, 50, 51, 100, 101, 250};
        for (int volume : volumes) {
            String entityType = "AUDIT_VOLUME_" + volume;
            saveEvents(entityType, volume);

            int expectedPages = (int) Math.ceil(volume / 20.0);
            mockMvc.perform(get("/auditoria")
                            .header("Authorization", "Bearer " + token)
                            .param("entidadeTipo", entityType)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(volume))
                    .andExpect(jsonPath("$.totalPages").value(expectedPages))
                    .andExpect(jsonPath("$.content.length()").value(Math.min(volume, 20)));
        }

        mockMvc.perform(get("/auditoria")
                        .header("Authorization", "Bearer " + token)
                        .param("entidadeTipo", "AUDIT_VOLUME_101")
                        .param("page", "5")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(101));
    }

    @Test
    void combinaFiltrosEOrdenaDeFormaEstavel() throws Exception {
        OffsetDateTime base = OffsetDateTime.parse("2026-09-24T10:00:00Z");
        repository.save(new AuditoriaEvento(base, TipoAuditoriaEvento.DOCUMENTO_EMITIDO, "AUDIT_FILTER", "1",
                "AUDITADMIN", "Audit Admin", "ADMINISTRADOR", ResultadoAuditoria.SUCESSO,
                "FT 1", "Primeiro", "{}"));
        repository.save(new AuditoriaEvento(base.plusHours(1), TipoAuditoriaEvento.DOCUMENTO_ANULADO, "AUDIT_FILTER", "2",
                "OTHER", "Outro", "OPERADOR", ResultadoAuditoria.FALHA,
                "FT 2", "Segundo", "{}"));

        mockMvc.perform(get("/auditoria")
                        .header("Authorization", "Bearer " + token)
                        .param("entidadeTipo", "AUDIT_FILTER")
                        .param("tipoEvento", "DOCUMENTO_ANULADO")
                        .param("utilizadorId", "OTHER")
                        .param("resultado", "FALHA")
                        .param("referencia", "ft 2")
                        .param("desde", "2026-09-24T00:00:00Z")
                        .param("ate", "2026-09-25T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].entidadeId").value("2"));

        mockMvc.perform(get("/auditoria")
                        .header("Authorization", "Bearer " + token)
                        .param("entidadeTipo", "AUDIT_FILTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].entidadeId").value("2"))
                .andExpect(jsonPath("$.content[1].entidadeId").value("1"));
    }

    private void saveEvents(String entityType, int count) {
        OffsetDateTime base = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        repository.saveAll(IntStream.range(0, count)
                .mapToObj(index -> new AuditoriaEvento(base.plusMinutes(index), TipoAuditoriaEvento.LOGIN_SUCESSO,
                        entityType, String.valueOf(index), "AUDITADMIN", "Audit Admin", "ADMINISTRADOR",
                        ResultadoAuditoria.SUCESSO, "REF-" + index, "Volume", "{}"))
                .toList());
    }
}
