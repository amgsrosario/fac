package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "transporte")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Transporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Integer id;

    @Column(length = 30, nullable = false)
    @ToString.Include
    private String nome;

}
