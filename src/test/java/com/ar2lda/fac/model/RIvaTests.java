package com.ar2lda.fac.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RIvaTests {

    @Test
    void devolveTaxaCorrespondenteAoTipo() {
        RIva regime = new RIva("NOR", "Portugal Continental");
        regime.substituirTaxas(List.of(
                new RIvaTaxa(null, new TipoTaxaIva("ISENTA", "Isenta", false), new BigDecimal("0.00")),
                new RIvaTaxa(null, new TipoTaxaIva("NORMAL", "Normal", false), new BigDecimal("23.00"))
        ));

        assertThat(regime.getTaxa("ISENTA")).isEqualByComparingTo("0.00");
        assertThat(regime.getTaxa("NORMAL")).isEqualByComparingTo("23.00");
        assertThat(regime.getTaxa("FUTURA")).isNull();
    }
}
