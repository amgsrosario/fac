package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "morada")
@Getter

@ToString(onlyExplicitlyIncluded = true)
public class Morada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @Setter
    private Cliente cliente;

    @Column(length = 80, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(length = 60, nullable = false)
    @Setter
    @ToString.Include
    private String morada;

    @Column(length = 60)
    @Setter
    @ToString.Include
    private String morada1;

    @Column(length = 50)
    @Setter
    @ToString.Include
    private String localidade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_codpostal", nullable = false)
    @Setter
    private CodPostal codPostal;

    @Column(length = 20, nullable = false)
    @Setter
    @ToString.Include
    private String tipo;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private boolean principal = false;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private boolean inativo = false;

}