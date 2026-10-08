package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "riva")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class RIva {

    @Enumerated(EnumType.STRING) @Column(length=40, nullable=false) @Setter
    private MercadoFiscal mercado = MercadoFiscal.NACIONAL;
    @Enumerated(EnumType.STRING) @Column(length=30, nullable=false) @Setter
    private TratamentoLiquidacao tratamentoLiquidacao = TratamentoLiquidacao.NORMAL;
    @Column(length=500) @Setter private String fundamentoFiscal;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_misencao") @Setter private MIsencao mIsencao;
    @Column(length=10, nullable=false) @Setter private String jurisdicao = "PT";
    @Enumerated(EnumType.STRING) @Column(length=20, nullable=false) @Setter
    private TerritorioFiscal territorioFiscal = TerritorioFiscal.CONTINENTE;
    @Id
    @Column(length = 3, nullable = false)
    @ToString.Include
    private String id;

    @Column(length = 30, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @OneToMany(mappedBy = "riva", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<RIvaTaxa> taxas = new ArrayList<>();

    public RIva() {

    }

    public RIva(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public List<RIvaTaxa> getTaxas() {
        return List.copyOf(taxas);
    }

    public void substituirTaxas(List<RIvaTaxa> novasTaxas) {
        taxas.clear();
        novasTaxas.forEach(taxa -> {
            taxa.setRiva(this);
            taxas.add(taxa);
        });
    }

    public BigDecimal getTaxa(String tipoTaxaIvaId) {
        return taxas.stream()
                .filter(taxa -> taxa.getTipoTaxaIva().getId().equals(tipoTaxaIvaId))
                .map(RIvaTaxa::getValor)
                .findFirst()
                .orElse(null);
    }
}
