package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.ImportacaoDadosMestres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface ImportacaoDadosMestresRepository extends JpaRepository<ImportacaoDadosMestres, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ImportacaoDadosMestres> findWithLockById(UUID id);
}
