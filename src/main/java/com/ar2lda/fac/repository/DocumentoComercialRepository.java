package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.repository.projection.ExtratoAnteriorProjection;
import com.ar2lda.fac.repository.projection.ExtratoMovimentoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;

public interface DocumentoComercialRepository extends JpaRepository<DocumentoComercial, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DocumentoComercial d where d.id = :id")
    java.util.Optional<DocumentoComercial> findByIdForUpdate(@Param("id") Long id);

    boolean existsByTipoDocumentoIdAndSerie(String tipoDocumentoId, String serie);

    @Query("""
            select max(d.dataEmissao)
            from DocumentoComercial d
            where d.tipoDocumento.id = :tipoDocumentoId
              and d.serie = :serie
              and d.estado <> com.ar2lda.fac.model.EstadoDocumentoComercial.RASCUNHO
            """)
    LocalDate findUltimaDataEmissao(
            @Param("tipoDocumentoId") String tipoDocumentoId,
            @Param("serie") String serie
    );

    @Query("""
            select d.moeda.id as moedaId,
                   sum(case when d.tipoDocumento.sinalContabilistico = 1 then d.valorTotal else 0 end) as debito,
                   sum(case
                        when d.tipoDocumento.sinalContabilistico = 2 then d.valorTotal
                        when d.tipoDocumento.sinalContabilistico = 1 and d.tipoDocumento.liquidacaoImediata = true then d.valorTotal
                        else 0
                   end) as credito
            from DocumentoComercial d
            where d.cliente.id = :clienteId
              and d.estado = com.ar2lda.fac.model.EstadoDocumentoComercial.EMITIDO
              and d.numeroDocumento is not null
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
                   d.dataVencimento as dataVencimento, d.moeda.id as moedaId,
                   d.tipoDocumento.sinalContabilistico as sinalContabilistico,
                   d.tipoDocumento.liquidacaoImediata as liquidacaoImediata,
                   d.valorTotal as valor
            from DocumentoComercial d
            where d.cliente.id = :clienteId
              and d.estado = com.ar2lda.fac.model.EstadoDocumentoComercial.EMITIDO
              and d.numeroDocumento is not null
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
