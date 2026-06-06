package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "familia")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Familia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(length = 30, nullable = false)
    @Setter
    @ToString.Include
    private String descricao;

    public Familia() {
    }

    public Familia(String descricao) {
        this.descricao = descricao;
    }
}
