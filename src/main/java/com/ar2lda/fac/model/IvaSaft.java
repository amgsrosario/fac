package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@ToString
@Table(name = "ivasaft")
public class IvaSaft {

    @Id
    @Column(length = 3, nullable = false)
    @Setter
    private String id;

    @Column(length = 50, nullable = false)
    @Setter
    private String nome;


}
