package com.ar2lda.fac.fiscal.catalog;

import com.ar2lda.fac.fiscal.catalog.*;
import com.ar2lda.fac.service.FiscalDocumentSignatureSource;
import com.ar2lda.fac.financial.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import com.ar2lda.fac.security.SecuritySettings;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.*;

/** M01 only: authorized #38 snapshot clone; no fixtures, DML or commercial provisioning. */
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named="M01_CLUSTER", matches="[0-9]+")
@SpringBootTest(properties={"spring.jpa.show-sql=false","fac.demo.seed-on-startup=false","fac.security.enabled=true"})
@AutoConfigureMockMvc
@Transactional(readOnly=true)
class M01PreparedSnapshotTests {
    @Autowired JdbcTemplate db;
    @Autowired FiscalCatalog catalog;
    @Autowired FiscalConfigurationReadiness readiness;
    @Autowired FinancialReconciliation diagnostic;
    @Autowired ApplicationContext context;
    @Autowired SecuritySettings securitySettings;
    @Autowired FilterChainProxy securityFilters;
    @Autowired MockMvc http;
    long count(String table) {return db.queryForObject("select count(*) from "+table,Long.class);}
    @Test void exactDisposableIdentityAndV28() {
        assertThat(db.queryForObject("select current_database()",String.class)).isEqualTo("tuuli_m01_test");
        assertThat(db.queryForObject("select system_identifier::text from pg_control_system()",String.class)).isEqualTo(System.getenv("M01_CLUSTER"));
        assertThat(db.queryForObject("select max(version::int) from flyway_schema_history",Integer.class)).isEqualTo(28);
        assertThat(count("flyway_schema_history")).isEqualTo(28);
        assertThat(db.queryForObject("select count(*) from flyway_schema_history where not success",Long.class)).isZero();
    }
    @Test void mastersAndCompleteReferencesRemain() {
        assertThat(count("cliente")).isEqualTo(4);assertThat(count("artigo")).isEqualTo(10);
        assertThat(count("codpostal")).isEqualTo(197772);assertThat(count("freguesia")).isEqualTo(5188);
    }
    @Test void noProvisioningHistoryOrFiscalDefinitions() {
        for(String t:new String[]{"utilizador","tipodocumento","serie","parametros_documento_comercial","documento_comercial","documento_financeiro","linha_documento_comercial","linha_documento_financeiro","pendente","auditoria_evento","financial_operation","financial_position","financial_receipt","fiscal_catalog_revision"}) assertThat(count(t)).as(t).isZero();
    }
    @Test void emptyCatalogRemainsFailClosedWithoutPreventingStartup() {
        assertThat(catalog.verifiedSnapshot()).isEmpty();
        assertThatThrownBy(()->catalog.ftDeferredCreation()).isInstanceOf(IllegalStateException.class);
        var report=readiness.inspect();assertThat(report.rows()).isEmpty();
        assertThat(report.configurationReady()).isFalse();assertThat(report.creationBindingAvailable()).isFalse();
        assertThat(report.operationalActivationPermitted()).isFalse();
    }
    /** Effective state and one anonymous protected GET; not a full security audit. */
    @Test void signatureSourceAbsentAndProtectedRequestRequiresAuthentication() throws Exception {
        assertThat(context.getBeansOfType(FiscalDocumentSignatureSource.class)).isEmpty();
        assertThat(securitySettings.enabled()).isTrue();
        var filters=securityFilters.getFilters("/clientes");
        assertThat(filters).anyMatch(BearerTokenAuthenticationFilter.class::isInstance);
        assertThat(filters).anyMatch(AuthorizationFilter.class::isInstance);
        http.perform(get("/clientes")).andExpect(status().isUnauthorized());
    }
    @Test void reconciliationRemainsEmptyAndInactive() {
        for(Long id:db.queryForList("select id from cliente",Long.class)) {
            var r=diagnostic.inspect(new FinancialCore.Scope(1,id,"EUR"),0,50);
            assertThat(r.summary().documents()).isZero();assertThat(r.summary().comparableDifference()).isNull();
            assertThat(r.operationalActivationPermitted()).isFalse();
        }
    }
    @Test void allInspectionTransactionsAreReadOnly() {
        assertThat(db.queryForObject("show transaction_read_only",String.class)).isEqualTo("on");
    }
}
