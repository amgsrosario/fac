package com.ar2lda.fac.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "moeda")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Moeda {

    @Id
    @Column(length = 3, nullable = false)
    @ToString.Include
    private String id;

    @Column(length = 30, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(precision = 20, scale = 10)
    @Setter
    @ToString.Include
    private BigDecimal vcompra;

    @Column(precision = 20, scale = 10)
    @Setter
    @ToString.Include
    private BigDecimal vvenda;

    @Column(length = 5, nullable = false)
    @Setter
    @ToString.Include
    private String simbolo;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private Integer ndecimais;

    @Column(length = 10)
    @Setter
    @ToString.Include
    private String ciso;

    public Moeda() {}

    public Moeda(String id, String nome, BigDecimal vcompra, BigDecimal vvenda, String simbolo, Integer ndecimais, String ciso) {
        this.id = id;
        this.nome = nome;
        this.vcompra = vcompra;
        this.vvenda = vvenda;
        this.simbolo = simbolo;
        this.ndecimais = ndecimais;
        this.ciso = ciso;
    }
}