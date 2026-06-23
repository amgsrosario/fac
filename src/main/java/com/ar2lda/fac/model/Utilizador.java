package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
}
