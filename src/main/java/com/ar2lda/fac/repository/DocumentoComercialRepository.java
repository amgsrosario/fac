package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.DocumentoComercial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface DocumentoComercialRepository extends JpaRepository<DocumentoComercial, Long> {

    @Query("""
            select max(d.dataEmissao)
            from DocumentoComercial d
            where d.tipoDocumento.id = :tipoDocumentoId
              and d.serie = :serie
              and d.estado = com.ar2lda.fac.model.EstadoDocumentoComercial.EMITIDO
            """)
    LocalDate findUltimaDataEmissao(
            @Param("tipoDocumentoId") String tipoDocumentoId,
            @Param("serie") String serie
    );
}
