package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pais")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Pais {
    @Id
    @Column(length = 3, nullable = false)
    @ToString.Include
    private String id;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    public Pais() {

    }

    public Pais(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}
