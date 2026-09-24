package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.controller.dto.ContaCorrentePendenteResumoMoedaDto;
import com.ar2lda.fac.controller.dto.PendenteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PendenteRepository extends JpaRepository<Pendente, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pendente p where p.id = :id")
    Optional<Pendente> findByIdForUpdate(@Param("id") Long id);

    Optional<Pendente> findByDocumentoComercialId(Long documentoComercialId);

    List<Pendente> findByClienteIdOrderByDataDocumentoAscNumeroDocumentoAsc(Long clienteId);

    @Query(value = """
            select new com.ar2lda.fac.controller.dto.PendenteDto(
                p.id, d.id, c.id, t.id, p.numeroDocumento, p.serieDocumento,
                p.valorDocumento, p.valorPendente, p.dataDocumento, p.dataVencimento, m.id)
            from Pendente p
            join p.documentoComercial d
            join p.cliente c
            join p.tipoDocumento t
            join p.moeda m
            where (:clienteId is null or c.id = :clienteId)
              and (:excluirLiquidados = false or p.valorPendente > 0)
              and (:vencimento = 'all'
                or (:vencimento = 'overdue' and p.valorPendente > 0 and p.dataVencimento < :hoje)
                or (:vencimento = 'not-overdue' and not (p.valorPendente > 0 and p.dataVencimento < :hoje)))
              and (:searchEmpty = true
                or lower(concat(t.id, concat(' ', concat(p.serieDocumento, concat('/', str(p.numeroDocumento)))))) like concat('%', :search, '%')
                or str(c.id) like concat('%', :search, '%')
                or str(p.id) like concat('%', :search, '%')
                or (:searchLiquidado = true and p.valorPendente <= 0)
                or (:searchVencido = true and p.valorPendente > 0 and p.dataVencimento < :hoje)
                or (:searchParcial = true and p.valorPendente > 0 and p.valorPendente < p.valorDocumento and p.dataVencimento >= :hoje)
                or (:searchAberto = true and p.valorPendente = p.valorDocumento and p.dataVencimento >= :hoje))
            """, countQuery = """
            select count(p.id)
            from Pendente p
            join p.cliente c
            join p.tipoDocumento t
            where (:clienteId is null or c.id = :clienteId)
              and (:excluirLiquidados = false or p.valorPendente > 0)
              and (:vencimento = 'all'
                or (:vencimento = 'overdue' and p.valorPendente > 0 and p.dataVencimento < :hoje)
                or (:vencimento = 'not-overdue' and not (p.valorPendente > 0 and p.dataVencimento < :hoje)))
              and (:searchEmpty = true
                or lower(concat(t.id, concat(' ', concat(p.serieDocumento, concat('/', str(p.numeroDocumento)))))) like concat('%', :search, '%')
                or str(c.id) like concat('%', :search, '%')
                or str(p.id) like concat('%', :search, '%')
                or (:searchLiquidado = true and p.valorPendente <= 0)
                or (:searchVencido = true and p.valorPendente > 0 and p.dataVencimento < :hoje)
                or (:searchParcial = true and p.valorPendente > 0 and p.valorPendente < p.valorDocumento and p.dataVencimento >= :hoje)
                or (:searchAberto = true and p.valorPendente = p.valorDocumento and p.dataVencimento >= :hoje))
            """)
    Page<PendenteDto> findContaCorrente(
            @Param("clienteId") Long clienteId,
            @Param("search") String search,
            @Param("searchEmpty") boolean searchEmpty,
            @Param("searchLiquidado") boolean searchLiquidado,
            @Param("searchVencido") boolean searchVencido,
            @Param("searchParcial") boolean searchParcial,
            @Param("searchAberto") boolean searchAberto,
            @Param("vencimento") String vencimento,
            @Param("excluirLiquidados") boolean excluirLiquidados,
            @Param("hoje") LocalDate hoje,
            Pageable pageable
    );

    @Query("""
            select new com.ar2lda.fac.controller.dto.ContaCorrentePendenteResumoMoedaDto(
                m.id, count(p.id), sum(case when p.valorPendente > 0 then 1 else 0 end),
                sum(p.valorDocumento), sum(p.valorPendente))
            from Pendente p
            join p.cliente c
            join p.tipoDocumento t
            join p.moeda m
            where (:clienteId is null or c.id = :clienteId)
              and (:excluirLiquidados = false or p.valorPendente > 0)
              and (:vencimento = 'all'
                or (:vencimento = 'overdue' and p.valorPendente > 0 and p.dataVencimento < :hoje)
                or (:vencimento = 'not-overdue' and not (p.valorPendente > 0 and p.dataVencimento < :hoje)))
              and (:searchEmpty = true
                or lower(concat(t.id, concat(' ', concat(p.serieDocumento, concat('/', str(p.numeroDocumento)))))) like concat('%', :search, '%')
                or str(c.id) like concat('%', :search, '%')
                or str(p.id) like concat('%', :search, '%')
                or (:searchLiquidado = true and p.valorPendente <= 0)
                or (:searchVencido = true and p.valorPendente > 0 and p.dataVencimento < :hoje)
                or (:searchParcial = true and p.valorPendente > 0 and p.valorPendente < p.valorDocumento and p.dataVencimento >= :hoje)
                or (:searchAberto = true and p.valorPendente = p.valorDocumento and p.dataVencimento >= :hoje))
            group by m.id
            order by m.id
            """)
    List<ContaCorrentePendenteResumoMoedaDto> summarizeContaCorrente(
            @Param("clienteId") Long clienteId,
            @Param("search") String search,
            @Param("searchEmpty") boolean searchEmpty,
            @Param("searchLiquidado") boolean searchLiquidado,
            @Param("searchVencido") boolean searchVencido,
            @Param("searchParcial") boolean searchParcial,
            @Param("searchAberto") boolean searchAberto,
            @Param("vencimento") String vencimento,
            @Param("excluirLiquidados") boolean excluirLiquidados,
            @Param("hoje") LocalDate hoje
    );

    @Query("""
            select p
            from Pendente p
            join fetch p.documentoComercial d
            join fetch p.cliente c
            join fetch p.tipoDocumento t
            join fetch p.moeda m
            where c.id = :clienteId
              and p.valorPendente > 0
              and (:moedaId is null or m.id = :moedaId)
            order by p.dataVencimento asc nulls last,
                     p.dataDocumento asc nulls last,
                     p.numeroDocumento asc,
                     p.id asc
            """)
    List<Pendente> findAbertosPorCliente(
            @Param("clienteId") Long clienteId,
            @Param("moedaId") String moedaId
    );

    @Query("""
            select p
            from Pendente p
            join fetch p.documentoComercial d
            join fetch p.cliente c
            join fetch p.tipoDocumento t
            join fetch p.moeda m
            where d.estado = com.ar2lda.fac.model.EstadoDocumentoComercial.EMITIDO
              and d.anulado = false
              and d.numeroDocumento is not null
              and p.dataDocumento <= :dataLimite
              and p.valorPendente > 0
              and (:apenasVencidos = false or p.dataVencimento <= :dataLimite)
              and (:filtrarClientes = false or c.id in :clienteIds)
            order by p.dataDocumento asc, t.id asc, p.serieDocumento asc, p.numeroDocumento asc
            """)
    List<Pendente> findPendentesListagem(
            @Param("dataLimite") LocalDate dataLimite,
            @Param("apenasVencidos") boolean apenasVencidos,
            @Param("filtrarClientes") boolean filtrarClientes,
            @Param("clienteIds") Collection<Long> clienteIds
    );

    @Query("""
            select p
            from Pendente p
            join fetch p.documentoComercial d
            join fetch p.cliente c
            join fetch p.tipoDocumento t
            join fetch p.moeda m
            where d.estado in (
                com.ar2lda.fac.model.EstadoDocumentoComercial.EMITIDO,
                com.ar2lda.fac.model.EstadoDocumentoComercial.ANULADO
              )
              and d.numeroDocumento is not null
              and p.dataDocumento <= :dataReferencia
              and (:apenasVencidos = false or p.dataVencimento <= :dataReferencia)
              and (d.anulado = false or d.dataHoraAnulacao is null or d.dataHoraAnulacao > :fimDataReferencia)
              and (:filtrarClientes = false or c.id in :clienteIds)
            order by p.dataVencimento asc, p.dataDocumento asc, t.id asc, p.serieDocumento asc, p.numeroDocumento asc
            """)
    List<Pendente> findPendentesADataListagem(
            @Param("dataReferencia") LocalDate dataReferencia,
            @Param("fimDataReferencia") OffsetDateTime fimDataReferencia,
            @Param("apenasVencidos") boolean apenasVencidos,
            @Param("filtrarClientes") boolean filtrarClientes,
            @Param("clienteIds") Collection<Long> clienteIds
    );
}
