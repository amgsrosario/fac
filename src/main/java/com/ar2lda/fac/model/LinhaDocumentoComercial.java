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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(
        name = "linha_documento_comercial",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_documento_comercial", "numero_linha"})
)
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class LinhaDocumentoComercial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento_comercial", nullable = false)
    @Setter
    private DocumentoComercial documentoComercial;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private Integer numeroLinha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artigo", nullable = false)
    @Setter
    private Artigo artigo;

    @Column(length = 80, nullable = false)
    @Setter
    @ToString.Include
    private String descricao;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal quantidade;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal precoUnitario;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorBruto;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Setter
    private TipoDescontoLinha tipoDesconto = TipoDescontoLinha.VALOR;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal desconto;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorDesconto;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorLinha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_taxa_iva", nullable = false)
    @Setter
    private TipoTaxaIva tipoTaxaIva;

    @Column(precision = 5, scale = 2, nullable = false)
    @Setter
    private BigDecimal percentagemIva;

    @Column(precision = 15, scale = 3)
    @Setter
    private BigDecimal peso;

    @Column(name = "artigo_codigo", length = 50)
    private String artigoCodigo;

    @Column(name = "unidade", length = 3)
    private String unidade;

    @Column(name = "tipo_taxa_iva_codigo", length = 20)
    private String tipoTaxaIvaCodigo;

    @Column(name = "tipo_taxa_iva_descricao", length = 50)
    private String tipoTaxaIvaDescricao;

    @Column(name = "base_tributavel", precision = 19, scale = 6)
    private BigDecimal baseTributavel;

    @Column(name = "valor_imposto", precision = 19, scale = 6)
    private BigDecimal valorImposto;

    @Column(name = "total_linha", precision = 19, scale = 6)
    private BigDecimal totalLinha;

    public void consolidarSnapshotFiscal(BigDecimal baseTributavel, BigDecimal valorImposto, BigDecimal totalLinha) {
        if (artigo == null || tipoTaxaIva == null) {
            throw new IllegalStateException("Artigo e tipo de taxa IVA são obrigatórios no snapshot da linha");
        }
        this.artigoCodigo = artigo.getCodigo();
        this.unidade = artigo.getUnidade();
        this.tipoTaxaIvaCodigo = tipoTaxaIva.getId();
        this.tipoTaxaIvaDescricao = tipoTaxaIva.getDescricao();
        this.baseTributavel = baseTributavel;
        this.valorImposto = valorImposto;
        this.totalLinha = totalLinha;
    }

}
