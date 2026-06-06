package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Artigo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtigoRepository extends JpaRepository<Artigo, String> {

    boolean existsByCodigoIdentificacao(String codigoIdentificacao);

    boolean existsByCodigoIdentificacaoAndCodigoNot(String codigoIdentificacao, String codigo);
}
