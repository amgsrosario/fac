package com.ar2lda.fac;

import com.ar2lda.fac.model.Utilizador;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andReturn().getResponse().getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("token").asText();

        mockMvc.perform(get("/utilizadores")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
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
    }
}
