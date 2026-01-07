package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "misencao")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class MIsencao {

    @Id
    @Column(length = 3, nullable = false)
    @Setter
    @ToString.Include
    private String id;

    @Column(length = 60, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ivasaft", nullable = false)
    @Setter
    private IvaSaft ivaSaft;



}
