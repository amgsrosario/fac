package com.ar2lda.fac;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReferentialIntegrityTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void todasAsChavesEstrangeirasImpedemEliminacaoEmCascata() {
        List<String> regrasPerigosas = jdbcTemplate.queryForList("""
                select tc.table_name || '.' || kcu.column_name || ' -> ' ||
                       ccu.table_name || ' (' || rc.delete_rule || ')'
                from information_schema.table_constraints tc
                join information_schema.key_column_usage kcu
                  on tc.constraint_name = kcu.constraint_name
                 and tc.constraint_schema = kcu.constraint_schema
                join information_schema.constraint_column_usage ccu
                  on tc.constraint_name = ccu.constraint_name
                 and tc.constraint_schema = ccu.constraint_schema
                join information_schema.referential_constraints rc
                  on tc.constraint_name = rc.constraint_name
                 and tc.constraint_schema = rc.constraint_schema
                where tc.constraint_type = 'FOREIGN KEY'
                  and tc.table_schema = 'public'
                  and rc.delete_rule <> 'NO ACTION'
                order by tc.table_name, kcu.column_name
                """, String.class);

        assertThat(regrasPerigosas)
                .as("Nenhuma relacao do FAC pode apagar ou desligar historico automaticamente")
                .isEmpty();
    }

    @Test
    void todasAsColunasRelacionaisTemChaveEstrangeira() {
        List<String> colunasSemFk = jdbcTemplate.queryForList("""
                select c.table_name || '.' || c.column_name
                from information_schema.columns c
                where c.table_schema = 'public'
                  and c.column_name like 'id\\_%' escape '\\'
                  and not exists (
                    select 1
                    from information_schema.table_constraints tc
                    join information_schema.key_column_usage kcu
                      on tc.constraint_name = kcu.constraint_name
                     and tc.constraint_schema = kcu.constraint_schema
                    where tc.constraint_type = 'FOREIGN KEY'
                      and tc.table_schema = c.table_schema
                      and tc.table_name = c.table_name
                      and kcu.column_name = c.column_name
                  )
                order by c.table_name, c.column_name
                """, String.class);

        assertThat(colunasSemFk)
                .as("Todas as referencias id_* devem ser protegidas pelo PostgreSQL")
                .isEmpty();
    }

    @Test
    void documentosEParametrosTemRelacaoCompostaComSerie() {
        List<String> tabelas = jdbcTemplate.queryForList("""
                select distinct tc.table_name
                from information_schema.table_constraints tc
                join information_schema.constraint_column_usage ccu
                  on tc.constraint_name = ccu.constraint_name
                 and tc.constraint_schema = ccu.constraint_schema
                where tc.constraint_type = 'FOREIGN KEY'
                  and tc.table_schema = 'public'
                  and ccu.table_name = 'serie'
                  and tc.table_name in (
                    'documento_comercial',
                    'documento_financeiro',
                    'parametros_documento_comercial'
                  )
                order by tc.table_name
                """, String.class);

        assertThat(tabelas).containsExactly(
                "documento_comercial",
                "documento_financeiro",
                "parametros_documento_comercial"
        );
    }
}
