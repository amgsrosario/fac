package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.LinhaDocumentoComercial;
import com.ar2lda.fac.model.TipoLinhaDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LinhaDocumentoComercialRepository extends JpaRepository<LinhaDocumentoComercial, Long> {

    @EntityGraph(attributePaths = {"artigo", "tipoTaxaIva", "documentoComercial"})
    List<LinhaDocumentoComercial> findByDocumentoComercialIdOrderByNumeroLinha(Long documentoId);

    @Query("""
            select l
            from LinhaDocumentoComercial l
            where l.documentoComercial.estado <> com.ar2lda.fac.model.EstadoDocumentoComercial.RASCUNHO
              and l.documentoComercial.dataEmissao >= :dataInicial
              and l.documentoComercial.dataEmissao <= :dataFinal
              and (:filtrarClientes = false or l.documentoComercial.cliente.id in :clienteIds)
              and (:filtrarArtigos = false or l.artigo.codigo in :artigoIds or l.artigoCodigo in :artigoIds)
            """)
    Page<LinhaDocumentoComercial> findAnaliticas(
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            @Param("filtrarClientes") boolean filtrarClientes,
            @Param("clienteIds") Collection<Long> clienteIds,
            @Param("filtrarArtigos") boolean filtrarArtigos,
            @Param("artigoIds") Collection<String> artigoIds,
            Pageable pageable
    );

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
