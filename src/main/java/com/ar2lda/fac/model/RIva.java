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
