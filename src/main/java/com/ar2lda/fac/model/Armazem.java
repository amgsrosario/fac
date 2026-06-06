package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "armazem")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Armazem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(length = 100, nullable = false)
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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_codpostal", nullable = false)
    @Setter
    private CodPostal codPostal;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String localidade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pais", nullable = false)
    @Setter
    private Pais pais;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_freguesia")
    @Setter
    private Freguesia freguesia;

    public Armazem() {
    }

    public Armazem(String nome, String morada, String morada1, String localidade) {
        this.nome = nome;
        this.morada = morada;
        this.morada1 = morada1;
        this.localidade = localidade;
    }
}
