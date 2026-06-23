package com.ar2lda.fac.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "auditoria_evento")
@Getter
public class AuditoriaEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private OffsetDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 50, nullable = false, updatable = false)
    private TipoAuditoriaEvento tipoEvento;

    @Column(name = "entidade_tipo", length = 50, nullable = false, updatable = false)
    private String entidadeTipo;

    @Column(name = "entidade_id", length = 100, nullable = false, updatable = false)
    private String entidadeId;

    @Column(name = "utilizador_id", length = 20, updatable = false)
    private String utilizadorId;

    @Column(name = "utilizador_nome", length = 100, updatable = false)
    private String utilizadorNome;

    @Column(name = "utilizador_perfil", length = 20, updatable = false)
    private String utilizadorPerfil;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, updatable = false)
    private ResultadoAuditoria resultado;

    @Column(length = 100, updatable = false)
    private String referencia;

    @Column(length = 500, nullable = false, updatable = false)
    private String descricao;

    @Column(name = "dados_essenciais", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String dadosEssenciais;

    protected AuditoriaEvento() {}

    public AuditoriaEvento(OffsetDateTime dataHora, TipoAuditoriaEvento tipoEvento, String entidadeTipo, String entidadeId,
                           String utilizadorId, String utilizadorNome, String utilizadorPerfil,
                           ResultadoAuditoria resultado, String referencia, String descricao, String dadosEssenciais) {
        this.dataHora = dataHora;
        this.tipoEvento = tipoEvento;
        this.entidadeTipo = entidadeTipo;
        this.entidadeId = entidadeId;
        this.utilizadorId = utilizadorId;
        this.utilizadorNome = utilizadorNome;
        this.utilizadorPerfil = utilizadorPerfil;
        this.resultado = resultado;
        this.referencia = referencia;
        this.descricao = descricao;
        this.dadosEssenciais = dadosEssenciais;
    }
}
