package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "linha_documento_financeiro")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class LinhaDocumentoFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento_financeiro", nullable = false)
    private DocumentoFinanceiro documentoFinanceiro;

    @Column(nullable = false)
    private Integer numeroLinha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pendente", nullable = false)
    private Pendente pendente;

    @Column(nullable = false)
    private LocalDate dataDocumento;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false)
    private Long numeroDocumento;

    @Column(length = 10, nullable = false)
    private String serieDocumento;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal valorDocumento = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal valorPendenteAntes = BigDecimal.ZERO;

    @Column(name = "valor_pagamento_bruto", precision = 19, scale = 6, nullable = false)
    private BigDecimal valorALiquidar = BigDecimal.ZERO;

    @Column(precision = 9, scale = 6)
    private BigDecimal descontoPercentual;

    @Column(name = "valor_desconto_financeiro", precision = 19, scale = 6, nullable = false)
    private BigDecimal descontoValor = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal valorPagamentoLiquido = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal novoValorPendente = BigDecimal.ZERO;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_moeda", nullable = false)
    private Moeda moeda;
}
