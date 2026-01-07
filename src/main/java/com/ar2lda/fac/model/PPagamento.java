package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ppagamento")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class PPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @ToString.Include
    private Integer id;

    @Column(length = 30, nullable = false)
    @ToString.Include
    private String nome;

    @Column(nullable = false)
    @ToString.Include
    private Integer dias;

}
