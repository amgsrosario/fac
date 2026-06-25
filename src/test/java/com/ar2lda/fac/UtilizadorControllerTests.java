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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
                                  "papel": "ADMINISTRADOR",
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
                                  "email": "admin.fac@example.com"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/utilizadores/ADMIN1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Administrador FAC"))
                .andExpect(jsonPath("$.inativo").value(false));

        createUtilizador("ADMIN2", "admin2@example.com");
        mockMvc.perform(patch("/utilizadores/ADMIN1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inativo").value(true));
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
                                  "papel": "ADMINISTRADOR",
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
                                  "papel": "ADMINISTRADOR",
                                  "inativo": false
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void protegeUltimoAdministradorAtivo() throws Exception {
        createUtilizador("ADMINUNICO", "admin.unico@example.com");

        mockMvc.perform(patch("/utilizadores/ADMINUNICO/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\":false}"))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/utilizadores/ADMINUNICO/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"papel\":\"OPERADOR\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void permiteRedefinirPasswordSemExporSegredo() throws Exception {
        createUtilizador("RESET1", "reset1@example.com");
        String before = repository.findById("RESET1").orElseThrow().getPasswordHash();

        mockMvc.perform(post("/utilizadores/RESET1/redefinir-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaPassword\":\"Nova#2026Reset\"}"))
                .andExpect(status().isNoContent());

        String after = repository.findById("RESET1").orElseThrow().getPasswordHash();
        assertThat(after).startsWith("$2").isNotEqualTo(before).doesNotContain("Nova#2026Reset");
    }

    @Test
    void listaComFiltrosDePerfilEEstado() throws Exception {
        createUtilizador("ADMINLIST", "admin.list@example.com");

        mockMvc.perform(post("/utilizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "CONSULTALIST",
                                  "nome": "Consulta Lista",
                                  "email": "consulta.list@example.com",
                                  "password": "Fac#2026Teste",
                                  "papel": "CONSULTA",
                                  "inativo": false
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/utilizadores?papel=CONSULTA&ativo=true&q=consulta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value("CONSULTALIST"))
                .andExpect(jsonPath("$.content[0].papel").value("CONSULTA"))
                .andExpect(jsonPath("$.content[0].ativo").value(true));
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
                  "papel": "ADMINISTRADOR",
                  "inativo": false
                                }
                                """.formatted(codigo, email)))
                .andExpect(status().isCreated());
    }
}
