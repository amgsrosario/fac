package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.DocumentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface DocumentoFinanceiroRepository extends JpaRepository<DocumentoFinanceiro, Long> {

    @Query("""
            select max(d.dataEmissao)
            from DocumentoFinanceiro d
            where d.tipoDocumento.id = :tipoDocumentoId
              and d.serie = :serie
            """)
    LocalDate findUltimaDataEmissao(
            @Param("tipoDocumentoId") String tipoDocumentoId,
            @Param("serie") String serie
    );
}
