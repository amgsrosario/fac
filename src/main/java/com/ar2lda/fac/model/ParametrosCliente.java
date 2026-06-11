package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "parametros_cliente")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class ParametrosCliente {

    public static final Long PARAMETROS_ID = 1L;

    @Id
    @Column(nullable = false)
    @ToString.Include
    private Long id = PARAMETROS_ID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pais")
    @Setter
    private Pais pais;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_moeda")
    @Setter
    private Moeda moeda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_riva")
    @Setter
    private RIva riva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mpagamento")
    @Setter
    private MPagamento mPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ppagamento")
    @Setter
    private PPagamento pPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transporte")
    @Setter
    private Transporte transporte;

    @Setter
    private Boolean retencao;
}
