package com.ar2lda.fac.model;
import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="taxa_iva_territorial") @Getter
public class TaxaIvaTerritorial {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=10) private String jurisdicao;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TerritorioFiscal territorio;
 @Column(nullable=false,length=20) private String categoria;
 @Column(nullable=false,precision=5,scale=2) private BigDecimal valor;
 private LocalDate vigenteDesde;
 private LocalDate vigenteAte;
}
