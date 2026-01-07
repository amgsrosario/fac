package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ppagamento")
@Getter
@ToString
public class PPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Integer id;

    @Column(length = 30, nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer dias;

}
