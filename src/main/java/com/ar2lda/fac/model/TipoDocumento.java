package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tipodocumento")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class TipoDocumento {

    @Id
    @Column(length = 3, nullable = false)
    @ToString.Include
    private String id;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String descricao;

    @Column(name = "codigo_fiscal", length = 2)
    @Setter
    @ToString.Include
    private String codigoFiscal;

    @Column(length = 25)
    @Setter
    private String modeloEmissao1;

    @Column(length = 25)
    @Setter
    private String modeloEmissao2;

    @Column(length = 25)
    @Setter
    private String modeloEmissao3;

    @Column(length = 25)
    @Setter
    private String modeloEmissao4;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private Integer areaGestao;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private Integer entidade;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private Integer sinalContabilistico;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private boolean liquidacaoImediata;

    public TipoDocumento() {
    }

    public TipoDocumento(String id, String descricao, String modeloEmissao1, String modeloEmissao2,
                         String modeloEmissao3, String modeloEmissao4, Integer areaGestao, Integer entidade,
                         Integer sinalContabilistico, boolean liquidacaoImediata) {
        this(id, descricao, null, modeloEmissao1, modeloEmissao2, modeloEmissao3, modeloEmissao4,
                areaGestao, entidade, sinalContabilistico, liquidacaoImediata);
    }

    public TipoDocumento(String id, String descricao, String codigoFiscal, String modeloEmissao1, String modeloEmissao2,
                         String modeloEmissao3, String modeloEmissao4, Integer areaGestao, Integer entidade,
                         Integer sinalContabilistico, boolean liquidacaoImediata) {
        this.id = id;
        this.descricao = descricao;
        this.codigoFiscal = codigoFiscal;
        this.modeloEmissao1 = modeloEmissao1;
        this.modeloEmissao2 = modeloEmissao2;
        this.modeloEmissao3 = modeloEmissao3;
        this.modeloEmissao4 = modeloEmissao4;
        this.areaGestao = areaGestao;
        this.entidade = entidade;
        this.sinalContabilistico = sinalContabilistico;
        this.liquidacaoImediata = liquidacaoImediata;
    }
}
