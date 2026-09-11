package com.ar2lda.fac;

import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.repository.CodPostalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CodPostalControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private CodPostalRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                new CodPostal("9910-001", "Postal Beja Alfa"),
                new CodPostal("9910-002", "Postal Beja Beta"),
                new CodPostal("9910-003", "Postal Coimbra"),
                new CodPostal("9920-001", "Postal Aveiro"),
                new CodPostal("9920-002", "Postal Braga")));
    }

    @Test
    void listaSemPesquisaMantemContratoPaginado() throws Exception {
        mockMvc.perform(get("/codpostal?page=0&size=2&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    void pesquisaPorIdCompletoEPrefixo() throws Exception {
        mockMvc.perform(get("/codpostal?search=9910-001&page=0&size=25&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value("9910-001"));

        mockMvc.perform(get("/codpostal?search=9910-&page=0&size=2&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void pesquisaNomeSemDistinguirMaiusculas() throws Exception {
        mockMvc.perform(get("/codpostal?search=BEJA&page=0&size=25&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void paginaEOrdenaPorId() throws Exception {
        mockMvc.perform(get("/codpostal?search=9920-&page=1&size=1&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].id").value("9920-002"));
    }

    @Test
    void ordenaPorNomeEDevolvePesquisaSemResultados() throws Exception {
        mockMvc.perform(get("/codpostal?search=9920-&page=0&size=25&sort=nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Postal Aveiro"));

        mockMvc.perform(get("/codpostal?search=sem-resultado-postal&page=0&size=25&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
