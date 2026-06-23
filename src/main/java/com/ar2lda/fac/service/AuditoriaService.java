package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.AuditoriaEventoDto;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.AuditoriaEventoRepository;
import com.ar2lda.fac.repository.UtilizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private final AuditoriaEventoRepository repository;
    private final UtilizadorRepository utilizadorRepository;

    @Transactional
    public void registar(TipoAuditoriaEvento tipo, String entidadeTipo, Object entidadeId,
                         String descricao, String dadosEssenciais) {
        String codigo = currentUserCode();
        String nome = codigo == null ? "SISTEMA" : utilizadorRepository.findById(codigo)
                .map(Utilizador::getNome).orElse(codigo);
        repository.save(new AuditoriaEvento(tipo, entidadeTipo, String.valueOf(entidadeId), codigo, nome,
                descricao, dadosEssenciais == null ? "{}" : dadosEssenciais));
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaEventoDto> consultar(OffsetDateTime desde, OffsetDateTime ate, TipoAuditoriaEvento tipo,
            String entidadeTipo, String entidadeId, String utilizadorId, ResultadoAuditoria resultado, Pageable pageable) {
        Specification<AuditoriaEvento> spec = Specification.where(null);
        if (desde != null) spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("dataHora"), desde));
        if (ate != null) spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("dataHora"), ate));
        if (tipo != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("tipoEvento"), tipo));
        if (entidadeTipo != null && !entidadeTipo.isBlank()) spec = spec.and((r, q, cb) -> cb.equal(r.get("entidadeTipo"), entidadeTipo));
        if (entidadeId != null && !entidadeId.isBlank()) spec = spec.and((r, q, cb) -> cb.equal(r.get("entidadeId"), entidadeId));
        if (utilizadorId != null && !utilizadorId.isBlank()) spec = spec.and((r, q, cb) -> cb.equal(r.get("utilizadorId"), utilizadorId));
        if (resultado != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("resultado"), resultado));
        return repository.findAll(spec, pageable).map(this::toDto);
    }

    private String currentUserCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()) ? auth.getName() : null;
    }

    private AuditoriaEventoDto toDto(AuditoriaEvento e) {
        return new AuditoriaEventoDto(e.getId(), e.getDataHora(), e.getTipoEvento(), e.getEntidadeTipo(), e.getEntidadeId(),
                e.getUtilizadorId(), e.getUtilizadorNome(), e.getResultado(), e.getDescricao(), e.getDadosEssenciais());
    }
}
