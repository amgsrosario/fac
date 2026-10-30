package com.ar2lda.fac.fiscal.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

/** Deliberately incomplete records exercise inventory only, not valid fiscal emission. */
@SpringBootTest @Transactional
class FiscalConfigurationReadinessTests {
    @Autowired JdbcTemplate db;
    @Autowired FiscalConfigurationReadiness readiness;
    long client;
    @BeforeEach void fixture() {
        db.update("insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing");
        db.update("insert into transporte(id,nome) values('R2T','Teste') on conflict do nothing");
        db.update("insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais) values('R2A','Teste','Rua','1000-020','Lisboa','PT') on conflict do nothing");
        db.update("insert into mpagamento(id,nome) values('R2P','Teste') on conflict do nothing");
        db.update("insert into utilizador(codigo,nome,email,password_hash,inativo) values('R2_EMISSOR','Teste','r2@fac.test','!NOT-A-LOGIN!',false)");
        db.update("insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico) values('R2D','Inventario sem classificacao fiscal inferida',3,1,2)");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador) values('R2D','NORMAL','Teste',0)");
        client=db.queryForObject("select nextval('cliente_id_seq')",Long.class);
        db.update("insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais) values(?,'Teste','Rua','1000-020',?,'EUR','CON','R2T','PT')",client,Long.toString(900000000+client));
    }
    void commercial(String payload,String version) {
        db.update("""
          insert into documento_comercial(id_tipo_documento,serie,estado,data_emissao,id_cliente,
            id_armazem_carga,id_moeda,id_riva,id_ppagamento,id_transporte,cliente_nome,cliente_nif,
            cliente_morada,cliente_cod_postal,cliente_pais,data_carga,carga_nome,carga_morada,
            carga_cod_postal,carga_localidade,carga_pais,data_vencimento,qr_payload,qr_payload_version)
          values('R2D','NORMAL','RASCUNHO','2026-01-01',?,'R2A','EUR','CON','P30','R2T',
            'Teste','999999990','Rua','1000-020','PT','2026-01-01','Teste','Rua',
            '1000-020','Lisboa','PT','2026-01-31',?,?)
          """,client,payload,version);
    }
    void financial(String payload,String version) {
        db.update("""
          insert into documento_financeiro(id_cliente,id_tipo_documento,serie,numero_documento,
            data_emissao,id_moeda,id_mpagamento,data_hora_operacao,id_emissor,momento_emissao,
            qr_payload,qr_payload_version)
          values(?,'R2D','NORMAL',1,'2026-01-01','EUR','R2P',now(),'R2_EMISSOR',now(),?,?)
          """,client,payload,version);
    }
    FiscalConfigurationReadiness.Row only() {
        var rows=readiness.inspect().rows().stream().filter(r->r.id().equals("R2D")).toList();
        assertThat(rows).hasSize(1);return rows.getFirst();
    }
    @Test void ordinaryConfigurationDoesNotAcquireDemoFinding() {
        assertThat(only().findings()).doesNotContain(FiscalConfigurationReadiness.Finding.DEMO_ARTIFACTS);
    }
    @ParameterizedTest @ValueSource(strings={"DEMO26","DEMO-X","AT-CODE"})
    void flagsSeriesOrValidationCodeMarkers(String marker) {
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at) values('R2D',?,'Teste',0,?)",
                marker.equals("AT-CODE")?"OTHER":marker,marker.equals("AT-CODE")?"DEMO-FT":null);
        assertThat(only().findings()).contains(FiscalConfigurationReadiness.Finding.DEMO_ARTIFACTS);
    }
    @ParameterizedTest @ValueSource(strings={"commercial-payload","commercial-version","financial-payload","financial-version"})
    void flagsEitherPayloadOrVersionWithoutDependingOnSeries(String source) {
        String payload=source.endsWith("payload")?"TUULI-DEMO-1\n{}":null;
        String version=source.endsWith("version")?"TUULI-DEMO-1":null;
        if(source.startsWith("commercial"))commercial(payload,version);else financial(payload,version);
        assertThat(only().findings()).contains(FiscalConfigurationReadiness.Finding.DEMO_ARTIFACTS);
        assertThat(readiness.inspect().operationalActivationPermitted()).isFalse();
    }
    @Test void overlappingMarkersDoNotDuplicateRowsOrChangeHistoryAndInventoryDoesNotWrite() {
        commercial("TUULI-DEMO-1\n{}","TUULI-DEMO-1");financial("TUULI-DEMO-1\n{}","TUULI-DEMO-1");
        db.update("insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at) values('R2D','DEMO26','Teste',0,'DEMO-RC'),('R2D','DEMO-X','Teste',0,'DEMO-RC')");
        String before=db.queryForObject("select (select count(*) from documento_comercial)||'/'||(select count(*) from documento_financeiro)||'/'||(select count(*) from financial_operation)",String.class);
        var row=only();assertThat(row.findings()).contains(FiscalConfigurationReadiness.Finding.DEMO_ARTIFACTS);
        assertThat(row.commercialHistory()).isZero();assertThat(row.financialHistory()).isEqualTo(1);
        assertThat(db.queryForObject("select (select count(*) from documento_comercial)||'/'||(select count(*) from documento_financeiro)||'/'||(select count(*) from financial_operation)",String.class)).isEqualTo(before);
    }
}
