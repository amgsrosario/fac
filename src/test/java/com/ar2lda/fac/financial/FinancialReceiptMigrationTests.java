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
    @ValueSource(strings={"19","20"})
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
                    s.execute("""
                        insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing;
                        insert into transporte(id,nome) values('MIG','Teste');
                        insert into empresa(id,nome,nif,morada,id_codpostal,localidade,id_pais,capital_social,matricula_registo_comercial,cae,descricao_cae,email)
                          values(1,'Teste','500000000','Rua','1000-020','Lisboa','PT',0,'Registo','00000','Teste','migration@fac.test');
                        insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais)
                          values(1,'Teste','Rua','1000-020','999999990','EUR','CON','MIG','PT');
                        insert into mpagamento(id,nome) values('MIG','Teste');
                        insert into utilizador(codigo,nome,email,password_hash,inativo,papel,criado_em,atualizado_em)
                          values('MIG','Teste','migration@fac.test','fixture',false,'ADMINISTRADOR',current_timestamp,current_timestamp);
                        insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal)
                          values('HRC','Legacy',3,1,2,'RC');
                        insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at) values('HRC','LEG','Legacy',1,'LEG');
                        insert into documento_financeiro(id,id_cliente,id_tipo_documento,serie,numero_documento,data_emissao,id_moeda,
                          valor_pagamento_bruto,valor_desconto_financeiro,valor_pagamento_liquido,id_mpagamento,data_hora_operacao,id_emissor,
                          momento_emissao,observacoes,atcud,codigo_validacao_at,qr_payload,qr_payload_version)
                          values(1,1,'HRC','LEG',1,'2020-01-01','EUR',1.000001,0.000001,1,'MIG',current_timestamp,'MIG',current_timestamp,
                          'historical','LEG-1','LEG','historical QR','AT-QR-1.1');
                        insert into financial_operation(id,empresa_id,cliente_id,moeda_id,kind,request_key,request_payload,actor,reason)
                          values(1,1,1,'EUR','CREATE','migration','fixture','MIG','fixture');
                        insert into financial_position(id,empresa_id,cliente_id,moeda_id,nature,origin,original,available,creation_id)
                          values(1,1,1,'EUR','DEBT','migration',100,100,1);
                        """);
                    c.commit();before=scalar(c,"select concat_ws('|',valor_pagamento_bruto,valor_desconto_financeiro,atcud,qr_payload) from documento_financeiro where id=1");
                }
                var current=Flyway.configure().dataSource(fixture,user,password).cleanDisabled(true).load();
                assertThat(current.migrate().migrationsExecuted).isEqualTo(21-Integer.parseInt(version));current.validate();
                assertThat(current.info().current().getVersion().getVersion()).isEqualTo("21");assertThat(current.migrate().migrationsExecuted).isZero();
                try(var c=DriverManager.getConnection(fixture,user,password);var s=c.createStatement()) {
                    assertThat(scalar(c,"select concat_ws('|',valor_pagamento_bruto,valor_desconto_financeiro,atcud,qr_payload) from documento_financeiro where id=1")).isEqualTo(before);
                    assertThat(scalar(c,"select available::text from financial_position where id=1")).isEqualTo("100.00");
                    assertThat(scalar(c,"select count(*) from financial_receipt")).isEqualTo("0");
                    assertThat(scalar(c,"select count(*) from pg_trigger where tgname in ('financial_receipt_allocation_sealed','financial_receipt_component_sealed') and not tgdeferrable")).isEqualTo("2");
                    s.execute("update documento_financeiro set anulado=true where id=1");
                    assertThat(scalar(c,"select anulado::text from documento_financeiro where id=1")).isEqualTo("true");
                }
            } finally {stmt.execute("drop database "+name);}
        }
    }
}
