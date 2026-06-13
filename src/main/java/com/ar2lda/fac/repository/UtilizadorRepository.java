package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilizadorRepository extends JpaRepository<Utilizador, String> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndCodigoNot(String email, String codigo);

    Optional<Utilizador> findByCodigoIgnoreCaseOrEmailIgnoreCase(String codigo, String email);
}
