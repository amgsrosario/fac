package com.ar2lda.fac.financial;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.net.URI;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="M03_DISPOSABLE",matches="true")
class ProjectedFrMigrationTests {
    @Test void upgradeV30WithoutBackfillAndSecondStart() throws Exception {
        String url=System.getenv("FAC_TEST_DATASOURCE_URL");
        URI uri=URI.create(url.substring(5));assertThat(uri.getPath()).startsWith("/tuuli_fr_").endsWith("_test");
        String user=System.getenv("FAC_TEST_DATASOURCE_USERNAME"), password=System.getenv("FAC_TEST_DATASOURCE_PASSWORD");
        String database="tuuli_fr_upgrade_"+UUID.randomUUID().toString().replace("-","")+"_test";
        try(var connection=DriverManager.getConnection(url,user,password);var statement=connection.createStatement()) {
            statement.execute("create database "+database);
            String destination="jdbc:postgresql://"+uri.getRawAuthority()+"/"+database;
            try {
                Flyway.configure().dataSource(destination,user,password).target("30").cleanDisabled(true).load().migrate();
                try(var c=DriverManager.getConnection(destination,user,password);var s=c.createStatement()) {
                    s.execute("update riva set nome='FR preserved master' where id='CON'");
                }
                var flyway=Flyway.configure().dataSource(destination,user,password).cleanDisabled(true).load();
                assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);flyway.validate();
                assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("31");
                assertThat(flyway.migrate().migrationsExecuted).isZero();
                try(var c=DriverManager.getConnection(destination,user,password);var s=c.createStatement();var r=s.executeQuery(
                    "select nome,(select count(*) from financial_immediate_invoice),(select count(*) from financial_operation),"+
                    "(select count(*) from fiscal_type_identity) from riva where id='CON'")) {
                    assertThat(r.next()).isTrue();assertThat(r.getString(1)).isEqualTo("FR preserved master");
                    assertThat(r.getLong(2)).isZero();assertThat(r.getLong(3)).isZero();assertThat(r.getLong(4)).isZero();
                }
            } finally {statement.execute("drop database "+database+" with (force)");}
        }
    }
}
