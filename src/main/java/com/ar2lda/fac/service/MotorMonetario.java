package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.TipoDescontoLinha;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MotorMonetario {

    private MotorMonetario() {}

    public record Valores(BigDecimal bruto, BigDecimal desconto, BigDecimal base, BigDecimal ivaCalculado) {}

    public static BigDecimal dinheiro(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    public static void validarQuantidadePreco(BigDecimal quantidade, BigDecimal preco) {
        if (quantidade == null || preco == null || quantidade.signum() <= 0 || preco.signum() < 0) {
            throw new BadRequestException("Quantidade, preço ou taxa fiscal inválidos");
        }
        if (quantidade.scale() > 6 || preco.scale() > 6
                || quantidade.precision() - quantidade.scale() > 13
                || preco.precision() - preco.scale() > 13) {
            throw new BadRequestException("Quantidade/preço excedem precisão suportada");
        }
    }

    public static void validarDesconto(BigDecimal desconto) {
        if (desconto != null && (desconto.scale() > 6
                || desconto.precision() - desconto.scale() > 13)) {
            throw new BadRequestException("Desconto excede precisão suportada");
        }
    }

    public static Valores calcular(BigDecimal quantidade, BigDecimal preco, TipoDescontoLinha tipo,
                                   BigDecimal desconto, BigDecimal taxa) {
        validarQuantidadePreco(quantidade, preco);
        validarDesconto(desconto);
        if (taxa == null || taxa.signum() < 0) {
            throw new BadRequestException("Taxa fiscal inválida");
        }
        BigDecimal bruto = dinheiro(quantidade.multiply(preco));
        BigDecimal d = desconto == null ? BigDecimal.ZERO : desconto;
        if (d.signum() < 0 || (tipo == TipoDescontoLinha.PERCENTAGEM && d.compareTo(new BigDecimal("100")) > 0)) {
            throw new BadRequestException("Desconto inválido");
        }
        BigDecimal valor = tipo == TipoDescontoLinha.PERCENTAGEM
                ? dinheiro(bruto.multiply(d).divide(new BigDecimal("100")))
                : dinheiro(d);
        if (valor.compareTo(bruto) > 0) {
            throw new BadRequestException("Desconto superior ao bruto da linha");
        }
        BigDecimal base = bruto.subtract(valor);
        return new Valores(bruto, valor, base, dinheiro(base.multiply(taxa).divide(new BigDecimal("100"))));
    }
}
