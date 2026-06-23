package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.AuditoriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Long>, JpaSpecificationExecutor<AuditoriaEvento> {
}
