package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LinhaDocumentoFinanceiroRepository extends JpaRepository<LinhaDocumentoFinanceiro, Long> {

    List<LinhaDocumentoFinanceiro> findByDocumentoFinanceiroIdOrderByNumeroLinha(Long documentoFinanceiroId);

    List<LinhaDocumentoFinanceiro> findByPendenteIdInOrderByDocumentoFinanceiroDataEmissaoAscDocumentoFinanceiroNumeroDocumentoAscNumeroLinhaAsc(
            Collection<Long> pendenteIds
    );

    @Query("""
            select count(l) > 0
            from LinhaDocumentoFinanceiro l
            where l.pendente.documentoComercial.id = :documentoComercialId
              and l.documentoFinanceiro.id <> :documentoFinanceiroId
              and l.documentoFinanceiro.anulado = false
            """)
    boolean existsOtherActiveLinesForDocumentoComercial(
            @Param("documentoComercialId") Long documentoComercialId,
            @Param("documentoFinanceiroId") Long documentoFinanceiroId
    );
}
