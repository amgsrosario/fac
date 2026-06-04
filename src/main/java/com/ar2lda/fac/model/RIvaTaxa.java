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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(
        name = "riva_taxa",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_riva", "id_tipo_taxa_iva"})
)
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class RIvaTaxa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_riva", nullable = false)
    @Setter
    private RIva riva;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_taxa_iva", nullable = false)
    @Setter
    @ToString.Include
    private TipoTaxaIva tipoTaxaIva;

    @Column(precision = 4, scale = 2, nullable = false)
    @Setter
    @ToString.Include
    private BigDecimal valor;

    public RIvaTaxa() {
    }

    public RIvaTaxa(RIva riva, TipoTaxaIva tipoTaxaIva, BigDecimal valor) {
        this.riva = riva;
        this.tipoTaxaIva = tipoTaxaIva;
        this.valor = valor;
    }
}
