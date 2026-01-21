package com.ar2lda.fac.repository;

import com.ar2lda.fac.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
