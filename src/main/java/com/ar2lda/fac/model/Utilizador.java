package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Entity
@Table(name = "utilizador")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Utilizador {

    @Id
    @Column(length = 20, nullable = false)
    @ToString.Include
    private String codigo;

    @Column(length = 100, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(length = 100, nullable = false, unique = true)
    @Setter
    @ToString.Include
    private String email;

    @Column(name = "password_hash", length = 60, nullable = false)
    @Setter
    private String passwordHash;

    @Column(nullable = false)
    @Setter
    @ToString.Include
    private boolean inativo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Setter
    private PapelUtilizador papel = PapelUtilizador.ADMINISTRADOR;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @Column(name = "ultimo_login_em")
    private OffsetDateTime ultimoLoginEm;

    @Column(name = "criado_por", length = 20, updatable = false)
    private String criadoPor;

    @Column(name = "atualizado_por", length = 20)
    private String atualizadoPor;

    public Utilizador() {
    }

    public Utilizador(String codigo, String nome, String email, String passwordHash, boolean inativo) {
        this.codigo = codigo;
        this.nome = nome;
        this.email = email;
        this.passwordHash = passwordHash;
        this.inativo = inativo;
        this.papel = PapelUtilizador.ADMINISTRADOR;
    }

    public void marcarCriacao(OffsetDateTime instante, String responsavel) {
        if (criadoEm == null) {
            criadoEm = instante;
            criadoPor = responsavel;
        }
        atualizadoEm = instante;
        atualizadoPor = responsavel;
    }

    public void marcarAtualizacao(OffsetDateTime instante, String responsavel) {
        atualizadoEm = instante;
        atualizadoPor = responsavel;
    }

    public void registarLogin(OffsetDateTime instante) {
        ultimoLoginEm = instante;
    }

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (criadoEm == null) criadoEm = now;
        if (atualizadoEm == null) atualizadoEm = now;
    }

    @PreUpdate
    void preUpdate() {
        if (atualizadoEm == null) atualizadoEm = OffsetDateTime.now();
    }
}
