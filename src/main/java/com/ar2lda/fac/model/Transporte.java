package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "transporte")
@Getter
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class Transporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Integer id;

    @Column(length = 30, nullable = false)
    @ToString.Include
    private String nome;

    public Transporte(String nome){
        this.nome=nome;
    }

    public void alterarNome(String nome){
        if(this.nome==null||this.nome.isBlank()){
            throw new IllegalArgumentException("O nome do transporte é obrigatório.");
        }
        this.nome=nome;
    }
}
