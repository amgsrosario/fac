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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private JwtEncoder jwtEncoder;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private AuditoriaEventoRepository auditoriaEventoRepository;

    @Autowired private com.ar2lda.fac.security.LoginAttemptLimiter limiter;

    @BeforeEach
    void createUser() {
        // Isolate each HTTP scenario; dedicated tests below and unit tests exercise production limits.
        org.springframework.test.util.ReflectionTestUtils.setField(limiter, "windowStart", Long.MIN_VALUE);
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
    void passwordResetInvalidatesExistingTokenAndRequiresNewPassword() throws Exception {
        String oldToken = login("security@fac.test", "FacTest1!");

        mockMvc.perform(post("/utilizadores/SECTEST/redefinir-password")
                        .header("Authorization", "Bearer " + oldToken)
                        .contentType("application/json")
                        .content("{\"novaPassword\":\"Outra#2026Senha\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login").contentType("application/json")
                        .content("{\"username\":\"security@fac.test\",\"password\":\"FacTest1!\"}"))
                .andExpect(status().isBadRequest());

        String newToken = login("security@fac.test", "Outra#2026Senha");
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());
    }

    @Test
    void deactivationInvalidatesTokenAndReactivationDoesNotRestoreIt() throws Exception {
        String token = login("security@fac.test", "FacTest1!");
        utilizadorRepository.save(new Utilizador("SECADMIN2", "Segundo administrador", "admin2@fac.test",
                passwordEncoder.encode("FacTest1!"), false));
        String adminToken = login("admin2@fac.test", "FacTest1!");

        mockMvc.perform(patch("/utilizadores/SECTEST/estado")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{\"ativo\":false}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login").contentType("application/json")
                        .content("{\"username\":\"security@fac.test\",\"password\":\"FacTest1!\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/utilizadores/SECTEST/estado")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json").content("{\"ativo\":true}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsExpiredTamperedUnknownAndInvalidSessionVersionTokens() throws Exception {
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer "
                        + signedToken("SECTEST", 0L, Instant.now().minusSeconds(120))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer "
                        + signedToken("UNKNOWN", 0L, Instant.now().plusSeconds(60))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer "
                        + signedToken("SECTEST", 99L, Instant.now().plusSeconds(60))))
                .andExpect(status().isUnauthorized());
        String token = signedToken("SECTEST", 0L, Instant.now().plusSeconds(60));
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMissingStringFractionalAndNegativeSessionVersions() throws Exception {
        for (Object version : new Object[]{null, "0", 0.5d, -1L}) {
            mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer "
                            + signedToken("SECTEST", version, Instant.now().plusSeconds(60))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void staleUserCannotOverwritePersistedSessionInvalidation() {
        entityManager.flush();
        Utilizador stale = utilizadorRepository.findById("SECTEST").orElseThrow();
        entityManager.clear();
        Utilizador current = utilizadorRepository.findById("SECTEST").orElseThrow();
        current.invalidarSessoes();
        entityManager.flush();
        entityManager.clear();

        org.assertj.core.api.Assertions.assertThat(utilizadorRepository.findById("SECTEST").orElseThrow()
                .getTokenVersion()).isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            entityManager.merge(stale);
            entityManager.flush();
        }).isInstanceOf(jakarta.persistence.OptimisticLockException.class);
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

        mockMvc.perform(get("/documentos-financeiros/resumos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

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

        mockMvc.perform(post("/utilizadores/SECTEST/redefinir-password").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{\"novaPassword\":\"Outra#2026Senha\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/utilizadores/SECTEST/estado").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{\"ativo\":false}"))
                .andExpect(status().isForbidden());

        org.assertj.core.api.Assertions.assertThat(auditoriaEventoRepository.findAll())
                .anyMatch(evento -> evento.getTipoEvento() == TipoAuditoriaEvento.TENTATIVA_ADMINISTRATIVA_NEGADA
                        && "OPERADOR".equals(evento.getUtilizadorId()));
    }

    @Test
    void permissoesImportacaoExportacaoDadosMestres() throws Exception {
        String adminToken = login("security@fac.test", "FacTest1!");
        Utilizador operador = new Utilizador("OPEXPORT", "Operador Export", "opexport@fac.test",
                passwordEncoder.encode("FacTest1!"), false);
        operador.setPapel(PapelUtilizador.OPERADOR);
        utilizadorRepository.save(operador);
        String operadorToken = login("opexport@fac.test", "FacTest1!");
        Utilizador consulta = new Utilizador("CONEXPORT", "Consulta Export", "conexport@fac.test",
                passwordEncoder.encode("FacTest1!"), false);
        consulta.setPapel(PapelUtilizador.CONSULTA);
        utilizadorRepository.save(consulta);
        String consultaToken = login("conexport@fac.test", "FacTest1!");

        mockMvc.perform(get("/exportacoes/clientes").header("Authorization", "Bearer " + operadorToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/exportacoes/clientes").header("Authorization", "Bearer " + consultaToken))
                .andExpect(status().isOk());

        MockMultipartFile file = new MockMultipartFile("file", "clientes.csv", "text/csv",
                "nome;morada;morada1;localidade;nif;tel;tm;email;email1;tspiva;iban;retencao;inativo;observacoes;codPostalId;paisId;moedaId;mPagamentoId;pPagamentoId;rivaId;transporteId\n".getBytes());

        mockMvc.perform(multipart("/importacoes/clientes/validar").file(file)
                        .header("Authorization", "Bearer " + operadorToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(multipart("/importacoes/clientes/validar").file(file)
                        .header("Authorization", "Bearer " + consultaToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/importacoes/clientes/modelo").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(auditoriaEventoRepository.findAll())
                .anyMatch(evento -> evento.getTipoEvento() == TipoAuditoriaEvento.TENTATIVA_IMPORTACAO_NEGADA
                        && "OPEXPORT".equals(evento.getUtilizadorId()));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
            "ADMINISTRADOR,OPERADOR", "ADMINISTRADOR,CONSULTA", "OPERADOR,CONSULTA",
            "CONSULTA,OPERADOR", "OPERADOR,ADMINISTRADOR", "CONSULTA,ADMINISTRADOR"
    })
    void mudancaEfetivaDePerfilInvalidaTokenEAtualizaAutoridades(
            PapelUtilizador anterior, PapelUtilizador novo) throws Exception {
        Utilizador alvo = new Utilizador("PERFIL", "Perfil teste", "perfil@fac.test",
                passwordEncoder.encode("FacTest1!"), false);
        alvo.setPapel(anterior);
        utilizadorRepository.save(alvo);
        String adminToken = login("SECTEST", "FacTest1!");
        String oldToken = login("PERFIL", "FacTest1!");
        mockMvc.perform(patch("/utilizadores/PERFIL/perfil")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json").content("{\"papel\":\"" + novo + "\"}"))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        org.assertj.core.api.Assertions.assertThat(utilizadorRepository.findById("PERFIL").orElseThrow()
                .getTokenVersion()).isEqualTo(1L);
        mockMvc.perform(get("/series").header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isUnauthorized());
        String response = mockMvc.perform(post("/auth/login").contentType("application/json")
                        .content("{\"username\":\"PERFIL\",\"password\":\"FacTest1!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.papel").value(novo.name()))
                .andReturn().getResponse().getContentAsString();
        var json = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(response);
        var permissions = new java.util.HashSet<String>();
        json.get("permissoes").forEach(value -> permissions.add(value.asText()));
        org.assertj.core.api.Assertions.assertThat(permissions).containsExactlyInAnyOrderElementsOf(
                novo.permissoes().stream().map(Enum::name).toList());
        String newToken = json.get("token").asText();
        mockMvc.perform(get("/series").header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer " + newToken))
                .andExpect(novo == PapelUtilizador.ADMINISTRADOR ? status().isOk() : status().isForbidden());
        org.assertj.core.api.Assertions.assertThat(auditoriaEventoRepository.findAll())
                .anyMatch(evento -> evento.getTipoEvento() == TipoAuditoriaEvento.UTILIZADOR_PERFIL_ALTERADO
                        && "SECTEST".equals(evento.getUtilizadorId()));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(PapelUtilizador.class)
    void mesmoPerfilNaoInvalidaSessao(PapelUtilizador papel) throws Exception {
        Utilizador alvo = new Utilizador("IGUAL", "Mesmo perfil", "igual@fac.test",
                passwordEncoder.encode("FacTest1!"), false);
        alvo.setPapel(papel);
        utilizadorRepository.save(alvo);
        String token = login("IGUAL", "FacTest1!");
        String admin = login("SECTEST", "FacTest1!");
        mockMvc.perform(patch("/utilizadores/IGUAL/perfil")
                        .header("Authorization", "Bearer " + admin)
                        .contentType("application/json").content("{\"papel\":\"" + papel + "\"}"))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        org.assertj.core.api.Assertions.assertThat(utilizadorRepository.findById("IGUAL").orElseThrow()
                .getTokenVersion()).isZero();
        mockMvc.perform(get("/series").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void ultimoAdministradorNaoPodeSerDespromovidoNemInvalidado() throws Exception {
        String token = login("SECTEST", "FacTest1!");
        mockMvc.perform(patch("/utilizadores/SECTEST/perfil")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{\"papel\":\"CONSULTA\"}"))
                .andExpect(status().isConflict());
        org.assertj.core.api.Assertions.assertThat(utilizadorRepository.findById("SECTEST").orElseThrow()
                .getTokenVersion()).isZero();
        mockMvc.perform(get("/utilizadores").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void perfilAlteradoNaoPodeSerRestauradoPorGravacaoDesatualizada() throws Exception {
        Utilizador user = new Utilizador("STALEPROFILE", "Concorrencia perfil", "staleprofile@fac.test",
                passwordEncoder.encode("FacTest1!"), false);
        user.setPapel(PapelUtilizador.OPERADOR);
        utilizadorRepository.saveAndFlush(user);
        entityManager.clear();
        Utilizador stale = utilizadorRepository.findById("STALEPROFILE").orElseThrow();
        entityManager.clear();
        String admin = login("SECTEST", "FacTest1!");
        mockMvc.perform(patch("/utilizadores/STALEPROFILE/perfil")
                        .header("Authorization", "Bearer " + admin)
                        .contentType("application/json").content("{\"papel\":\"CONSULTA\"}"))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        Utilizador current = utilizadorRepository.findById("STALEPROFILE").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(current.getPapel()).isEqualTo(PapelUtilizador.CONSULTA);
        org.assertj.core.api.Assertions.assertThat(current.getTokenVersion()).isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            entityManager.merge(stale);
            entityManager.flush();
        }).isInstanceOf(jakarta.persistence.OptimisticLockException.class);
    }

    @Test
    void pdfFinanceiroExigeCapacidadeEConfiguracaoAtTemGateProprio() throws Exception {
        String token = scopedToken(java.util.List.of("SERIE_GERIR"));
        mockMvc.perform(get("/documentos-financeiros/1/pdf").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/documentos-comerciais/1/pdf").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/documentos-financeiros/resumos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        var tipo = new com.ar2lda.fac.model.TipoDocumento("PAT", "Permissoes AT", null, null, null, null, 1, 1, 1, false);
        entityManager.persist(tipo);
        mockMvc.perform(post("/series").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("""
                {"tipoDocumentoId":"PAT","serie":"SEMAT","nome":"Sem codigo AT"}
                """)).andExpect(status().isCreated());
        mockMvc.perform(post("/series").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("""
                {"tipoDocumentoId":"PAT","serie":"COMAT","nome":"Com codigo AT",
                "codigoAt":"ATPERM","dataCodigoAt":"2026-01-01"}
                """)).andExpect(status().isForbidden());
        mockMvc.perform(put("/series/PAT/SEMAT").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("""
                {"nome":"Alteracao AT","codigoAt":"ATPERM","dataCodigoAt":"2026-01-01"}
                """)).andExpect(status().isForbidden());
    }

    @Test
    void incompleteAndWronglyTypedClaimsAreUnauthorized() throws Exception {
        java.util.Map<String,Object> valid = new java.util.LinkedHashMap<>();
        valid.put("iss","fac"); valid.put("sub","SECTEST");
        valid.put("exp", java.util.Date.from(Instant.now().plusSeconds(300)));
        valid.put("token_version",0L); valid.put("authorities",java.util.List.of("CONFIGURACAO_GERIR"));
        for (String key : java.util.List.of("exp","sub","iss","token_version","authorities")) {
            var claims = new java.util.LinkedHashMap<>(valid); claims.remove(key);
            assertInvalidClaims(claims);
        }
        for (var invalid : java.util.List.of(
                java.util.Map.entry("sub",(Object)""), java.util.Map.entry("sub",(Object)123),
                java.util.Map.entry("iss",(Object)"other"), java.util.Map.entry("iss",(Object)123),
                java.util.Map.entry("exp",(Object)"not-a-date"),
                java.util.Map.entry("authorities",(Object)"CONFIGURACAO_GERIR"),
                java.util.Map.entry("authorities",(Object)java.util.List.of(123)),
                java.util.Map.entry("authorities",(Object)java.util.List.of("UNKNOWN")))) {
            var claims=new java.util.LinkedHashMap<>(valid); claims.put(invalid.getKey(),invalid.getValue());
            assertInvalidClaims(claims);
        }
        mockMvc.perform(get("/utilizadores").header("Authorization","Bearer malformed")).andExpect(status().isUnauthorized());
    }

    private void assertInvalidClaims(java.util.Map<String,Object> claims) throws Exception {
        // Sign raw JSON to test types that claim builders or Nimbus setters would otherwise coerce.
        var secret = context.getBean(javax.crypto.SecretKey.class);
        var header = new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS256);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(claims);
        var token = new com.nimbusds.jose.JWSObject(header,new com.nimbusds.jose.Payload(json));
        token.sign(new com.nimbusds.jose.crypto.MACSigner(secret.getEncoded()));
        mockMvc.perform(get("/utilizadores").header("Authorization","Bearer "+token.serialize()))
                .andExpect(status().isUnauthorized());
    }

    @Autowired private org.springframework.context.ApplicationContext context;

    @Test
    void loginIsLimitedBeforeDatabaseAuditAndDoesNotTrustForwardedHeaders() throws Exception {
        long before=auditoriaEventoRepository.count();
        for (int i=0;i<8;i++) mockMvc.perform(post("/auth/login").contentType("application/json")
                .header("X-Forwarded-For","198.51.100."+i)
                .content("{\"username\":\"missing-gate-user\",\"password\":\"Synthetic1!\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Utilizador ou password invalidos"));
        mockMvc.perform(post("/auth/login").contentType("application/json")
                .header("X-Forwarded-For","203.0.113.99")
                .content("{\"username\":\"missing-gate-user\",\"password\":\"Synthetic1!\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("Retry-After"));
        org.assertj.core.api.Assertions.assertThat(auditoriaEventoRepository.count()).isEqualTo(before+8);
        org.assertj.core.api.Assertions.assertThat(auditoriaEventoRepository.findAll())
                .allMatch(e -> !e.getDadosEssenciais().contains("Synthetic1!"));
    }

    @Test
    void healthIsPublicAndForeignOriginDoesNotReceiveCorsPermission() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/auth/login")
                .header("Origin","https://foreign.invalid").header("Access-Control-Request-Method","POST"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().doesNotExist("Access-Control-Allow-Origin"));
    }

    private String scopedToken(java.util.List<String> authorities) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("fac").issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300)).subject("SECTEST")
                .claim("token_version", 0L).claim("authorities", authorities).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType("application/json").content("""
                {"username":"%s","password":"%s"}
                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("token").asText();
    }

    private String signedToken(String subject, Object version, Instant expiresAt) {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder().issuer("fac").issuedAt(expiresAt.minusSeconds(60))
                .expiresAt(expiresAt).subject(subject)
                .claim("authorities", java.util.List.of("CONFIGURACAO_GERIR"));
        if (version != null) {
            builder.claim("token_version", version);
        }
        JwtClaimsSet claims = builder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
