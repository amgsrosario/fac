package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QrFiscalValidatorTests {

    private final QrFiscalValidator validator = new QrFiscalValidator();

    @Test
    void rejeitaCamposForaDaOrdemOficial() {
        assertThatThrownBy(() -> validator.validate(
                "A:500000000*B:509999990*C:PT*D:FT*E:N*F:20260615*H:ABCD1234-1*G:FT 2026/1"
                        + "*I1:PT*N:23.00*O:123.00*Q:YhGV*R:9999"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Campos do QR fiscal fora da ordem oficial");
    }

    @Test
    void rejeitaValoresMonetariosSemDuasCasasDecimais() {
        assertThatThrownBy(() -> validator.validate(
                "A:500000000*B:509999990*C:PT*D:FT*E:N*F:20260615*G:FT 2026/1*H:ABCD1234-1"
                        + "*I1:PT*N:23*O:123.00*Q:YhGV*R:9999"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Campo N do QR fiscal deve ter duas casas decimais");
    }

    @Test
    void rejeitaCertificadoNaoNumerico() {
        assertThatThrownBy(() -> validator.validate(
                "A:500000000*B:509999990*C:PT*D:FT*E:N*F:20260615*G:FT 2026/1*H:ABCD1234-1"
                        + "*I1:PT*N:23.00*O:123.00*Q:YhGV*R:ABCD"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Campo R do QR fiscal deve conter o número do certificado");
    }
}
