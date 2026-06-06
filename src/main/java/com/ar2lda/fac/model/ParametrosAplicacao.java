package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "parametros_aplicacao")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class ParametrosAplicacao {

    public static final Long PARAMETROS_ID = 1L;

    @Id
    @Column(nullable = false)
    @ToString.Include
    private Long id = PARAMETROS_ID;

    @Column(nullable = false)
    @Setter
    private Integer atrasoCargaMinutos;

    @Column(nullable = false)
    @Setter
    private Integer decimaisQuantidade;

    @Column(nullable = false)
    @Setter
    private Integer decimaisValor;

    public ParametrosAplicacao() {
    }

    public ParametrosAplicacao(Integer atrasoCargaMinutos, Integer decimaisQuantidade, Integer decimaisValor) {
        this.atrasoCargaMinutos = atrasoCargaMinutos;
        this.decimaisQuantidade = decimaisQuantidade;
        this.decimaisValor = decimaisValor;
    }
}
