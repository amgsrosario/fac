package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.TipoDescontoLinha;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class MotorMonetarioTests {
    @Test
    void fechaBrutoEImpostoPorLinhaAntesDeSomar() {
        var v = calcular("1", "0.335", "0", "23");
        assertThat(v.bruto()).isEqualByComparingTo("0.34");
        assertThat(v.ivaCalculado()).isEqualByComparingTo("0.08");
        assertThat(v.base().add(v.ivaCalculado())).isEqualByComparingTo("0.42");
        assertThat(v.bruto().scale()).isEqualTo(2);
        assertThat(v.ivaCalculado().scale()).isEqualTo(2);
    }

    @Test
    void tresLinhasDeUmCentimoNaoAcumulamFraccoesDeIva() {
        var v = calcular("1", "0.01", "0", "23");
        assertThat(v.ivaCalculado()).isEqualByComparingTo("0.00");
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < 3; i++) total = total.add(v.base()).add(v.ivaCalculado());
        assertThat(total).isEqualByComparingTo("0.03");
    }

    @Test
    void descontoPercentualParteDoBrutoJaFechado() {
        var v = MotorMonetario.calcular(new BigDecimal("1"), new BigDecimal("0.335"),
                TipoDescontoLinha.PERCENTAGEM, new BigDecimal("10"), new BigDecimal("23"));
        assertThat(v.bruto()).isEqualByComparingTo("0.34");
        assertThat(v.desconto()).isEqualByComparingTo("0.03");
        assertThat(v.base()).isEqualByComparingTo("0.31");
        assertThat(v.ivaCalculado()).isEqualByComparingTo("0.07");
        assertThat(v.base().add(v.ivaCalculado())).isEqualByComparingTo("0.38");
    }

    @Test
    void rejeitaQuantidadePrecoTaxaEDescontoInvalidos() {
        assertThatThrownBy(() -> calcular("0", "1", "0", "23")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> calcular("1", "-1", "0", "23")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> calcular("1", "1", "0", "-1")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> calcular("1", "1", "2", "23")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void descontoAbsolutoFechaSemDuploArredondamento() {
        var v = calcular("1", "1", "0.005", "23");
        assertThat(v.desconto()).isEqualByComparingTo("0.01");
        assertThat(v.base()).isEqualByComparingTo("0.99");
        assertThat(v.ivaCalculado()).isEqualByComparingTo("0.23");
        assertThatThrownBy(() -> calcular("1", "1", "0.0049999", "23"))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("precisão");
    }

    private MotorMonetario.Valores calcular(String qtd, String preco, String desconto, String taxa) {
        return MotorMonetario.calcular(new BigDecimal(qtd), new BigDecimal(preco),
                TipoDescontoLinha.VALOR, new BigDecimal(desconto), new BigDecimal(taxa));
    }
}
