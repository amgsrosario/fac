package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "empresa")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Empresa {

    public static final Long EMPRESA_ID = 1L;

    @Id
    @Column(nullable = false)
    @ToString.Include
    private Long id = EMPRESA_ID;

    @Column(length = 100, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(length = 20, nullable = false)
    @Setter
    @ToString.Include
    private String nif;

    @Column(length = 60, nullable = false)
    @Setter
    @ToString.Include
    private String morada;

    @Column(length = 60)
    @Setter
    @ToString.Include
    private String morada1;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_codpostal", nullable = false)
    @Setter
    private CodPostal codPostal;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String localidade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pais", nullable = false)
    @Setter
    private Pais pais;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_freguesia")
    @Setter
    private Freguesia freguesia;

    @Column(precision = 19, scale = 2, nullable = false)
    @Setter
    private BigDecimal capitalSocial;

    @Column(length = 100, nullable = false)
    @Setter
    private String matriculaRegistoComercial;

    @Column(length = 10, nullable = false)
    @Setter
    private String cae;

    @Column(length = 100, nullable = false)
    @Setter
    private String descricaoCae;

    @Column(length = 120, nullable = false)
    @Setter
    private String email;

    @Column(length = 120)
    @Setter
    private String web;

    @Column(name = "nome_comercial", length = 100)
    @Setter
    private String nomeComercial;

    @Column(length = 30)
    @Setter
    private String telefone;

    @Column(length = 34)
    @Setter
    private String iban;

    @Column(name = "bic_swift", length = 11)
    @Setter
    private String bicSwift;

    @Column(name = "observacoes_legais", length = 1000)
    @Setter
    private String observacoesLegais;

    @Column(name = "texto_rodape", length = 500)
    @Setter
    private String textoRodape;

    @Column(name = "observacoes_comerciais_default", length = 1000)
    @Setter
    private String observacoesComerciaisDefault;

    @Column(columnDefinition = "bytea")
    private byte[] logo;

    @Column(name = "logo_media_type", length = 20)
    private String logoMediaType;

    @Column(name = "logo_nome_interno", length = 80)
    private String logoNomeInterno;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @Column(name = "atualizado_por", length = 20)
    private String atualizadoPor;

    public Empresa() {
    }

    public Empresa(String nome, String nif, String morada, String morada1, String localidade,
                   BigDecimal capitalSocial, String matriculaRegistoComercial, String cae,
                   String descricaoCae, String email, String web) {
        this.nome = nome;
        this.nif = nif;
        this.morada = morada;
        this.morada1 = morada1;
        this.localidade = localidade;
        this.capitalSocial = capitalSocial;
        this.matriculaRegistoComercial = matriculaRegistoComercial;
        this.cae = cae;
        this.descricaoCae = descricaoCae;
        this.email = email;
        this.web = web;
    }

    public void definirLogo(byte[] conteudo, String mediaType, String nomeInterno) {
        this.logo = conteudo == null ? null : conteudo.clone();
        this.logoMediaType = mediaType;
        this.logoNomeInterno = nomeInterno;
    }

    public byte[] getLogo() {
        return logo == null ? null : logo.clone();
    }

    public void removerLogo() {
        logo = null;
        logoMediaType = null;
        logoNomeInterno = null;
    }

    public void marcarAtualizacao(OffsetDateTime instante, String responsavel) {
        atualizadoEm = instante;
        atualizadoPor = responsavel;
    }

    @PrePersist
    void prePersist() {
        if (atualizadoEm == null) atualizadoEm = OffsetDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        if (atualizadoEm == null) atualizadoEm = OffsetDateTime.now();
    }
}
