package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "serie")
@IdClass(SerieId.class)
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Serie {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    @Setter
    @ToString.Include
    private TipoDocumento tipoDocumento;

    @Id
    @Column(length = 10, nullable = false)
    @ToString.Include
    private String serie;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(name = "codigo_at", length = 100)
    @Setter
    private String codigoAt;

    @Column(name = "data_codigo_at")
    @Setter
    private LocalDate dataCodigoAt;

    @Column(nullable = false)
    @ToString.Include
    private Long numerador = 0L;

    public Serie() {
    }

    public Serie(TipoDocumento tipoDocumento, String serie, String nome, String codigoAt, LocalDate dataCodigoAt) {
        this.tipoDocumento = tipoDocumento;
        this.serie = serie;
        this.nome = nome;
        this.codigoAt = codigoAt;
        this.dataCodigoAt = dataCodigoAt;
    }

    public Long proximoNumero() {
        numerador++;
        return numerador;
    }
}
