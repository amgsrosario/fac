package com.ar2lda.fac.model;

import java.io.Serializable;

public record SerieId(
        String tipoDocumento,
        String serie
) implements Serializable {
}
