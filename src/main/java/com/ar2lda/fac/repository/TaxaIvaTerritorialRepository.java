package com.ar2lda.fac.repository;
import com.ar2lda.fac.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
public interface TaxaIvaTerritorialRepository extends JpaRepository<TaxaIvaTerritorial,Long> {
 @Query("select t from TaxaIvaTerritorial t where t.jurisdicao=:jurisdicao and t.territorio=:territorio and t.categoria=:categoria and (t.vigenteDesde is null or t.vigenteDesde<=:data) and (t.vigenteAte is null or t.vigenteAte>=:data)")
 List<TaxaIvaTerritorial> aplicaveis(@Param("jurisdicao") String jurisdicao,@Param("territorio") TerritorioFiscal territorio,@Param("categoria") String categoria,@Param("data") LocalDate data);
}
