package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.CodPostal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodPostalRepository extends JpaRepository<CodPostal, String> {

    @Query(value = """
            select c from CodPostal c
            where c.id = :query
               or c.id like concat(:query, '%')
               or lower(c.nome) like lower(concat(:query, '%'))
               or exists (select l.id from CodPostalLocalidade l where l.codPostal = c and lower(l.nome) like lower(concat(:query, '%')))
            order by case
                when c.id = :query then 0
                when c.id like concat(:query, '%') then 1
                when lower(c.nome) like lower(concat(:query, '%')) then 2
                else 3
            end, c.id
            """,
            countQuery = """
            select count(c) from CodPostal c
            where c.id = :query
               or c.id like concat(:query, '%')
               or lower(c.nome) like lower(concat(:query, '%'))
               or exists (select l.id from CodPostalLocalidade l where l.codPostal = c and lower(l.nome) like lower(concat(:query, '%')))
            """)
    Page<CodPostal> search(String query, Pageable pageable);
}
