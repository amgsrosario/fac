package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pendente")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Pendente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento_comercial", nullable = false, unique = true)
    private DocumentoComercial documentoComercial;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false)
    private Long numeroDocumento;

    @Column(name = "serie_documento", length = 10, nullable = false)
    private String serieDocumento;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal valorDocumento = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal valorPendente = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate dataDocumento;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_moeda", nullable = false)
    private Moeda moeda;
}
