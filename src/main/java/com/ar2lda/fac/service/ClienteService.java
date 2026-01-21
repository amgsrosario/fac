package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ClienteCreateDto;
import com.ar2lda.fac.controller.dto.ClienteDto;
import com.ar2lda.fac.controller.dto.ClienteUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.ClienteMapper;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final CodPostalRepository codPostalRepository;
    private final MoedaRepository moedaRepository;
    private final MPagamentoRepository mPagamentoRepository;
    private final PPagamentoRepository pPagamentoRepository;
    private final RIvaRepository rIvaRepository;
    private final TransporteRepository transporteRepository;

    private final ClienteMapper mapper;

    @Transactional
    public ClienteDto create(ClienteCreateDto dto) {
        // NIF uniqueness check
        if (clienteRepository.existsByNif(dto.nif())) {
            throw new ConflictException("Já existe um cliente com o NIF: " + dto.nif());
        }
        Cliente c = new Cliente();
        applyCreate(dto, c);
        Cliente saved = clienteRepository.save(c);
        return mapper.toDTO(saved);
    }

    public Page<ClienteDto> list(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(mapper::toDTO);
    }

    public ClienteDto getById(Long id) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
        return mapper.toDTO(c);
    }

    @Transactional
    public ClienteDto update(Long id, ClienteUpdateDto dto) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
        // NIF uniqueness check for update
        if (clienteRepository.existsByNifAndIdNot(dto.nif(), id)) {
            throw new ConflictException("Já existe um cliente com o NIF: " + dto.nif());
        }
        applyUpdate(dto, c);
        Cliente saved = clienteRepository.save(c);
        return mapper.toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
        clienteRepository.delete(c);
    }

    private void applyCreate(ClienteCreateDto dto, Cliente c) {
        // Scalars
        c.setNome(dto.nome());
        c.setMorada(dto.morada());
        c.setMorada1(dto.morada1());
        c.setLocalidade(dto.localidade());
        c.setNif(dto.nif());
        c.setTel(dto.tel());
        c.setTm(dto.tm());
        c.setEmail(dto.email());
        c.setEmail1(dto.email1());
        c.setTspiva(dto.tspiva());
        c.setIban(dto.iban());
        c.setRetencao(dto.retencao());
        c.setInativo(dto.inativo());
        c.setObservacoes(dto.observacoes());
        // Relations (required)
        c.setCodPostal(findCodPostal(dto.codPostalId()));
        c.setMoeda(findMoeda(dto.moedaId()));
        // Relations (optionals)
        c.setMPagamento(findMPagamento(dto.mPagamentoId()));
        c.setPPagamento(findPPagamento(dto.pPagamentoId()));
        c.setRiva(findRIva(dto.rivaId()));
        c.setTransporte(findTransporte(dto.transporteId()));
    }

    private void applyUpdate(ClienteUpdateDto dto, Cliente c) {
        // Scalars
        c.setNome(dto.nome());
        c.setMorada(dto.morada());
        c.setMorada1(dto.morada1());
        c.setLocalidade(dto.localidade());
        c.setNif(dto.nif());
        c.setTel(dto.tel());
        c.setTm(dto.tm());
        c.setEmail(dto.email());
        c.setEmail1(dto.email1());
        c.setTspiva(dto.tspiva());
        c.setIban(dto.iban());
        c.setRetencao(dto.retencao());
        c.setInativo(dto.inativo());
        c.setObservacoes(dto.observacoes());
        // Relations (required)
        c.setCodPostal(findCodPostal(dto.codPostalId()));
        c.setMoeda(findMoeda(dto.moedaId()));
        // Relations (optionals)
        c.setMPagamento(findMPagamento(dto.mPagamentoId()));
        c.setPPagamento(findPPagamento(dto.pPagamentoId()));
        c.setRiva(findRIva(dto.rivaId()));
        c.setTransporte(findTransporte(dto.transporteId()));
    }

    private CodPostal findCodPostal(String id) {
        return codPostalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Código postal não encontrado: " + id));
    }

    private Moeda findMoeda(String id) {
        return moedaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moeda não encontrada: " + id));
    }

    private MPagamento findMPagamento(Integer id) {
        if (id == null) return null;
        return mPagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modo de pagamento não encontrado: " + id));
    }

    private PPagamento findPPagamento(Integer id) {
        if (id == null) return null;
        return pPagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prazo de pagamento não encontrado: " + id));
    }

    private RIva findRIva(String id) {
        if (id == null) return null;
        return rIvaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Regime de IVA não encontrado: " + id));
    }

    private Transporte findTransporte(Integer id) {
        if (id == null) return null;
        return transporteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transporte não encontrado: " + id));
    }
}
