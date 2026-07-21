package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Pendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PendenteRepository extends JpaRepository<Pendente, Long> {

    Optional<Pendente> findByDocumentoComercialId(Long documentoComercialId);

    List<Pendente> findByClienteIdOrderByDataDocumentoAscNumeroDocumentoAsc(Long clienteId);

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
              and (:filtrarClientes = false or c.id in :clienteIds)
            order by p.dataDocumento asc, t.id asc, p.serieDocumento asc, p.numeroDocumento asc
            """)
    List<Pendente> findPendentesListagem(
            @Param("dataLimite") LocalDate dataLimite,
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
              and (d.anulado = false or d.dataHoraAnulacao is null or d.dataHoraAnulacao > :fimDataReferencia)
              and (:filtrarClientes = false or c.id in :clienteIds)
            order by p.dataVencimento asc, p.dataDocumento asc, t.id asc, p.serieDocumento asc, p.numeroDocumento asc
            """)
    List<Pendente> findPendentesADataListagem(
            @Param("dataReferencia") LocalDate dataReferencia,
            @Param("fimDataReferencia") OffsetDateTime fimDataReferencia,
            @Param("filtrarClientes") boolean filtrarClientes,
            @Param("clienteIds") Collection<Long> clienteIds
    );
}
