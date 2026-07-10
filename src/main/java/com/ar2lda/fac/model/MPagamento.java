package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mpagamento")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class MPagamento {

    @Id
    @Column(length = 3, nullable = false)
    @Setter
    @ToString.Include
    private String id;

    @Column(length = 30, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

}
