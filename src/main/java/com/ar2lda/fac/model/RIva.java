package com.ar2lda.fac.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "riva")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class RIva {

    @Id
    @Column(length = 3, nullable = false)
    @Setter
    @ToString.Include
    private String id;

    @Column(length = 30, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(precision = 4, scale = 2)
    @Setter
    @ToString.Include
    private BigDecimal isenta;

    @Column(precision = 4, scale = 2)
    @Setter
    @ToString.Include
    private BigDecimal reduzida;

    @Column(precision = 4, scale = 2)
    @Setter
    @ToString.Include
    private BigDecimal intermedia;

    @Column(precision = 4, scale = 2)
    @Setter
    @ToString.Include
    private BigDecimal normal;
}
