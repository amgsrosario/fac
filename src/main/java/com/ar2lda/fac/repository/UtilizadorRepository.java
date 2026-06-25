package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import com.ar2lda.fac.model.PapelUtilizador;
import java.util.List;

import java.util.Optional;

public interface UtilizadorRepository extends JpaRepository<Utilizador, String>, JpaSpecificationExecutor<Utilizador> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndCodigoNot(String email, String codigo);

    Optional<Utilizador> findByCodigoIgnoreCaseOrEmailIgnoreCase(String codigo, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Utilizador u where u.papel = :papel order by u.codigo")
    List<Utilizador> findByPapelForUpdate(PapelUtilizador papel);
}
