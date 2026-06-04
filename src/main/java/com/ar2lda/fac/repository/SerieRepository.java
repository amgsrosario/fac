package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Serie;
import com.ar2lda.fac.model.SerieId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, SerieId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from Serie s
            where s.tipoDocumento.id = :tipoDocumentoId
              and s.serie = :serie
            """)
    Optional<Serie> findForUpdate(
            @Param("tipoDocumentoId") String tipoDocumentoId,
            @Param("serie") String serie
    );
}
