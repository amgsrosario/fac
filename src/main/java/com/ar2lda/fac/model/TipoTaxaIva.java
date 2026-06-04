package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tipotaxaiva")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class TipoTaxaIva {

    @Id
    @Column(length = 20, nullable = false)
    @ToString.Include
    private String id;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String descricao;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private boolean inativo;

    public TipoTaxaIva() {
    }

    public TipoTaxaIva(String id, String descricao, boolean inativo) {
        this.id = id;
        this.descricao = descricao;
        this.inativo = inativo;
    }
}
