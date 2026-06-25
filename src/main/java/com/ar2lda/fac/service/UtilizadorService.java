package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.UtilizadorCreateDto;
import com.ar2lda.fac.controller.dto.UtilizadorDto;
import com.ar2lda.fac.controller.dto.UtilizadorUpdateDto;
import com.ar2lda.fac.controller.dto.UtilizadorEstadoDto;
import com.ar2lda.fac.controller.dto.UtilizadorPerfilDto;
import com.ar2lda.fac.controller.dto.UtilizadorPasswordResetDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.UtilizadorMapper;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.UtilizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Set;
import com.ar2lda.fac.model.PapelUtilizador;
import com.ar2lda.fac.model.ResultadoAuditoria;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UtilizadorService {

    private final UtilizadorRepository repository;
    private final UtilizadorMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final AuditoriaService auditoriaService;
    private final AuditoriaIsoladaService auditoriaIsoladaService;
    private final Clock clock;

    private static final Set<String> PASSWORDS_PROIBIDAS = Set.of(
            "password", "password123!", "admin123!", "fac12345!", "1234567890"
    );

    @Transactional
    public UtilizadorDto create(UtilizadorCreateDto dto) {
        String codigo = normalizeCodigo(dto.codigo());
        String email = normalizeEmail(dto.email());
        if (repository.existsById(codigo)) {
            throw new ConflictException("Utilizador já existe: " + codigo);
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email já está associado a outro utilizador: " + email);
        }

        validarPassword(dto.password(), codigo);
        Utilizador entity = mapper.fromCreateDTO(dto);
        entity = new Utilizador(codigo, entity.getNome().trim(), email, passwordEncoder.encode(dto.password()), entity.isInativo());
        entity.setPapel(dto.papel());
        String actor = currentUserService.currentCodeOrSystem();
        entity.marcarCriacao(OffsetDateTime.now(clock), actor);
        Utilizador saved = repository.save(entity);
        auditoriaService.registar(TipoAuditoriaEvento.UTILIZADOR_CRIADO, "UTILIZADOR", saved.getCodigo(),
                "Utilizador criado", dados("perfil", saved.getPapel().name(), "ativo", String.valueOf(!saved.isInativo())));
        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<UtilizadorDto> list(String pesquisa, PapelUtilizador papel, Boolean ativo, Pageable pageable) {
        Specification<Utilizador> spec = Specification.where(null);
        if (pesquisa != null && !pesquisa.isBlank()) {
            String like = "%" + pesquisa.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("codigo")), like),
                    cb.like(cb.lower(root.get("nome")), like),
                    cb.like(cb.lower(root.get("email")), like)
            ));
        }
        if (papel != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("papel"), papel));
        if (ativo != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("inativo"), !ativo));
        return repository.findAll(spec, pageable).map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public UtilizadorDto getByCodigo(String codigo) {
        return mapper.toDTO(findEntityByCodigo(codigo));
    }

    @Transactional
    public UtilizadorDto update(String codigo, UtilizadorUpdateDto dto) {
        Utilizador existing = findEntityByCodigo(codigo);
        String email = normalizeEmail(dto.email());
        if (repository.existsByEmailIgnoreCaseAndCodigoNot(email, existing.getCodigo())) {
            throw new ConflictException("Email já está associado a outro utilizador: " + email);
        }

        mapper.applyUpdate(dto, existing);
        existing.setEmail(email);
        existing.marcarAtualizacao(OffsetDateTime.now(clock), currentUserService.currentCodeOrSystem());
        repository.save(existing);
        auditoriaService.registar(TipoAuditoriaEvento.UTILIZADOR_ALTERADO, "UTILIZADOR", existing.getCodigo(),
                "Dados do utilizador alterados", "{\"versao\":1}");
        return mapper.toDTO(existing);
    }

    @Transactional
    public UtilizadorDto alterarEstado(String codigo, UtilizadorEstadoDto dto) {
        Utilizador existing = findEntityByCodigo(codigo);
        boolean novoInativo = !dto.ativo();
        if (novoInativo && !existing.isInativo() && existing.getPapel() == PapelUtilizador.ADMINISTRADOR) {
            protegerUltimoAdministrador(existing, TipoAuditoriaEvento.TENTATIVA_ULTIMO_ADMINISTRADOR_NEGADA,
                    "Tentativa de desativar o último administrador ativo");
        }
        existing.setInativo(novoInativo);
        existing.marcarAtualizacao(OffsetDateTime.now(clock), currentUserService.currentCodeOrSystem());
        auditoriaService.registar(TipoAuditoriaEvento.UTILIZADOR_ESTADO_ALTERADO, "UTILIZADOR", existing.getCodigo(),
                dto.ativo() ? "Utilizador ativado" : "Utilizador desativado",
                dados("ativo", dto.ativo().toString()));
        return mapper.toDTO(existing);
    }

    @Transactional
    public UtilizadorDto alterarPerfil(String codigo, UtilizadorPerfilDto dto) {
        Utilizador existing = findEntityByCodigo(codigo);
        PapelUtilizador anterior = existing.getPapel();
        if (anterior == PapelUtilizador.ADMINISTRADOR && dto.papel() != PapelUtilizador.ADMINISTRADOR && !existing.isInativo()) {
            protegerUltimoAdministrador(existing, TipoAuditoriaEvento.TENTATIVA_ULTIMO_ADMINISTRADOR_NEGADA,
                    "Tentativa de retirar o perfil ao último administrador ativo");
        }
        String actor = currentUserService.currentCodeOrSystem();
        if (actor.equalsIgnoreCase(existing.getCodigo()) && nivel(dto.papel()) > nivel(anterior)) {
            auditarNegada(existing, TipoAuditoriaEvento.TENTATIVA_ADMINISTRATIVA_NEGADA, "Autoelevação de perfil recusada");
            throw new BadRequestException("Não é permitido elevar o próprio perfil");
        }
        existing.setPapel(dto.papel());
        existing.marcarAtualizacao(OffsetDateTime.now(clock), actor);
        auditoriaService.registar(TipoAuditoriaEvento.UTILIZADOR_PERFIL_ALTERADO, "UTILIZADOR", existing.getCodigo(),
                "Perfil do utilizador alterado", dados("perfilAnterior", anterior.name(), "perfilNovo", dto.papel().name()));
        return mapper.toDTO(existing);
    }

    @Transactional
    public void redefinirPassword(String codigo, UtilizadorPasswordResetDto dto) {
        Utilizador existing = findEntityByCodigo(codigo);
        validarPassword(dto.novaPassword(), existing.getCodigo());
        existing.setPasswordHash(passwordEncoder.encode(dto.novaPassword()));
        existing.marcarAtualizacao(OffsetDateTime.now(clock), currentUserService.currentCodeOrSystem());
        auditoriaService.registar(TipoAuditoriaEvento.UTILIZADOR_PASSWORD_REDEFINIDA, "UTILIZADOR", existing.getCodigo(),
                "Password do utilizador redefinida", "{\"versao\":1}");
    }

    private Utilizador findEntityByCodigo(String codigo) {
        String normalized = normalizeCodigo(codigo);
        return repository.findById(normalized)
                .orElseThrow(() -> new NotFoundException("Utilizador não encontrado: " + normalized));
    }

    private String normalizeCodigo(String codigo) {
        return codigo.toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void protegerUltimoAdministrador(Utilizador alvo, TipoAuditoriaEvento evento, String descricao) {
        long ativos = repository.findByPapelForUpdate(PapelUtilizador.ADMINISTRADOR).stream()
                .filter(utilizador -> !utilizador.isInativo()).count();
        if (ativos <= 1) {
            auditarNegada(alvo, evento, descricao);
            throw new ConflictException(descricao);
        }
    }

    private void auditarNegada(Utilizador alvo, TipoAuditoriaEvento evento, String descricao) {
        auditoriaIsoladaService.registar(evento, "UTILIZADOR", alvo.getCodigo(), currentUserService.currentUserOrNull(),
                ResultadoAuditoria.FALHA, alvo.getCodigo(), descricao, "{\"versao\":1}");
    }

    private void validarPassword(String password, String codigo) {
        String lower = password.toLowerCase(Locale.ROOT);
        if (PASSWORDS_PROIBIDAS.contains(lower) || lower.contains(codigo.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("A password escolhida é demasiado simples ou previsível");
        }
    }

    private int nivel(PapelUtilizador papel) {
        return switch (papel) {
            case CONSULTA -> 1;
            case OPERADOR -> 2;
            case ADMINISTRADOR -> 3;
        };
    }

    private String dados(String... pairs) {
        StringBuilder json = new StringBuilder("{\"versao\":1");
        for (int i = 0; i < pairs.length; i += 2) {
            json.append(",\"").append(pairs[i]).append("\":\"")
                    .append(pairs[i + 1].replace("\\", "\\\\").replace("\"", "\\\""))
                    .append("\"");
        }
        return json.append('}').toString();
    }
}
