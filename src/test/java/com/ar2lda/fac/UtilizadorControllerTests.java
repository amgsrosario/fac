package com.ar2lda.fac;

import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.UtilizadorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UtilizadorControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilizadorRepository repository;

    @Test
    void crudUtilizadorSemExporPassword() throws Exception {
        mockMvc.perform(post("/utilizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "admin1",
                                  "nome": "Administrador",
                                  "email": "ADMIN@EXAMPLE.COM",
                                  "password": "Fac#2026Admin",
                                  "inativo": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("ADMIN1"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        Utilizador stored = repository.findById("ADMIN1").orElseThrow();
        assertThat(stored.getPasswordHash()).isNotEqualTo("Fac#2026Admin").startsWith("$2");

        mockMvc.perform(get("/utilizadores/admin1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Administrador"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(put("/utilizadores/ADMIN1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Administrador FAC",
                                  "email": "admin.fac@example.com",
                                  "inativo": true
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/utilizadores/ADMIN1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Administrador FAC"))
                .andExpect(jsonPath("$.inativo").value(true));

        mockMvc.perform(delete("/utilizadores/ADMIN1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejeitaPasswordFraca() throws Exception {
        mockMvc.perform(post("/utilizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "TESTE1",
                                  "nome": "Teste",
                                  "email": "teste1@example.com",
                                  "password": "password",
                                  "inativo": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
    }

    @Test
    void rejeitaEmailDuplicado() throws Exception {
        createUtilizador("TESTE2", "teste@example.com");

        mockMvc.perform(post("/utilizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "TESTE3",
                                  "nome": "Outro teste",
                                  "email": "TESTE@EXAMPLE.COM",
                                  "password": "Fac#2026Teste",
                                  "inativo": false
                                }
                                """))
                .andExpect(status().isConflict());
    }

    private void createUtilizador(String codigo, String email) throws Exception {
        mockMvc.perform(post("/utilizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "%s",
                                  "nome": "Utilizador de teste",
                                  "email": "%s",
                                  "password": "Fac#2026Teste",
                                  "inativo": false
                                }
                                """.formatted(codigo, email)))
                .andExpect(status().isCreated());
    }
}
