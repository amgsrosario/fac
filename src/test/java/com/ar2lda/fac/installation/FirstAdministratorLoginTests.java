package com.ar2lda.fac.installation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"fac.security.enabled=true","spring.jpa.show-sql=false","fac.demo.seed-on-startup=false"})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="M02_DISPOSABLE",matches="true")
class FirstAdministratorLoginTests {
    @Autowired JdbcTemplate db;
    @Autowired MockMvc http;
    @Test void provisionedAdministratorLogsInWithExistingJwtAndPermissions() throws Exception {
        String operator=System.getenv("M02_OPERATOR");
        db.execute("grant usage on schema public to "+operator);
        db.execute("grant execute on function public.local_admin_identity() to "+operator);
        db.execute("grant execute on function public.provision_first_administrator(text,text,uuid,text,text,text,timestamptz,text,text,text,text) to "+operator);
        try(var c=DriverManager.getConnection(System.getenv("FAC_TEST_DATASOURCE_URL"),operator,System.getenv("M02_OPERATOR_PASSWORD"));
            var query=c.createStatement();var result=query.executeQuery("select * from public.local_admin_identity()")) {
            result.next();var auth=new FirstAdministratorAuthorization(System.getenv("FAC_TEST_DATASOURCE_URL"),operator,
                result.getString("database_name"),result.getString("cluster_id"),result.getObject("installation_id",UUID.class),
                "SYNTHETIC-LOGIN","Synthetic operator",Instant.now().plusSeconds(300));
            new FirstAdministratorProvisioner().provision(c,auth,
                new FirstAdministratorProvisioner.Administrator("M02LOGIN","Synthetic","m02login@example.test"),"Unique8!Synthetic".toCharArray());
        }
        http.perform(get("/utilizadores")).andExpect(status().isUnauthorized());
        String body=http.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"m02login@example.test\",\"password\":\"Unique8!Synthetic\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.papel").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.permissoes",org.hamcrest.Matchers.hasItem("CONFIGURACAO_GERIR"))).andReturn().getResponse().getContentAsString();
        String token=new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("token").asText();
        http.perform(get("/utilizadores").header("Authorization","Bearer "+token)).andExpect(status().isOk());
        http.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"m02login@example.test\",\"password\":\"Incorrect8!\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(db.queryForObject("select count(*) from local_admin_provisioning where consumed_at is not null",Long.class)).isEqualTo(1);
        assertThat(db.queryForObject("select count(*) from auditoria_evento where referencia='SYNTHETIC-LOGIN'",Long.class)).isEqualTo(1);
        assertThat(db.queryForObject("select count(*) from auditoria_evento where tipo_evento='LOGIN_SUCESSO'",Long.class)).isEqualTo(1);
    }
}
