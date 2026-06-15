package com.ar2lda.fac.repository.projection;

import java.math.BigDecimal;

public interface ExtratoAnteriorProjection {
    String getMoedaId();
    BigDecimal getDebito();
    BigDecimal getCredito();
}
