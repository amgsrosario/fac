package com.ar2lda.fac.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "moeda")
@Getter
@ToString
public class Moeda {

    @Id
    @Column(length = 3, nullable = false)
    @Setter
    private String id;

    @Column(length = 30, nullable = false)
    @Setter
    private String nome;

    @Column(precision = 20, scale = 10)
    @Setter
    private BigDecimal vcompra;

    @Column(precision = 20, scale = 10)
    @Setter
    private BigDecimal vvenda;

    @Column(length = 5, nullable = false)
    @Setter
    private String simbolo;

    @Column(nullable = false)
    @Setter
    private Integer ndecimais;

    @Column(length = 10)
    @Setter
    private String ciso;
}