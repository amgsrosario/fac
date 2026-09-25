package com.ar2lda.fac;

import com.ar2lda.fac.model.Familia;
import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.TipoArtigo;
import com.ar2lda.fac.model.TipoTaxaIva;
import com.ar2lda.fac.repository.ArtigoRepository;
import com.ar2lda.fac.repository.FamiliaRepository;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArtigoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FamiliaRepository familiaRepository;

    @Autowired
    private ArtigoRepository artigoRepository;

    @Autowired
    private TipoTaxaIvaRepository tipoTaxaIvaRepository;

    private Long familiaId;
    private Familia familia;
    private TipoTaxaIva ivaNormal;

    @BeforeEach
    void createFamilia() {
        familia = familiaRepository.save(new Familia("Família de teste"));
        familiaId = familia.getId();
        ivaNormal = tipoTaxaIvaRepository.findById("NORMAL").orElseThrow();
    }

    @Test
    void crudArtigo() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ART001", "5601234567890")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("ART001"))
                        .andExpect(jsonPath("$.familiaId").value(familiaId))
                .andExpect(jsonPath("$.tipoArtigo").value("SERVICO"))
                .andExpect(jsonPath("$.ivaCompraId").value("REDUZIDA"))
                .andExpect(jsonPath("$.ivaVendaId").value("NORMAL"))
                .andExpect(jsonPath("$.pvp").value(12.345678));

        mockMvc.perform(get("/artigos/ART001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Artigo de teste"))
                .andExpect(jsonPath("$.codigoIdentificacao").value("5601234567890"));

        mockMvc.perform(put("/artigos/ART001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "abreviatura": "Art. atualizado",
                                  "codigoIdentificacao": "5601234567890",
                                  "descricao": "Artigo atualizado",
                                  "tipoArtigo": "ARTIGO",
                                  "unidade": "UN",
                                  "familiaId": %d,
                                  "peso": 2.500,
                                  "ivaCompraId": "NORMAL",
                                  "ivaVendaId": "NORMAL",
                                  "pvp": 15.000000,
                                  "inativo": true,
                                  "retencao": false,
                                  "observacoes": "Atualizado"
                                }
                                """.formatted(familiaId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/artigos/ART001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Artigo atualizado"))
                .andExpect(jsonPath("$.tipoArtigo").value("ARTIGO"))
                .andExpect(jsonPath("$.inativo").value(true));

        mockMvc.perform(delete("/artigos/ART001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejeitaCodigoForaDoFormato() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("art-001", "5601234567891")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("codigo"));
    }

    @Test
    void rejeitaCodigoIdentificacaoDuplicado() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ART002", "5601234567892")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ART003", "5601234567892")))
                .andExpect(status().isConflict());
    }

    @Test
    void criaArtigoFisico() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ART004", "5601234567894", "ARTIGO")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoArtigo").value("ARTIGO"));
    }

    @Test
    void rejeitaTipoAusente() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "ART005",
                                  "abreviatura": "Art. teste",
                                  "codigoIdentificacao": "5601234567895",
                                  "descricao": "Artigo de teste",
                                  "unidade": "UN",
                                  "familiaId": %d,
                                  "peso": 1.250,
                                  "ivaCompraId": "REDUZIDA",
                                  "ivaVendaId": "NORMAL",
                                  "pvp": 12.345678,
                                  "inativo": false,
                                  "retencao": false,
                                  "observacoes": "ObservaÃ§Ã£o de teste"
                                }
                                """.formatted(familiaId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("tipoArtigo"));
    }

    @Test
    void rejeitaTipoInvalido() throws Exception {
        mockMvc.perform(post("/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ART006", "5601234567896").replace("\"SERVICO\"", "\"PRODUTO\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaArtigosComPaginacaoPesquisaEstadoEVolumeAcimaDoLimiteAntigo() throws Exception {
        List<Artigo> artigos = new ArrayList<>();
        for (int i = 1; i <= 201; i++) {
            Artigo artigo = new Artigo("VOL%04d".formatted(i));
            artigo.setDescricao("Artigo escalabilidade");
            artigo.setAbreviatura("Escala");
            artigo.setTipoArtigo(TipoArtigo.SERVICO);
            artigo.setUnidade("UN");
            artigo.setFamilia(familia);
            artigo.setPeso(BigDecimal.ONE);
            artigo.setIvaCompra(ivaNormal);
            artigo.setIvaVenda(ivaNormal);
            artigo.setPvp(BigDecimal.valueOf(i));
            artigo.setInativo(i % 2 == 0);
            artigo.setRetencao(false);
            artigos.add(artigo);
        }
        artigoRepository.saveAllAndFlush(artigos);

        mockMvc.perform(get("/artigos")
                        .param("search", "Artigo escalabilidade")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "descricao,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(201))
                .andExpect(jsonPath("$.totalPages").value(11))
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.content[0].codigo").value("VOL0001"));

        mockMvc.perform(get("/artigos")
                        .param("search", "Artigo escalabilidade")
                        .param("page", "10")
                        .param("size", "20")
                        .param("sort", "codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].codigo").value("VOL0201"));

        mockMvc.perform(get("/artigos")
                        .param("search", "Artigo escalabilidade")
                        .param("page", "4")
                        .param("size", "50")
                        .param("sort", "codigo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/artigos")
                        .param("search", "VOL0201")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].codigo").value("VOL0201"));

        mockMvc.perform(get("/artigos")
                        .param("search", "Artigo escalabilidade")
                        .param("inativo", "true")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(100));

        mockMvc.perform(get("/artigos").param("sort", "campoInexistente,asc"))
                .andExpect(status().isBadRequest());
    }

    private String createJson(String codigo, String codigoIdentificacao) {
        return createJson(codigo, codigoIdentificacao, "SERVICO");
    }

    private String createJson(String codigo, String codigoIdentificacao, String tipoArtigo) {
        return """
                {
                  "codigo": "%s",
                  "abreviatura": "Art. teste",
                  "codigoIdentificacao": "%s",
                  "descricao": "Artigo de teste",
                  "tipoArtigo": "%s",
                  "unidade": "UN",
                  "familiaId": %d,
                  "peso": 1.250,
                  "ivaCompraId": "REDUZIDA",
                  "ivaVendaId": "NORMAL",
                  "pvp": 12.345678,
                  "inativo": false,
                  "retencao": false,
                  "observacoes": "Observação de teste"
                }
                """.formatted(codigo, codigoIdentificacao, tipoArtigo, familiaId);
    }
}
