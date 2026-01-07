package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mpagamento")
@Getter
@ToString
public class MPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Integer id;

    @Column(length = 30, nullable = false)
    private String nome;

}
