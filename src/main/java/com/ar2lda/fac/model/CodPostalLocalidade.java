package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "codpostal_localidade",
        uniqueConstraints = @UniqueConstraint(name = "uk_codpostal_localidade_codigo_nome", columnNames = {"codpostal_id", "nome"}),
        indexes = @Index(name = "ix_codpostal_localidade_nome", columnList = "nome"))
@Getter
@Setter
@NoArgsConstructor
public class CodPostalLocalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codpostal_id", nullable = false)
    private CodPostal codPostal;

    @Column(length = 180, nullable = false)
    private String nome;

    public CodPostalLocalidade(CodPostal codPostal, String nome) {
        this.codPostal = codPostal;
        this.nome = nome;
    }
}
