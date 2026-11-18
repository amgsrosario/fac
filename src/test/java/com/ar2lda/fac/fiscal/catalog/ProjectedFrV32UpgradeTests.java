package com.ar2lda.fac.fiscal.catalog;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties="spring.flyway.target=31")
class ProjectedFrV32UpgradeTests extends ProjectedFrFixture {
    @Autowired DataSource source;

    @Test void existingFrSurvivesUpgradeAndCanBeCorrected() throws Exception {
        assertThat(db.queryForObject("select max(installed_rank) from flyway_schema_history",Integer.class)).isEqualTo(31);
        long id=immediateDraft("SYNTH");
        var issued=issuer.issueProjected(key(),request(id));
        var original=state(id);
        var identity=correctionIdentity(id);
        var flyway=Flyway.configure().dataSource(source).cleanDisabled(true).load();
        assertThat(flyway.migrate().migrationsExecuted).isOne();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("32");
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        noSideEffects(id,original);
        assertThat(correctionIdentity(id)).isEqualTo(identity);
        correction.voidReceiptForFundsNeverReceived(id,"Sem entrada de fundos",true);
        correction.voidInvoice(id,"Emissão indevida");
        assertThat(correction.statement(id)).hasSize(4);
        assertThat(correctionIdentity(id)).isEqualTo(identity);
        assertThat(queries.position(scope(),issued.positionId()).residual()).isZero();
    }
}
