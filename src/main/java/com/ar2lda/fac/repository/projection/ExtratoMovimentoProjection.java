package com.ar2lda.fac.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface ExtratoMovimentoProjection {
    Long getId();
    LocalDate getData();
    OffsetDateTime getMomento();
    String getTipoDocumentoId();
    String getSerie();
    Long getNumeroDocumento();
    String getDescricao();
    LocalDate getDataVencimento();
    String getMoedaId();
    Integer getSinalContabilistico();
    BigDecimal getValor();
}
