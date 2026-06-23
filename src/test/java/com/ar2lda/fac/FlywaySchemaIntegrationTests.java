package com.ar2lda.fac;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywaySchemaIntegrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void esquemaEstaNaVersaoQuatroEContemSnapshotFiscalEBlocoTres() {
        Integer versao = jdbcTemplate.queryForObject(
                "select max(version::integer) from flyway_schema_history where success",
                Integer.class
        );
        Integer colunasSnapshot = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.columns
                where table_schema = 'public'
                  and ((table_name = 'documento_comercial' and column_name = 'fiscal_snapshot_version')
                    or (table_name = 'linha_documento_comercial' and column_name = 'artigo_codigo'))
                """, Integer.class);

        Integer estruturasBlocoTres = jdbcTemplate.queryForObject("""
                select count(*) from information_schema.columns
                where table_schema = 'public'
                  and ((table_name = 'documento_comercial' and column_name = 'motivo_anulacao')
                    or (table_name = 'utilizador' and column_name = 'papel')
                    or (table_name = 'auditoria_evento' and column_name = 'dados_essenciais'))
                """, Integer.class);

        assertThat(versao).isEqualTo(4);
        assertThat(colunasSnapshot).isEqualTo(2);
        assertThat(estruturasBlocoTres).isEqualTo(3);
    }
}
