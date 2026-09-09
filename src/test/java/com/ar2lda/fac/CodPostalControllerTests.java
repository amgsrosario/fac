package com.ar2lda.fac;

import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.CodPostalLocalidade;
import com.ar2lda.fac.repository.CodPostalLocalidadeRepository;
import com.ar2lda.fac.repository.CodPostalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CodPostalControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private CodPostalRepository codPostalRepository;
    @Autowired private CodPostalLocalidadeRepository localidadeRepository;

    @BeforeEach
    void setup() {
        CodPostal postal = codPostalRepository.save(new CodPostal("7800-123", "Beja"));
        postal.setDistrito("Beja");
        postal.setConcelho("Beja");
        postal.setFreguesia("Beja (Salvador e Santa Maria da Feira)");
        localidadeRepository.save(new CodPostalLocalidade(postal, "Herdade do Xisto"));
        codPostalRepository.save(new CodPostal("7800-124", "Beja Norte"));
    }

    @Test
    void pesquisaExactaTemPrioridade() throws Exception {
        mockMvc.perform(get("/codpostal/search?q=7800-123&limit=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigoPostal").value("7800-123"))
                .andExpect(jsonPath("$.content[0].nome").value("Beja"));
    }

    @Test
    void pesquisaPorPrefixoEAlternativaTemLimite() throws Exception {
        mockMvc.perform(get("/codpostal/search").param("q", "Herdade do Xisto").param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].codigoPostal").value("7800-123"));

        mockMvc.perform(get("/codpostal/search?q=7800&limit=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void listaMantemCamposGeograficosECompatibilidade() throws Exception {
        mockMvc.perform(get("/codpostal/7800-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7800-123"))
                .andExpect(jsonPath("$.concelho").value("Beja"));
    }

    @Test
    void ignoraPesquisaVaziaOuCurta() throws Exception {
        mockMvc.perform(get("/codpostal/search").param("q", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
