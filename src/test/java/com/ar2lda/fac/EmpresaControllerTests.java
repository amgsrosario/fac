package com.ar2lda.fac;

import com.ar2lda.fac.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmpresaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpresaRepository empresaRepository;

    @BeforeEach
    void setup() throws Exception {
        empresaRepository.deleteAll();

        mockMvc.perform(post("/codpostal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "3750-002",
                                  "nome": "Águeda"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void criaConsultaEAtualizaEmpresa() throws Exception {
        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demonstração, Lda",
                                  "nif": "509999999",
                                  "morada": "Rua Principal",
                                  "morada1": "Zona Industrial",
                                  "codPostalId": "3750-002",
                                  "localidade": "Águeda",
                                  "paisId": "PT",
                                  "freguesiaId": "010103",
                                  "capitalSocial": 0,
                                  "matriculaRegistoComercial": "CRC Águeda 509999999",
                                  "cae": "62010",
                                  "descricaoCae": "Atividades de programação informática",
                                  "email": "geral@fac.test",
                                  "web": "https://fac.test",
                                  "nomeComercial": "FAC Demo",
                                  "telefone": "+351 234 000 000",
                                  "iban": "PT50000201231234567890154",
                                  "bicSwift": "CGDIPTPL",
                                  "textoRodape": "Rodape comercial",
                                  "observacoesLegais": "Observacoes legais",
                                  "observacoesComerciaisDefault": "Obrigado pela preferencia."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomeComercial").value("FAC Demo"))
                .andExpect(jsonPath("$.iban").value("PT50000201231234567890154"))
                .andExpect(jsonPath("$.nome").value("FAC Demonstração, Lda"))
                .andExpect(jsonPath("$.capitalSocial").value(0))
                .andExpect(jsonPath("$.concelho").exists());

        mockMvc.perform(get("/empresa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nif").value("509999999"))
                .andExpect(jsonPath("$.freguesiaId").value("010103"));

        mockMvc.perform(put("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demonstração Atualizada, Lda",
                                  "nif": "509999999",
                                  "morada": "Rua Nova",
                                  "codPostalId": "3750-002",
                                  "localidade": "Águeda",
                                  "paisId": "PT",
                                  "capitalSocial": 0,
                                  "matriculaRegistoComercial": "CRC Águeda 509999999",
                                  "cae": "62010",
                                  "descricaoCae": "Atividades de programação informática",
                                  "email": "geral@fac.test",
                                  "iban": "PT50000201231234567890154",
                                  "bicSwift": "CGDIPTPL",
                                  "textoRodape": "Rodape alterado"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/empresa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("FAC Demonstração Atualizada, Lda"))
                .andExpect(jsonPath("$.textoRodape").value("Rodape alterado"))
                .andExpect(jsonPath("$.freguesiaId").doesNotExist());
    }

    @Test
    void rejeitaSegundaEmpresa() throws Exception {
        String empresaJson = """
                {
                  "nome": "FAC Demonstração, Lda",
                  "nif": "509999999",
                  "morada": "Rua Principal",
                  "codPostalId": "3750-002",
                  "localidade": "Águeda",
                  "paisId": "PT",
                  "capitalSocial": 0,
                  "matriculaRegistoComercial": "CRC Águeda 509999999",
                  "cae": "62010",
                  "descricaoCae": "Atividades de programação informática",
                  "email": "geral@fac.test"
                }
                """;

        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(empresaJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(empresaJson))
                .andExpect(status().isConflict());
    }

    @Test
    void rejeitaCapitalSocialNegativo() throws Exception {
        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demonstração, Lda",
                                  "nif": "509999999",
                                  "morada": "Rua Principal",
                                  "codPostalId": "3750-002",
                                  "localidade": "Águeda",
                                  "paisId": "PT",
                                  "capitalSocial": -1,
                                  "matriculaRegistoComercial": "CRC Águeda 509999999",
                                  "cae": "62010",
                                  "descricaoCae": "Atividades de programação informática",
                                  "email": "geral@fac.test"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaNifPtInvalidoEIbanInvalido() throws Exception {
        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demo, Lda",
                                  "nif": "599000001",
                                  "morada": "Rua Principal",
                                  "codPostalId": "3750-002",
                                  "localidade": "Agueda",
                                  "paisId": "PT",
                                  "capitalSocial": 0,
                                  "matriculaRegistoComercial": "CRC Agueda 599000001",
                                  "cae": "62010",
                                  "descricaoCae": "Programacao",
                                  "email": "geral@fac.test"
                                }
                                """))
                .andExpect(status().isBadRequest());

        criarEmpresaMinima();

        mockMvc.perform(put("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demo, Lda",
                                  "nif": "509999999",
                                  "morada": "Rua Principal",
                                  "codPostalId": "3750-002",
                                  "localidade": "Agueda",
                                  "paisId": "PT",
                                  "capitalSocial": 0,
                                  "matriculaRegistoComercial": "CRC Agueda 509999999",
                                  "cae": "62010",
                                  "descricaoCae": "Programacao",
                                  "email": "geral@fac.test",
                                  "iban": "PT50000000000000000000000"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guardaConsultaERemoveLogotipo() throws Exception {
        criarEmpresaMinima();
        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", MediaType.IMAGE_PNG_VALUE, png);

        mockMvc.perform(multipart("/empresa/logotipo").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temLogotipo").value(true))
                .andExpect(jsonPath("$.logotipoMediaType").value(MediaType.IMAGE_PNG_VALUE));

        mockMvc.perform(get("/empresa/logotipo"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(png));

        mockMvc.perform(delete("/empresa/logotipo"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/empresa/logotipo"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaLogotipoComTipoInvalido() throws Exception {
        criarEmpresaMinima();
        MockMultipartFile file = new MockMultipartFile("file", "logo.txt", MediaType.TEXT_PLAIN_VALUE, "texto".getBytes());

        mockMvc.perform(multipart("/empresa/logotipo").file(file))
                .andExpect(status().isBadRequest());
    }

    private void criarEmpresaMinima() throws Exception {
        mockMvc.perform(post("/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "FAC Demo, Lda",
                                  "nif": "509999999",
                                  "morada": "Rua Principal",
                                  "codPostalId": "3750-002",
                                  "localidade": "Agueda",
                                  "paisId": "PT",
                                  "capitalSocial": 0,
                                  "matriculaRegistoComercial": "CRC Agueda 509999999",
                                  "cae": "62010",
                                  "descricaoCae": "Programacao",
                                  "email": "geral@fac.test"
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
