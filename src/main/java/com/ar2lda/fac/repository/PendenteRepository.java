package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Pendente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PendenteRepository extends JpaRepository<Pendente, Long> {

    Optional<Pendente> findByDocumentoComercialId(Long documentoComercialId);

    List<Pendente> findByClienteIdOrderByDataDocumentoAscNumeroDocumentoAsc(Long clienteId);
}
