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

import java.math.BigDecimal;

@Entity
@Table(name = "artigo")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Artigo {

    @Id
    @Column(length = 50, nullable = false)
    @ToString.Include
    private String codigo;

    @Column(length = 30)
    @Setter
    @ToString.Include
    private String abreviatura;

    @Column(name = "codigo_identificacao", length = 100, unique = true)
    @Setter
    @ToString.Include
    private String codigoIdentificacao;

    @Column(length = 80, nullable = false)
    @Setter
    @ToString.Include
    private String descricao;

    @Column(length = 3, nullable = false)
    @Setter
    @ToString.Include
    private String unidade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_familia", nullable = false)
    @Setter
    private Familia familia;

    @Column(precision = 15, scale = 3)
    @Setter
    private BigDecimal peso;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_iva_compra", nullable = false)
    @Setter
    private TipoTaxaIva ivaCompra;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_iva_venda", nullable = false)
    @Setter
    private TipoTaxaIva ivaVenda;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal pvp;

    @Column(nullable = false)
    @Setter
    private boolean inativo;

    @Column(nullable = false)
    @Setter
    private boolean retencao;

    @Column(length = 250)
    @Setter
    private String observacoes;

    public Artigo() {
    }

    public Artigo(String codigo) {
        this.codigo = codigo;
    }
}
