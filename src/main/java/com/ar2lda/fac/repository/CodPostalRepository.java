package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.CodPostal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodPostalRepository extends JpaRepository<CodPostal, String> {
    @Query("""
            select c from CodPostal c
            where c.id like concat(:search, '%')
               or lower(c.nome) like concat('%', :search, '%')
            """)
    Page<CodPostal> findAllBySearch(@Param("search") String search, Pageable pageable);
}
