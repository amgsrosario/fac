package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LinhaDocumentoFinanceiroRepository extends JpaRepository<LinhaDocumentoFinanceiro, Long> {

    List<LinhaDocumentoFinanceiro> findByDocumentoFinanceiroIdOrderByNumeroLinha(Long documentoFinanceiroId);

    @Query("""
            select l
            from LinhaDocumentoFinanceiro l
            where l.documentoFinanceiro.dataEmissao >= :dataInicial
              and l.documentoFinanceiro.dataEmissao <= :dataFinal
              and (:clienteId is null or l.documentoFinanceiro.cliente.id = :clienteId)
              and (
                l.pendente is null
                or l.pendente.documentoComercial is null
                or l.pendente.documentoComercial.estado <> com.ar2lda.fac.model.EstadoDocumentoComercial.RASCUNHO
              )
            """)
    Page<LinhaDocumentoFinanceiro> findAnaliticas(
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            @Param("clienteId") Long clienteId,
            Pageable pageable
    );

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

    @Query("""
            select count(l) > 0
            from LinhaDocumentoFinanceiro l
            where l.pendente.documentoComercial.id = :documentoComercialId
              and l.documentoFinanceiro.anulado = false
            """)
    boolean existsActiveLinesForDocumentoComercial(
            @Param("documentoComercialId") Long documentoComercialId
    );
}
