package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "codpostal")
@Getter
@ToString
public class CodPostal {

    @Id
    @Column(length = 20, nullable = false)
    private String id;

    @Column(length = 50, nullable = false)
    @Setter
    private String nome;

}
