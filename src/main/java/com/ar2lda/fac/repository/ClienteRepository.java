package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.controller.dto.ClienteComPendentesResumoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @Query("""
            select c from Cliente c
            where (:inativo is null or c.inativo = :inativo)
              and (:searchEmpty = true
                or str(c.id) like concat('%', :search, '%')
                or lower(c.nome) like concat('%', :search, '%')
                or lower(c.nif) like concat('%', :search, '%')
                or lower(coalesce(c.email, '')) like concat('%', :search, '%')
                or lower(coalesce(c.email1, '')) like concat('%', :search, '%')
                or lower(coalesce(c.tel, '')) like concat('%', :search, '%')
                or lower(coalesce(c.tm, '')) like concat('%', :search, '%')
                or lower(coalesce(c.localidade, '')) like concat('%', :search, '%'))
            """)
    Page<Cliente> findAllBySearch(@Param("search") String search, @Param("searchEmpty") boolean searchEmpty,
                                  @Param("inativo") Boolean inativo, Pageable pageable);

    @Query(value = """
            select new com.ar2lda.fac.controller.dto.ClienteComPendentesResumoDto(
                c.id, c.id, c.nome, c.nif, c.moeda.id, mp.id, count(p.id))
            from Cliente c
            join Pendente p on p.cliente = c
            left join c.mPagamento mp
            where c.inativo = false
              and p.valorPendente > 0
              and (:searchEmpty = true
                or str(c.id) like concat('%', :search, '%')
                or lower(c.nome) like concat('%', :search, '%')
                or lower(c.nif) like concat('%', :search, '%')
                or lower(coalesce(c.email, '')) like concat('%', :search, '%')
                or lower(coalesce(c.localidade, '')) like concat('%', :search, '%'))
            group by c.id, c.nome, c.nif, c.moeda.id, mp.id
            """, countQuery = """
            select count(distinct c.id)
            from Cliente c
            join Pendente p on p.cliente = c
            where c.inativo = false
              and p.valorPendente > 0
              and (:searchEmpty = true
                or str(c.id) like concat('%', :search, '%')
                or lower(c.nome) like concat('%', :search, '%')
                or lower(c.nif) like concat('%', :search, '%')
                or lower(coalesce(c.email, '')) like concat('%', :search, '%')
                or lower(coalesce(c.localidade, '')) like concat('%', :search, '%'))
            """)
    Page<ClienteComPendentesResumoDto> findComPendentesAbertos(
            @Param("search") String search,
            @Param("searchEmpty") boolean searchEmpty,
            Pageable pageable
    );

    boolean existsByNif(String nif);
    boolean existsByNifAndIdNot(String nif, Long id);
}
