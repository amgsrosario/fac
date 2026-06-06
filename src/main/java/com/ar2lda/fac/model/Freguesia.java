package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "freguesia")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Freguesia {

    @Id
    @Column(length = 6, nullable = false)
    @ToString.Include
    private String codigo;

    @Column(length = 2, nullable = false)
    @Setter
    @ToString.Include
    private String codigoDistrito;

    @Column(length = 2, nullable = false)
    @Setter
    @ToString.Include
    private String codigoConcelho;

    @Column(length = 2, nullable = false)
    @Setter
    @ToString.Include
    private String codigoFreguesia;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String concelho;

    @Column(length = 80, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private boolean extinta;

    public Freguesia() {
    }

    public Freguesia(String codigo, String codigoDistrito, String codigoConcelho, String codigoFreguesia,
                     String concelho, String nome, boolean extinta) {
        this.codigo = codigo;
        this.codigoDistrito = codigoDistrito;
        this.codigoConcelho = codigoConcelho;
        this.codigoFreguesia = codigoFreguesia;
        this.concelho = concelho;
        this.nome = nome;
        this.extinta = extinta;
    }
}
