package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.DocumentoFinanceiro;
import com.ar2lda.fac.repository.projection.ExtratoAnteriorProjection;
import com.ar2lda.fac.repository.projection.ExtratoMovimentoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DocumentoFinanceiroRepository extends JpaRepository<DocumentoFinanceiro, Long> {

    boolean existsByTipoDocumentoIdAndSerie(String tipoDocumentoId, String serie);

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

    @Query("""
            select d.moeda.id as moedaId,
                   sum(case when d.tipoDocumento.sinalContabilistico = 1 then d.valorPagamentoBruto else 0 end) as debito,
                   sum(case when d.tipoDocumento.sinalContabilistico = 2 then d.valorPagamentoBruto else 0 end) as credito
            from DocumentoFinanceiro d
            where d.cliente.id = :clienteId
              and d.anulado = false
              and d.dataEmissao < :dataInicial
            group by d.moeda.id
            """)
    List<ExtratoAnteriorProjection> findExtratoAnterior(
            @Param("clienteId") Long clienteId,
            @Param("dataInicial") LocalDate dataInicial
    );

    @Query("""
            select d.id as id, d.dataEmissao as data, d.momentoEmissao as momento,
                   d.tipoDocumento.id as tipoDocumentoId, d.serie as serie,
                   d.numeroDocumento as numeroDocumento, d.tipoDocumento.descricao as descricao,
                   null as dataVencimento, d.moeda.id as moedaId,
                   d.tipoDocumento.sinalContabilistico as sinalContabilistico, d.valorPagamentoBruto as valor
            from DocumentoFinanceiro d
            where d.cliente.id = :clienteId
              and d.anulado = false
              and d.dataEmissao >= :dataInicial
              and d.dataEmissao <= :dataFinal
            order by d.dataEmissao, d.momentoEmissao, d.id
            """)
    List<ExtratoMovimentoProjection> findExtratoMovimentos(
            @Param("clienteId") Long clienteId,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal
    );
}
