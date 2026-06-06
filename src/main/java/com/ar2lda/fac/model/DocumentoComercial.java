package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "documento_comercial")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class DocumentoComercial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    @Setter
    private TipoDocumento tipoDocumento;

    @Column(length = 10, nullable = false)
    @Setter
    @ToString.Include
    private String serie;

    @Column(name = "numero_documento")
    @Setter
    @ToString.Include
    private Long numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Setter
    private EstadoDocumentoComercial estado = EstadoDocumentoComercial.RASCUNHO;

    @Column(nullable = false)
    @Setter
    private LocalDate dataEmissao;

    @Column(nullable = false)
    @Setter
    private LocalDate dataVencimento;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @Setter
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_morada_envio")
    @Setter
    private Morada moradaEnvio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_armazem_carga", nullable = false)
    @Setter
    private Armazem armazemCarga;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_moeda", nullable = false)
    @Setter
    private Moeda moeda;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_riva", nullable = false)
    @Setter
    private RIva riva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mpagamento")
    @Setter
    private MPagamento mPagamento;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ppagamento", nullable = false)
    @Setter
    private PPagamento pPagamento;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transporte", nullable = false)
    @Setter
    private Transporte transporte;

    @Column(length = 100, nullable = false)
    @Setter
    private String clienteNome;

    @Column(length = 20, nullable = false)
    @Setter
    private String clienteNif;

    @Column(length = 60, nullable = false)
    @Setter
    private String clienteMorada;

    @Column(length = 60)
    @Setter
    private String clienteMorada1;

    @Column(length = 20, nullable = false)
    @Setter
    private String clienteCodPostal;

    @Column(length = 50)
    @Setter
    private String clienteLocalidade;

    @Column(length = 3, nullable = false)
    @Setter
    private String clientePais;

    @Column(length = 80)
    @Setter
    private String envioNome;

    @Column(length = 60)
    @Setter
    private String envioMorada;

    @Column(length = 60)
    @Setter
    private String envioMorada1;

    @Column(length = 20)
    @Setter
    private String envioCodPostal;

    @Column(length = 50)
    @Setter
    private String envioLocalidade;

    @Column(length = 3)
    @Setter
    private String envioPais;

    @Column(nullable = false)
    @Setter
    private LocalDate dataCarga;

    @Setter
    private LocalTime horaCarga;

    @Column(length = 100)
    @Setter
    private String matricula;

    @Column(length = 100, nullable = false)
    @Setter
    private String cargaNome;

    @Column(length = 60, nullable = false)
    @Setter
    private String cargaMorada;

    @Column(length = 60)
    @Setter
    private String cargaMorada1;

    @Column(length = 20, nullable = false)
    @Setter
    private String cargaCodPostal;

    @Column(length = 50, nullable = false)
    @Setter
    private String cargaLocalidade;

    @Column(length = 3, nullable = false)
    @Setter
    private String cargaPais;

    @Setter
    private LocalDate dataDescarga;

    @Setter
    private LocalTime horaDescarga;

    @Column(length = 60)
    @Setter
    private String descargaMorada;

    @Column(length = 60)
    @Setter
    private String descargaMorada1;

    @Column(length = 20)
    @Setter
    private String descargaCodPostal;

    @Column(length = 50)
    @Setter
    private String descargaLocalidade;

    @Column(length = 3)
    @Setter
    private String descargaPais;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorBruto = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIsento = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorSujeitoReduzida = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorSujeitoIntermedia = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorSujeitoNormal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIvaReduzida = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIvaIntermedia = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIvaNormal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorRetencao = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIvaTotal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(precision = 15, scale = 3)
    @Setter
    private BigDecimal peso;

    @Column(length = 250)
    @Setter
    private String observacoes;

    @Setter
    private OffsetDateTime momentoEmissao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emissor")
    @Setter
    private Utilizador emissor;

    @Column(nullable = false)
    @Setter
    private boolean anulado = false;

    @Column(nullable = false)
    @Setter
    private boolean impresso = false;

    @Column(nullable = false)
    @Setter
    private boolean liquidado = false;
}
