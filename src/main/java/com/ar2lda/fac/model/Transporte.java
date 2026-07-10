package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "transporte")
@Getter
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class Transporte {

    @Id
    @Column(length = 3, nullable = false)
    @Setter
    @ToString.Include
    private String id;

    @Column(length = 30, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    public Transporte(String id, String nome){
        this.id=id;
        this.nome=nome;
    }

    public Transporte(String nome){
        this.nome=nome;
    }

    public void alterarNome(String nome){
        if(nome==null||nome.isBlank()){
            throw new IllegalArgumentException("O nome do transporte é obrigatório.");
        }
        this.nome=nome;
    }
}
