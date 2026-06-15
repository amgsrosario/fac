package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtcudServiceTests {

    private final AtcudService service = new AtcudService();

    @Test
    void geraAtcudComCodigoENumeroValidos() {
        assertThat(service.gerar("ABCD1234", 27L)).isEqualTo("ABCD1234-27");
    }

    @Test
    void removeEspacosExterioresDoCodigo() {
        assertThat(service.gerar("  ABCD1234  ", 27L)).isEqualTo("ABCD1234-27");
    }

    @Test
    void rejeitaCodigoNuloOuVazio() {
        assertThatThrownBy(() -> service.gerar(null, 27L))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.gerar("   ", 27L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejeitaNumeroNuloZeroOuNegativo() {
        assertThatThrownBy(() -> service.gerar("ABCD1234", null))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.gerar("ABCD1234", 0L))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.gerar("ABCD1234", -1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejeitaPrefixoVisualNoCodigo() {
        assertThatThrownBy(() -> service.gerar("ATCUD:ABCD1234", 27L))
                .isInstanceOf(BadRequestException.class);
    }
}
