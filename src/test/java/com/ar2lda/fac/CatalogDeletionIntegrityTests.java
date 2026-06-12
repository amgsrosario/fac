package com.ar2lda.fac;

import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogDeletionIntegrityTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private PaisRepository paisRepository;
    @Autowired private MoedaRepository moedaRepository;
    @Autowired private CodPostalRepository codPostalRepository;
    @Autowired private RIvaRepository rIvaRepository;
    @Autowired private TransporteRepository transporteRepository;
    @Autowired private ClienteRepository clienteRepository;

    @BeforeEach
    void setup() {
        Pais pais = paisRepository.save(new Pais("ZX", "Pais integridade"));
        Moeda moeda = moedaRepository.save(new Moeda(
                "ZXC", "Moeda integridade", BigDecimal.ONE, BigDecimal.ONE, "ZX", 2, "998"));
        CodPostal codPostal = codPostalRepository.save(new CodPostal("9999-998", "Local integridade"));
        Transporte transporte = transporteRepository.save(new Transporte("Transporte integridade"));

        Cliente cliente = new Cliente();
        cliente.setNome("Cliente integridade");
        cliente.setMorada("Rua integridade");
        cliente.setLocalidade("Local integridade");
        cliente.setNif("599999998");
        cliente.setEmail("integridade@fac.test");
        cliente.setCodPostal(codPostal);
        cliente.setPais(pais);
        cliente.setMoeda(moeda);
        cliente.setRiva(rIvaRepository.findById("CON").orElseThrow());
        cliente.setTransporte(transporte);
        clienteRepository.saveAndFlush(cliente);
    }

    @Test
    void naoEliminaMoedaUsadaPorCliente() throws Exception {
        assertUsedDeleteIsBlocked("/moedas/ZXC");
    }

    @Test
    void naoEliminaPaisUsadoPorCliente() throws Exception {
        assertUsedDeleteIsBlocked("/paises/ZX");
    }

    @Test
    void eliminaRegistosNuncaUtilizados() throws Exception {
        paisRepository.save(new Pais("ZY", "Pais nao utilizado"));
        moedaRepository.save(new Moeda(
                "ZYC", "Moeda nao utilizada", BigDecimal.ONE, BigDecimal.ONE, "ZY", 2, "997"));

        mockMvc.perform(delete("/paises/ZY")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/moedas/ZYC")).andExpect(status().isNoContent());
    }

    private void assertUsedDeleteIsBlocked(String path) throws Exception {
        mockMvc.perform(delete(path))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Registo nao pode ser eliminado ou alterado porque esta em utilizacao"));
    }
}
