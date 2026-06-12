package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.UtilizadorCreateDto;
import com.ar2lda.fac.controller.dto.UtilizadorDto;
import com.ar2lda.fac.controller.dto.UtilizadorUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.UtilizadorMapper;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.UtilizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UtilizadorService {

    private final UtilizadorRepository repository;
    private final UtilizadorMapper mapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UtilizadorDto create(UtilizadorCreateDto dto) {
        String codigo = normalizeCodigo(dto.codigo());
        String email = normalizeEmail(dto.email());
        if (repository.existsById(codigo)) {
            throw new ConflictException("Utilizador já existe: " + codigo);
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email já está associado a outro utilizador: " + email);
        }

        Utilizador entity = mapper.fromCreateDTO(dto);
        entity = new Utilizador(codigo, entity.getNome(), email, passwordEncoder.encode(dto.password()), entity.isInativo());
        return mapper.toDTO(repository.save(entity));
    }

    public Page<UtilizadorDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public UtilizadorDto getByCodigo(String codigo) {
        return mapper.toDTO(findEntityByCodigo(codigo));
    }

    public void update(String codigo, UtilizadorUpdateDto dto) {
        Utilizador existing = findEntityByCodigo(codigo);
        String email = normalizeEmail(dto.email());
        if (repository.existsByEmailIgnoreCaseAndCodigoNot(email, existing.getCodigo())) {
            throw new ConflictException("Email já está associado a outro utilizador: " + email);
        }

        mapper.applyUpdate(dto, existing);
        existing.setEmail(email);
        if (dto.password() != null) {
            existing.setPasswordHash(passwordEncoder.encode(dto.password()));
        }
        repository.save(existing);
    }

    public void delete(String codigo) {
        repository.delete(findEntityByCodigo(codigo));
        repository.flush();
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
}
