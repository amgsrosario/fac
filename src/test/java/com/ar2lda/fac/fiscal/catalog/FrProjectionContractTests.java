package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ar2lda.fac.controller.dto.FiscalProjectionDto;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FrProjectionContractTests {
    final ObjectMapper json=new ObjectMapper();
    @Test void explicitFrTupleDoesNotBecomeDeferredFt()throws Exception {
        var d=json.readTree(ProjectedFrIntegrationTests.FR_DEFINITION);
        assertThat(FrFiscalSemantics.permits(d,"FT",2,1,1,true)).isTrue();
        assertThat(FrFiscalSemantics.permits(d,"FT",2,1,1,false)).isFalse();
        assertThat(FrFiscalSemantics.permits(d,"FR",2,1,1,true)).isFalse();
    }
    @Test void malformedOrAmbiguousProjectionFailsClosed()throws Exception {
        for(String source:new String[]{ProjectedFrIntegrationTests.FR_DEFINITION.replace("Fatura-recibo","Fatura"),
            ProjectedFrIntegrationTests.FR_DEFINITION.replace("\"externalType\":\"FR\"","\"externalType\":\"FT\""),
            ProjectedFrIntegrationTests.FR_DEFINITION.replace("\"schemaVersion\":2","\"schemaVersion\":4294967298"),
            ProjectedFrIntegrationTests.FR_DEFINITION.replace("\"liquidacaoImediata\":true","\"liquidacaoImediata\":false")}) {
            var d=json.readTree(source);assertThatThrownBy(()->FrFiscalSemantics.validate(d)).isInstanceOf(IllegalStateException.class);
        }
    }
    @Test void readonlyProjectionExposesNoInventedHashOrPaymentMechanism() {
        var p=new FiscalProjectionDto("FR","FT",9000100L,"Fatura-recibo");
        assertThat(p.externalType()).isEqualTo("FR");assertThat(p.catalogCode()).isEqualTo("FT");
        assertThatThrownBy(()->new FiscalProjectionDto("FR","FT",null,"Fatura-recibo")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->new FiscalProjectionDto("FT","FT",1L,"Fatura")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void experimentalBeansAreAbsentWithoutTheProfile() {
        new org.springframework.boot.test.context.runner.ApplicationContextRunner()
            .withUserConfiguration(GovernedFrConfiguration.class,com.ar2lda.fac.financial.FinancialFrIssuer.class)
            .run(context->{assertThat(context).doesNotHaveBean(GovernedFrConfiguration.class);assertThat(context).doesNotHaveBean(com.ar2lda.fac.financial.FinancialFrIssuer.class);});
    }

}
