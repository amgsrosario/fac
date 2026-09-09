package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "codpostal")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class CodPostal {

    @Id
    @Column(length = 20, nullable = false)
    @ToString.Include
    private String id;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(length = 100)
    @Setter
    private String distrito;

    @Column(length = 100)
    @Setter
    private String concelho;

    @Column(length = 100)
    @Setter
    private String freguesia;

    @OneToMany(mappedBy = "codPostal", fetch = FetchType.LAZY)
    private List<CodPostalLocalidade> localidades = new ArrayList<>();

    public CodPostal() {}

    public CodPostal(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}
