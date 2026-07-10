package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.LinhaDocumentoComercial;
import com.ar2lda.fac.model.TipoLinhaDocumento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinhaDocumentoComercialRepository extends JpaRepository<LinhaDocumentoComercial, Long> {

    @EntityGraph(attributePaths = {"artigo", "tipoTaxaIva", "documentoComercial"})
    List<LinhaDocumentoComercial> findByDocumentoComercialIdOrderByNumeroLinha(Long documentoId);

    boolean existsByDocumentoComercialId(Long documentoId);

    boolean existsByDocumentoComercialIdAndTipoLinha(Long documentoId, TipoLinhaDocumento tipoLinha);

    void deleteByDocumentoComercialId(Long documentoId);

    @Query("""
            select coalesce(max(l.numeroLinha), 0)
            from LinhaDocumentoComercial l
            where l.documentoComercial.id = :documentoId
            """)
    Integer findMaxNumeroLinha(@Param("documentoId") Long documentoId);
}
