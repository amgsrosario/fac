package com.ar2lda.fac.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "parametros_documento_comercial")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class ParametrosDocumentoComercial {

    public static final Long PARAMETROS_ID = 1L;

    @Id
    @ToString.Include
    private Long id = PARAMETROS_ID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento")
    @Setter
    private TipoDocumento tipoDocumento;

    @Column(length = 10)
    @Setter
    private String serie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_armazem_carga")
    @Setter
    private Armazem armazemCarga;
}
