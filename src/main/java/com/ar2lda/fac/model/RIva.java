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
    private String id;

    @Column(length = 30, nullable = false)
    @Setter
    private String nome;

    @Column(precision = 4, scale = 2)
    @Setter
    private BigDecimal isenta;

    @Column(precision = 4, scale = 2)
    @Setter
    private BigDecimal reduzida;

    @Column(precision = 4, scale = 2)
    @Setter
    private BigDecimal intermedia;

    @Column(precision = 4, scale = 2)
    @Setter
    private BigDecimal normal;
}
