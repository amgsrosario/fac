package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "importacao_dados_mestres")
@Getter
@Setter
public class ImportacaoDadosMestres {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDadosMestres tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoImportacaoDadosMestres estado;

    @Column(nullable = false, length = 255)
    private String nomeFicheiro;

    @Column(nullable = false, length = 10)
    private String formato;

    @Column(nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(nullable = false)
    private int totalLinhas;

    @Column(nullable = false)
    private int linhasValidas;

    @Column(nullable = false)
    private int linhasComErro;

    @Column(nullable = false)
    private int linhasComAviso;

    @Column(nullable = false)
    private int registosNovos;

    @Column(nullable = false)
    private int duplicados;

    @Column(nullable = false)
    private int linhasIgnoradas;

    @Column(nullable = false)
    private OffsetDateTime criadoEm;

    @Column(nullable = false)
    private OffsetDateTime expiraEm;

    private OffsetDateTime confirmadoEm;

    @Column(length = 20)
    private String criadoPor;

    @Column(length = 20)
    private String confirmadoPor;

    @Version
    private Long versao;
}
