package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilizadorRepository extends JpaRepository<Utilizador, String> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndCodigoNot(String email, String codigo);
}
