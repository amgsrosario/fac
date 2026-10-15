package com.ar2lda.fac.financial;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.net.URI;
import java.sql.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class FinancialReceiptMigrationTests {
    static String scalar(Connection c,String sql) throws SQLException {
        try(var s=c.createStatement();var r=s.executeQuery(sql)) { assertThat(r.next()).isTrue();return r.getString(1); }
    }
    @ParameterizedTest
    @ValueSource(strings={"19","20","21","22","23"})
    void upgradePreservesLegacyPrecisionAndLedgerAndSecondStart(String version) throws Exception {
        String url=System.getenv("FAC_TEST_DATASOURCE_URL");assertThat(url).isNotBlank();
        URI u=URI.create(url.substring(5));assertThat(u.getPath()).endsWith("_test");
        String user=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_USERNAME","postgres");
        String password=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_PASSWORD","postgres");
        String name="op22_b1_upgrade_"+UUID.randomUUID().toString().replace("-","")+"_test";
        try(var admin=DriverManager.getConnection(url,user,password);var stmt=admin.createStatement()) {
            stmt.execute("create database "+name);String fixture="jdbc:postgresql://"+u.getRawAuthority()+"/"+name;
            try {
                Flyway.configure().dataSource(fixture,user,password).target(version).cleanDisabled(true).load().migrate();
                String before;
                try(var c=DriverManager.getConnection(fixture,user,password);var s=c.createStatement()) {
                    c.setAutoCommit(false);
                    seed(c,version);
                    if(Integer.parseInt(version)>=20) linkedReceipt(c,version);
                    c.commit();before=scalar(c,"select concat_ws('|',valor_pagamento_bruto,valor_desconto_financeiro,atcud,qr_payload) from documento_financeiro where id=1");
                }
                var current=Flyway.configure().dataSource(fixture,user,password).cleanDisabled(true).load();
                assertThat(current.migrate().migrationsExecuted).isEqualTo(24-Integer.parseInt(version));current.validate();
                assertThat(current.info().current().getVersion().getVersion()).isEqualTo("24");assertThat(current.migrate().migrationsExecuted).isZero();
                try(var c=DriverManager.getConnection(fixture,user,password);var s=c.createStatement()) {
                    assertThat(scalar(c,"select concat_ws('|',valor_pagamento_bruto,valor_desconto_financeiro,atcud,qr_payload) from documento_financeiro where id=1")).isEqualTo(before);
                    assertThat(scalar(c,"select available::text from financial_position where id=1")).isEqualTo("100.00");
                    assertThat(scalar(c,"select count(*) from financial_receipt")).isEqualTo(Integer.parseInt(version)>=20?"1":"0");
                    assertThat(scalar(c,"select count(*) from pg_trigger where tgname in ('financial_receipt_allocation_sealed','financial_receipt_component_sealed') and not tgdeferrable")).isEqualTo("2");
                    assertThat(scalar(c,"select data_valor is null from documento_financeiro where id=1")).isEqualTo(version.equals("23")?"f":"t");
                    if(Integer.parseInt(version)>=20) {
                        assertThat(scalar(c,"select effect_date::text from financial_operation_effect where id=3")).isEqualTo("2020-01-01");
                        assertThat(scalar(c,"select data_valor is null from documento_financeiro where id=2")).isEqualTo(version.equals("23")?"f":"t");
                        if(!version.equals("23")) {
                            assertThatThrownBy(()->s.execute("insert into financial_operation(id,empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason,reverse_of) values(4,1,1,'EUR','REVERSE','bad-history','fixture','MIG','fixture',3)"))
                                .isInstanceOf(SQLException.class).hasMessageContaining("no historical inference");
                        }
                    }
                    s.execute("update documento_financeiro set anulado=true where id=1");
                    assertThat(scalar(c,"select anulado::text from documento_financeiro where id=1")).isEqualTo("true");
                }
            } finally {stmt.execute("drop database "+name);}
        }
    }
    /** Issued against the real old schema; never disables triggers or fabricates NULL under V23. */
    static void linkedReceipt(Connection c,String version) throws SQLException {
        try(var s=c.createStatement()) {
            s.execute("""
                insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais) values('MIG','Teste','Rua','1000-020','Lisboa','PT');
                insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal) values('MFT','FT',2,1,1,'FT');
                insert into serie(id_tipo_documento,serie,nome,numerador) values('MFT','LEG','Teste',1);
                insert into documento_comercial(id,id_tipo_documento,serie,numero_documento,estado,data_emissao,id_cliente,
                  id_armazem_carga,id_moeda,id_riva,id_ppagamento,id_transporte,cliente_nome,cliente_nif,cliente_morada,
                  cliente_cod_postal,cliente_pais,data_carga,carga_nome,carga_morada,carga_cod_postal,carga_localidade,carga_pais,
                  data_vencimento,valor_total,numero_documento_completo)
                values(1,'MFT','LEG',1,'EMITIDO','2019-01-01',1,'MIG','EUR','CON','P30','MIG','Teste','999999990','Rua',
                  '1000-020','PT','2019-01-01','Teste','Rua','1000-020','Lisboa','PT','2019-01-31',100,'MFT LEG/1');
                insert into pendente(id,id_documento_comercial,id_cliente,id_tipo_documento,numero_documento,serie_documento,
                  valor_documento,valor_pendente,data_documento,data_vencimento,id_moeda)
                values(1,1,1,'MFT',1,'LEG',100,100,'2019-01-01','2019-01-31','EUR');
                insert into financial_operation(id,empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason)
                values(2,1,1,'EUR','CREATE','origin','fixture','MIG','fixture');
                insert into financial_position(id,empresa_id,cliente_id,moeda_id,nature,origin,original,available,creation_id)
                values(2,1,1,'EUR','DEBT','DOCUMENTO_COMERCIAL:1',100,90,2);
                insert into financial_document_origin(document_id,position_id) values(1,2);
                insert into documento_financeiro(id,id_cliente,id_tipo_documento,serie,numero_documento,data_emissao,id_moeda,
                  valor_pagamento_bruto,valor_desconto_financeiro,valor_pagamento_liquido,id_mpagamento,id_emissor,data_hora_operacao,momento_emissao%s)
                values(2,1,'HRC','LEG',2,'2020-01-01','EUR',10,0,10,'MIG','MIG',current_timestamp,current_timestamp%s);
                insert into financial_operation(id,empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason)
                values(3,1,1,'EUR','SETTLE','historical-receipt','fixture','MIG','fixture');
                insert into financial_allocation(id,operation_id,position_id,empresa_id,cliente_id,moeda_id,amount,effect)
                values(1,3,2,1,1,'EUR',10,'CONSUME');
                insert into financial_component(id,operation_id,kind,amount) values(1,3,'CASH_IN',10);
                insert into linha_documento_financeiro(id,id_documento_financeiro,numero_linha,id_pendente,
                  valor_pendente_antes,valor_pagamento_bruto,valor_desconto_financeiro,valor_pagamento_liquido,
                  data_documento,data_vencimento,id_tipo_documento,numero_documento,serie_documento,valor_documento,
                  desconto_percentual,novo_valor_pendente,id_moeda)
                values(1,2,1,1,100,10,0,10,'2019-01-01','2019-01-31','MFT',1,'LEG',100,0,90,'EUR');
                insert into financial_receipt(document_id,operation_id,empresa_id,cliente_id,moeda_id,request_key,request_payload)
                values(2,3,1,1,'EUR','historical-receipt','fixture');
                insert into financial_receipt_line(line_id,allocation_id) values(1,1);
                """.formatted(version.equals("23")?",data_valor":"",version.equals("23")?",'2020-01-01'":""));
        }
    }

    private static void seed(Connection c,String version) throws SQLException {
        try(var s=c.createStatement()) {
            s.execute("""
                        insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing;
                        insert into transporte(id,nome) values('MIG','Teste');
                        insert into empresa(id,nome,nif,morada,id_codpostal,localidade,id_pais,capital_social,matricula_registo_comercial,cae,descricao_cae,email)
                          values(1,'Teste','500000000','Rua','1000-020','Lisboa','PT',0,'Registo','00000','Teste','migration@fac.test');
                        insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais)
                          values(1,'Teste','Rua','1000-020','999999990','EUR','CON','MIG','PT'),
                            (2,'Generic','Rua','1000-020','999999991','EUR','CON','MIG','PT');
                        insert into mpagamento(id,nome) values('MIG','Teste');
                        insert into utilizador(codigo,nome,email,password_hash,inativo,papel,criado_em,atualizado_em)
                          values('MIG','Teste','migration@fac.test','fixture',false,'ADMINISTRADOR',current_timestamp,current_timestamp);
                        insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal)
                          values('HRC','Legacy',3,1,2,'RC');
                        insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at) values('HRC','LEG','Legacy',1,'LEG');
                        insert into documento_financeiro(id,id_cliente,id_tipo_documento,serie,numero_documento,data_emissao,id_moeda,
                          valor_pagamento_bruto,valor_desconto_financeiro,valor_pagamento_liquido,id_mpagamento,data_hora_operacao,id_emissor,
                          %smomento_emissao,observacoes,atcud,codigo_validacao_at,qr_payload,qr_payload_version)
                          values(1,1,'HRC','LEG',1,'2020-01-01','EUR',1.000001,0.000001,1,'MIG',current_timestamp,'MIG',%scurrent_timestamp,
                          'historical','LEG-1','LEG','historical QR','AT-QR-1.1');
                        insert into financial_operation(id,empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason)
                          values(1,1,2,'EUR','CREATE','migration','fixture','MIG','fixture');
                        insert into financial_position(id,empresa_id,cliente_id,moeda_id,nature,origin,original,available,creation_id)
                          values(1,1,2,'EUR','DEBT','migration',100,100,1);
                        """.formatted(version.equals("23")?"data_valor,":"",version.equals("23")?"'2020-01-01',":""));
        }
    }

    @org.junit.jupiter.api.Test
    void historicalNullReceiptUsesCompatibilityButRejectsUnsafeHistory() throws Exception {
        String url=System.getenv("FAC_TEST_DATASOURCE_URL");assertThat(url).isNotBlank();
        URI u=URI.create(url.substring(5));assertThat(u.getPath()).endsWith("_test");
        String user=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_USERNAME","postgres");
        String password=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_PASSWORD","postgres");
        String name="op22_slice1b_null_"+UUID.randomUUID().toString().replace("-","")+"_test";
        try(var admin=DriverManager.getConnection(url,user,password);var stmt=admin.createStatement()) {
            stmt.execute("create database "+name);String fixture="jdbc:postgresql://"+u.getRawAuthority()+"/"+name;
            try {
                Flyway.configure().dataSource(fixture,user,password).target("22").cleanDisabled(true).load().migrate();
                try(var c=DriverManager.getConnection(fixture,user,password)) {
                    c.setAutoCommit(false);seed(c,"22");linkedReceipt(c,"22");c.commit();
                }
                var flyway=Flyway.configure().dataSource(fixture,user,password).cleanDisabled(true).load();
                assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);flyway.validate();
                assertThat(flyway.migrate().migrationsExecuted).isZero();
                var jdbc=new org.springframework.jdbc.core.JdbcTemplate(new org.springframework.jdbc.datasource.DriverManagerDataSource(fixture,user,password));
                var queries=new JdbcFinancialQueries(jdbc);var scope=new FinancialCore.Scope(1,1,"EUR");
                var date=java.time.LocalDate.of(2020,1,1);
                assertThat(queries.balanceAt(scope,date).openDebt()).isEqualByComparingTo("90");
                var diagnostic=new FinancialReconciliation(jdbc);
                var observed=diagnostic.inspect(scope,0,500).page().items().getFirst();
                assertThat(observed.findings()).contains(FinancialReconciliation.Finding.HISTORICAL_VALUE_DATE_MISSING);
                assertThat(observed.nullValueDateLines()).isEqualTo(1);
                assertThat(diagnostic.receipts(scope,1,0,500).items().getFirst().valueDate()).isNull();
                var original=queries.operation(scope,3);
                assertThat(original.financialDocument().orElseThrow().valueDate()).isNull();
                assertThat(original.financialEffectDate()).contains(date);
                // A genuine pre-V23 receipt is not itself a reason to invent or reject a date.
                // A later undated generic effect makes this scope historically unsupported.
                try(var c=DriverManager.getConnection(fixture,user,password);var sql=c.createStatement()) {
                    c.setAutoCommit(false);
                    sql.execute("""
                        insert into financial_operation(id,empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason)
                          values(4,1,1,'EUR','SETTLE','undated','fixture','MIG','fixture');
                        insert into financial_allocation(id,operation_id,position_id,empresa_id,cliente_id,moeda_id,amount,effect)
                          values(2,4,2,1,1,'EUR',1,'CONSUME');
                        update financial_position set available=89 where id=2;
                        insert into financial_component(id,operation_id,kind,amount) values(2,4,'CASH_IN',1);
                        """);c.commit();
                }
                assertThat(queries.operation(scope,4).financialEffectDate()).isEmpty();
                assertThatThrownBy(()->queries.balanceAt(scope,date))
                    .isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("TUULI-FIN-TIME-001");
                assertThat(jdbc.queryForObject("select data_valor is null from documento_financeiro where id=2",Boolean.class)).isTrue();
            } finally {stmt.execute("drop database "+name);}
        }
    }

}
