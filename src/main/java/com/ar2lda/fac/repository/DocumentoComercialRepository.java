package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.controller.dto.DocumentoComercialResumoDto;
import com.ar2lda.fac.repository.projection.ExtratoAnteriorProjection;
import com.ar2lda.fac.repository.projection.ExtratoMovimentoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface DocumentoComercialRepository extends JpaRepository<DocumentoComercial, Long> {

    @Query("""
            select new com.ar2lda.fac.controller.dto.DocumentoComercialResumoDto(
                d.id, coalesce(d.tipoDocumentoCodigo, td.id), coalesce(d.tipoDocumentoDescricao, td.descricao),
                d.serie, d.serieDescricao, d.numeroDocumento, d.numeroDocumentoCompleto, d.atcud,
                d.estado, d.dataEmissao, d.dataVencimento, c.id, d.clienteNome, d.clienteNif,
                coalesce(d.moedaCodigo, m.id), d.moedaCodigo, d.moedaSimbolo, d.moedaCasasDecimais,
                d.valorBruto, d.valorDesconto, d.valorIvaTotal, d.valorRetencao, d.valorTotal,
                d.anulado, d.motivoAnulacao, d.impresso, d.liquidado)
            from DocumentoComercial d
            join d.tipoDocumento td
            join d.cliente c
            left join d.moeda m
            where (:searchEmpty = true or
                   lower(coalesce(d.numeroDocumentoCompleto, '')) like :search or
                   lower(coalesce(d.serie, '')) like :search or
                   lower(coalesce(d.clienteNome, '')) like :search or
                   lower(coalesce(d.clienteNif, '')) like :search or
                   lower(coalesce(d.tipoDocumentoDescricao, td.descricao, '')) like :search or
                   lower(cast(d.estado as string)) like :search)
              and (:estado is null or d.estado = :estado)
              and (:dataEmissaoEmpty = true or d.dataEmissao = :dataEmissao)
              and (:documentoEmpty = true or lower(coalesce(d.numeroDocumentoCompleto, '')) like :documento)
              and (:clienteEmpty = true or lower(coalesce(d.clienteNome, '')) like :cliente)
            """)
    Page<DocumentoComercialResumoDto> findResumos(
            @Param("search") String search,
            @Param("searchEmpty") boolean searchEmpty,
            @Param("estado") EstadoDocumentoComercial estado,
            @Param("dataEmissao") LocalDate dataEmissao,
            @Param("dataEmissaoEmpty") boolean dataEmissaoEmpty,
            @Param("documento") String documento,
            @Param("documentoEmpty") boolean documentoEmpty,
            @Param("cliente") String cliente,
            @Param("clienteEmpty") boolean clienteEmpty,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DocumentoComercial d where d.id = :id")
    java.util.Optional<DocumentoComercial> findByIdForUpdate(@Param("id") Long id);

    boolean existsByTipoDocumentoIdAndSerie(String tipoDocumentoId, String serie);

    @Query("""
            select d
            from DocumentoComercial d
            where d.estado <> com.ar2lda.fac.model.EstadoDocumentoComercial.RASCUNHO
              and d.dataEmissao >= :dataInicial
              and d.dataEmissao <= :dataFinal
              and (:mostrarAnulados = true or d.anulado = false)
              and (:filtrarClientes = false or d.cliente.id in :clienteIds)
            """)
    Page<DocumentoComercial> findAnaliticos(
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            @Param("mostrarAnulados") boolean mostrarAnulados,
            @Param("filtrarClientes") boolean filtrarClientes,
            @Param("clienteIds") Collection<Long> clienteIds,
            Pageable pageable
    );

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
