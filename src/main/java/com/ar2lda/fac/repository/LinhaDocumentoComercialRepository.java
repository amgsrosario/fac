package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.LinhaDocumentoComercial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinhaDocumentoComercialRepository extends JpaRepository<LinhaDocumentoComercial, Long> {

    List<LinhaDocumentoComercial> findByDocumentoComercialIdOrderByNumeroLinha(Long documentoId);

    boolean existsByDocumentoComercialId(Long documentoId);

    @Query("""
            select coalesce(max(l.numeroLinha), 0)
            from LinhaDocumentoComercial l
            where l.documentoComercial.id = :documentoId
            """)
    Integer findMaxNumeroLinha(@Param("documentoId") Long documentoId);
}
