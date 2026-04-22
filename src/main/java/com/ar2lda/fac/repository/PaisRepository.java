package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Pais;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaisRepository extends JpaRepository<Pais, String> {
}
