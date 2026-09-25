package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.controller.dto.ArtigoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArtigoRepository extends JpaRepository<Artigo, String> {

    @Query("""
            select new com.ar2lda.fac.controller.dto.ArtigoDto(
                a.codigo, a.abreviatura, a.codigoIdentificacao, a.descricao, a.tipoArtigo, a.unidade,
                f.id, a.peso, ic.id, iv.id, a.pvp, a.inativo, a.retencao, a.observacoes)
            from Artigo a
            join a.familia f
            join a.ivaCompra ic
            join a.ivaVenda iv
            where (:searchEmpty = true
                   or lower(a.codigo) like :search
                   or lower(a.descricao) like :search
                   or lower(coalesce(a.abreviatura, '')) like :search
                   or lower(coalesce(a.codigoIdentificacao, '')) like :search)
              and (:inativo is null or a.inativo = :inativo)
              and (:codigoEmpty = true or lower(a.codigo) like :codigo)
              and (:descricaoEmpty = true or lower(a.descricao) like :descricao)
              and (:unidade is null or a.unidade = :unidade)
              and (:ivaVendaId is null or iv.id = :ivaVendaId)
            """)
    Page<ArtigoDto> findResumos(
            @Param("search") String search,
            @Param("searchEmpty") boolean searchEmpty,
            @Param("inativo") Boolean inativo,
            @Param("codigo") String codigo,
            @Param("codigoEmpty") boolean codigoEmpty,
            @Param("descricao") String descricao,
            @Param("descricaoEmpty") boolean descricaoEmpty,
            @Param("unidade") String unidade,
            @Param("ivaVendaId") String ivaVendaId,
            Pageable pageable);

    boolean existsByCodigoIdentificacao(String codigoIdentificacao);

    boolean existsByCodigoIdentificacaoAndCodigoNot(String codigoIdentificacao, String codigo);
}
