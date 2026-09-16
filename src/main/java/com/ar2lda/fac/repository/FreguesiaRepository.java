package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Freguesia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FreguesiaRepository extends JpaRepository<Freguesia, String> {
    @Query("""
            select f from Freguesia f
            where f.codigo like concat(:search, '%')
               or translate(lower(f.nome), 'áàâãéêíóôõúüç', 'aaaaeeiooouuc') like concat('%', :search, '%')
               or translate(lower(f.concelho), 'áàâãéêíóôõúüç', 'aaaaeeiooouuc') like concat('%', :search, '%')
            """)
    Page<Freguesia> findAllBySearch(@Param("search") String search, Pageable pageable);
}
