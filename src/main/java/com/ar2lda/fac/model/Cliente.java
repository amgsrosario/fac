package com.ar2lda.fac.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

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

    @Column(length = 20, nullable = false, unique = true)
    @Setter
    @ToString.Include
    private String nif;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_moeda", nullable = false)
    @Setter
    private Moeda moeda;

    @Column(length = 20)
    @Setter
    @ToString.Include
    private String tel;

    @Column(length = 20)
    @Setter
    @ToString.Include
    private String tm;

    @Column(length = 120)
    @Setter
    @ToString.Include
    @Email
    private String email;

    @Column(length = 120)
    @Setter
    @ToString.Include
    @Email
    private String email1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mpagamento")
    @Setter
    private MPagamento mPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ppagamento")
    @Setter
    private PPagamento pPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_riva")
    @Setter
    private RIva riva;

    @Column(length = 20)
    @Setter
    @ToString.Include
    private String tspiva;

    @Column(length = 34)
    @Setter
    @ToString.Include
    private String iban;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private boolean retencao = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transporte")
    @Setter
    private Transporte transporte;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private boolean inativo = false;

    @Column(length = 300)
    @Setter
    @ToString.Include
    private String observacoes;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<Morada> moradas = new ArrayList<>();

}