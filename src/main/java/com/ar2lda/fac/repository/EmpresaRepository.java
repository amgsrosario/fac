package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
