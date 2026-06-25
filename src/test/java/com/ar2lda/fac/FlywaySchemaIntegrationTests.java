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
    void esquemaEstaNaVersaoCincoEContemSnapshotFiscalBlocoTresEMD28() {
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

        Integer estruturasMd28 = jdbcTemplate.queryForObject("""
                select count(*) from information_schema.columns
                where table_schema = 'public'
                  and ((table_name = 'empresa' and column_name in ('nome_comercial', 'iban', 'logo', 'texto_rodape'))
                    or (table_name = 'utilizador' and column_name in ('criado_em', 'atualizado_em', 'ultimo_login_em'))
                    or (table_name = 'documento_comercial' and column_name in ('emitente_iban', 'emitente_logo', 'emitente_texto_rodape')))
                """, Integer.class);

        assertThat(versao).isEqualTo(5);
        assertThat(colunasSnapshot).isEqualTo(2);
        assertThat(estruturasBlocoTres).isEqualTo(3);
        assertThat(estruturasMd28).isEqualTo(10);
    }
}
