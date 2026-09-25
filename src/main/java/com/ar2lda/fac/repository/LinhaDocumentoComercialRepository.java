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

    @EntityGraph(attributePaths = {
            "artigo", "tipoTaxaIva", "documentoComercial", "documentoComercial.cliente",
            "documentoComercial.tipoDocumento", "documentoComercial.moeda", "documentoComercial.riva",
            "documentoComercial.mPagamento", "documentoComercial.pPagamento",
            "documentoComercial.transporte", "documentoComercial.emissor"
    })
    @Query("""
            select l
            from LinhaDocumentoComercial l
            where l.documentoComercial.estado <> com.ar2lda.fac.model.EstadoDocumentoComercial.RASCUNHO
              and l.documentoComercial.anulado = false
              and (:mostrarTexto = true or l.tipoLinha <> com.ar2lda.fac.model.TipoLinhaDocumento.TEXTO)
              and l.documentoComercial.dataEmissao >= :dataInicial
              and l.documentoComercial.dataEmissao <= :dataFinal
              and (:filtrarClientes = false or l.documentoComercial.cliente.id in :clienteIds)
              and (:filtrarArtigos = false or l.artigo.codigo in :artigoIds or l.artigoCodigo in :artigoIds)
              and (:searchEmpty = true
                or lower(l.documentoComercial.tipoDocumento.id) like :search
                or lower(l.documentoComercial.serie) like :search
                or str(l.documentoComercial.numeroDocumento) like :search
                or lower(l.documentoComercial.clienteNome) like :search
                or lower(l.documentoComercial.clienteNif) like :search
                or lower(l.artigo.codigo) like :search
                or lower(l.artigoCodigo) like :search
                or lower(l.descricao) like :search)
            """)
    Page<LinhaDocumentoComercial> findAnaliticas(
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            @Param("mostrarTexto") boolean mostrarTexto,
            @Param("filtrarClientes") boolean filtrarClientes,
            @Param("clienteIds") Collection<Long> clienteIds,
            @Param("filtrarArtigos") boolean filtrarArtigos,
            @Param("artigoIds") Collection<String> artigoIds,
            @Param("search") String search,
            @Param("searchEmpty") boolean searchEmpty,
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
