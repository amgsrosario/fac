package com.ar2lda.fac;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Exercises a real upgrade without using the application's existing database as the fixture. */
class FiscalMigrationCompatibilityTests {

    @Test
    void upgradePreservaCatalogosCustomizadosEEmitidoLegacySemInventarResultadoFiscal() throws Exception {
        String configuredUrl = System.getenv("FAC_TEST_DATASOURCE_URL");
        assumeTrue(configuredUrl != null && !configuredUrl.isBlank(),
                "An explicit disposable PostgreSQL test datasource is required");
        assertThat(configuredUrl).startsWith("jdbc:postgresql://");
        String username = System.getenv().getOrDefault("FAC_TEST_DATASOURCE_USERNAME", "postgres");
        String password = System.getenv().getOrDefault("FAC_TEST_DATASOURCE_PASSWORD", "postgres");
        URI configured = URI.create(configuredUrl.substring("jdbc:".length()));
        assertThat(configured.getPath()).endsWith("_test");
        String ownedDatabase = "tuuli_fiscal_upgrade_" + UUID.randomUUID().toString().replace("-", "") + "_test";
        String fixtureUrl = "jdbc:postgresql://" + configured.getRawAuthority() + "/" + ownedDatabase
                + (configured.getRawQuery() == null ? "" : "?" + configured.getRawQuery());

        try (Connection admin = DriverManager.getConnection(configuredUrl, username, password)) {
            assertThat(value(admin, "select current_database()")).endsWith("_test");
            // Only this randomly named, newly created database can be dropped by this test.
            execute(admin, "CREATE DATABASE " + ownedDatabase);
            try {
                Flyway previous = Flyway.configure().dataSource(fixtureUrl, username, password)
                        .locations("classpath:db/migration").target("15").cleanDisabled(true).load();
                previous.migrate();
                try (Connection fixture = DriverManager.getConnection(fixtureUrl, username, password)) {
                    execute(fixture, """
                            INSERT INTO ivasaft(id,nome) VALUES ('LGY','Classificação legacy');
                            INSERT INTO misencao(id,nome,id_ivasaft) VALUES ('M07','Designação própria preservada','LGY');
                            UPDATE riva SET nome='Regime próprio existente' WHERE id='CON';
                            UPDATE riva_taxa SET valor=19 WHERE id_riva='CON' AND id_tipo_taxa_iva='NORMAL';
                            INSERT INTO codpostal(id,nome) VALUES ('1000-001','Lisboa');
                            INSERT INTO transporte(id,nome) VALUES ('TST','Teste');
                            INSERT INTO armazem(id,nome,morada,id_codpostal,localidade,id_pais)
                              VALUES ('TST','Armazém','Rua','1000-001','Lisboa','PT');
                            INSERT INTO familia(id,descricao) VALUES (1,'Teste');
                            INSERT INTO artigo(codigo,descricao,unidade,id_familia,id_iva_compra,id_iva_venda,pvp,tipo_artigo)
                              VALUES ('LEGACY','Linha antiga','UN',1,'NORMAL','NORMAL',0.335,'SERVICO');
                            INSERT INTO cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais)
                              VALUES (1,'Cliente antigo','Rua','1000-001','999999990','EUR','CON','TST','PT');
                            INSERT INTO tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal)
                              VALUES ('FT','Fatura',2,1,1,'FT');
                            INSERT INTO serie(id_tipo_documento,serie,nome,numerador,codigo_at)
                              VALUES ('FT','LEG','Legacy',1,'ABC');
                            INSERT INTO documento_comercial(id,id_tipo_documento,serie,numero_documento,estado,
                              data_emissao,id_cliente,id_armazem_carga,id_moeda,id_riva,id_ppagamento,id_transporte,
                              cliente_nome,cliente_nif,cliente_morada,cliente_cod_postal,cliente_pais,
                              data_carga,carga_nome,carga_morada,carga_cod_postal,carga_localidade,carga_pais,
                              data_vencimento,fiscal_snapshot_version,regime_iva_codigo,valor_bruto,
                              valor_sujeito_normal,valor_iva_normal,valor_iva_total,valor_total,qr_payload,qr_payload_version,
                              numero_documento_completo,atcud)
                              VALUES (1,'FT','LEG',1,'EMITIDO','2020-01-01',1,'TST','EUR','CON','P30','TST',
                              'Cliente antigo','999999990','Rua','1000-001','PT','2020-01-01','Armazém','Rua',
                              '1000-001','Lisboa','PT','2020-01-31',2,'CON',0.335000,0.335000,
                              0.077050,0.077050,0.412050,'payload histórico','AT-QR-1.1','FT LEG/1','ABC-1');
                            INSERT INTO linha_documento_comercial(id,id_documento_comercial,numero_linha,
                              id_artigo,descricao,quantidade,preco_unitario,valor_bruto,tipo_desconto,desconto,
                              valor_desconto,valor_linha,id_tipo_taxa_iva,percentagem_iva,artigo_codigo,unidade,
                              tipo_taxa_iva_codigo,tipo_taxa_iva_descricao,base_tributavel,valor_imposto,total_linha)
                              VALUES (1,1,1,'LEGACY','Linha antiga',1,0.335000,0.335000,'VALOR',0,0,
                              0.335000,'NORMAL',23,'LEGACY','UN','NORMAL','Normal histórico',
                              0.335000,0.077050,0.412050);
                            """);
                }

                Flyway current = Flyway.configure().dataSource(fixtureUrl, username, password)
                        .locations("classpath:db/migration").cleanDisabled(true).load();
                assertThat(current.migrate().migrationsExecuted).isEqualTo(2);
                current.validate();
                assertThat(current.info().current().getVersion().getVersion()).isEqualTo("17");
                assertThat(current.migrate().migrationsExecuted).isZero();
                try (Connection fixture = DriverManager.getConnection(fixtureUrl, username, password)) {
                    assertThat(value(fixture, "select nome || '|' || id_ivasaft from misencao where id='M07'"))
                            .isEqualTo("Designação própria preservada|LGY");
                    assertThat(value(fixture, "select descricao_oficial from misencao where id='M07'"))
                            .isEqualTo("Isento artigo 9.º do CIVA");
                    assertThat(value(fixture, "select count(*) from misencao where versao_oficial='V4.0 / 18-06-2026'"))
                            .isEqualTo("33");
                    assertThat(value(fixture, "select nome from riva where id='CON'"))
                            .isEqualTo("Regime próprio existente");
                    assertThat(value(fixture, "select valor from riva_taxa where id_riva='CON' and id_tipo_taxa_iva='NORMAL'"))
                            .isEqualTo("19.00");
                    assertThat(value(fixture, """
                            select fiscal_snapshot_version || '|' || valor_total || '|' || qr_payload || '|' || atcud
                            from documento_comercial where id=1
                            """)).isEqualTo("2|0.412050|payload histórico|ABC-1");
                    assertThat(value(fixture, """
                            select base_tributavel || '|' || valor_imposto || '|' || total_linha || '|' || percentagem_iva
                            from linha_documento_comercial where id=1
                            """)).isEqualTo("0.335000|0.077050|0.412050|23.00");
                    assertThat(value(fixture, """
                            select fiscal_motor_version is null and fiscal_mercado is null
                              and fiscal_tratamento_liquidacao is null and fiscal_misencao_codigo is null
                              and fiscal_fundamento is null and fiscal_jurisdicao is null
                              and fiscal_territorio_fiscal is null
                            from documento_comercial where id=1
                            """)).isEqualTo("t");
                    assertThat(value(fixture, """
                            select iva_calculado is null and iva_liquidado is null and fiscal_misencao_codigo is null
                              and fiscal_fundamento is null and fiscal_tratamento_liquidacao is null
                              and fiscal_projecao_qr is null from linha_documento_comercial where id=1
                            """)).isEqualTo("t");
                }
            } finally {
                execute(admin, "DROP DATABASE " + ownedDatabase);
            }
        }
    }

    private static void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static String value(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }
}
