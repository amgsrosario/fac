package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinhaDocumentoFinanceiroRepository extends JpaRepository<LinhaDocumentoFinanceiro, Long> {

    List<LinhaDocumentoFinanceiro> findByDocumentoFinanceiroIdOrderByNumeroLinha(Long documentoFinanceiroId);
}
