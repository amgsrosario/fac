package com.ar2lda.fac;

import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.model.PapelUtilizador;
import com.ar2lda.fac.repository.UtilizadorRepository;
import com.ar2lda.fac.repository.AuditoriaEventoRepository;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "fac.security.enabled=true")
@Transactional
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditoriaEventoRepository auditoriaEventoRepository;

    @BeforeEach
    void createUser() {
        utilizadorRepository.save(new Utilizador(
                "SECTEST",
                "Utilizador de Seguranca",
                "security@fac.test",
                passwordEncoder.encode("FacTest1!"),
                false
        ));
    }

    @Test
    void protectedEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/utilizadores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsTokenThatAllowsProtectedAccess() throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "security@fac.test",
                                  "password": "FacTest1!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.codigo").value("SECTEST"))
                .andExpect(jsonPath("$.papel").value("ADMINISTRADOR"))
                .andReturn().getResponse().getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("token").asText();

        mockMvc.perform(get("/utilizadores")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        org.assertj.core.api.Assertions.assertThat(auditoriaEventoRepository.findAll())
                .anyMatch(evento -> evento.getTipoEvento() == TipoAuditoriaEvento.LOGIN_SUCESSO
                        && "SECTEST".equals(evento.getUtilizadorId()));
    }

    @Test
    void inactiveUserCannotLogin() throws Exception {
        Utilizador inactive = new Utilizador(
                "SECINACTIVE",
                "Utilizador Inativo",
                "inactive@fac.test",
                passwordEncoder.encode("FacTest1!"),
                true
        );
        utilizadorRepository.save(inactive);

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "SECINACTIVE",
                                  "password": "FacTest1!"
                                }
                                """))
                .andExpect(status().isBadRequest());
        org.assertj.core.api.Assertions.assertThat(auditoriaEventoRepository.findAll())
                .anyMatch(evento -> evento.getTipoEvento() == TipoAuditoriaEvento.LOGIN_FALHADO);
    }

    @Test
    void utilizadorConsultaNaoPodeGerirSeries() throws Exception {
        Utilizador consulta = new Utilizador("CONSULTA", "Utilizador Consulta", "consulta@fac.test",
                passwordEncoder.encode("FacTest1!"), false);
        consulta.setPapel(PapelUtilizador.CONSULTA);
        utilizadorRepository.save(consulta);
        String response = mockMvc.perform(post("/auth/login").contentType("application/json").content("""
                {"username":"consulta@fac.test","password":"FacTest1!"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("CONSULTA"))
                .andExpect(jsonPath("$.permissoes").isArray())
                .andReturn().getResponse().getContentAsString();
        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("token").asText();

        mockMvc.perform(post("/series").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("""
                                {"tipoDocumentoId":"FT1","serie":"2026","nome":"Serie 2026"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/auditoria").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/clientes").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Permissao funcional insuficiente"));

        mockMvc.perform(post("/documentos-financeiros").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/empresa").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void utilizadorOperadorNaoPodeAcederAuditoriaNemConfiguracao() throws Exception {
        Utilizador operador = new Utilizador("OPERADOR", "Utilizador Operador", "operador@fac.test",
                passwordEncoder.encode("FacTest1!"), false);
        operador.setPapel(PapelUtilizador.OPERADOR);
        utilizadorRepository.save(operador);
        String response = mockMvc.perform(post("/auth/login").contentType("application/json").content("""
                {"username":"operador@fac.test","password":"FacTest1!"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("OPERADOR"))
                .andReturn().getResponse().getContentAsString();
        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("token").asText();

        mockMvc.perform(get("/auditoria").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/empresa").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
