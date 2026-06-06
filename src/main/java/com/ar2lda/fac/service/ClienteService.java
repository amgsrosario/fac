package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ClienteCreateDto;
import com.ar2lda.fac.controller.dto.ClienteDto;
import com.ar2lda.fac.controller.dto.ClienteUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.ClienteMapper;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.model.PPagamento;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.PaisRepository;
import com.ar2lda.fac.repository.PPagamentoRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.TransporteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private static final String RIVA_DEFAULT_ID = "CON";

    private final ClienteRepository clienteRepository;
    private final CodPostalRepository codPostalRepository;
    private final PaisRepository paisRepository;
    private final MoedaRepository moedaRepository;
    private final MPagamentoRepository mPagamentoRepository;
    private final PPagamentoRepository pPagamentoRepository;
    private final RIvaRepository rIvaRepository;
    private final TransporteRepository transporteRepository;
    private final ClienteMapper mapper;

    @Transactional
    public ClienteDto create(ClienteCreateDto dto) {
        if (clienteRepository.existsByNif(dto.nif())) {
            throw new ConflictException("Já existe um cliente com o NIF: " + dto.nif());
        }
        Cliente cliente = mapper.fromCreateDTO(dto);
        applyRelations(dto.codPostalId(), dto.paisId(), dto.moedaId(), dto.mPagamentoId(), dto.pPagamentoId(), dto.rivaId(),
                dto.transporteId(), cliente);
        return mapper.toDTO(clienteRepository.save(cliente));
    }

    public Page<ClienteDto> list(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(mapper::toDTO);
    }

    public ClienteDto getById(Long id) {
        return mapper.toDTO(findEntityById(id));
    }

    @Transactional
    public ClienteDto update(Long id, ClienteUpdateDto dto) {
        Cliente cliente = findEntityById(id);
        if (clienteRepository.existsByNifAndIdNot(dto.nif(), id)) {
            throw new ConflictException("Já existe um cliente com o NIF: " + dto.nif());
        }
        mapper.applyUpdate(dto, cliente);
        applyRelations(dto.codPostalId(), dto.paisId(), dto.moedaId(), dto.mPagamentoId(), dto.pPagamentoId(), dto.rivaId(),
                dto.transporteId(), cliente);
        return mapper.toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public void delete(Long id) {
        clienteRepository.delete(findEntityById(id));
    }

    private Cliente findEntityById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
    }

    private void applyRelations(String codPostalId, String paisId, String moedaId, Integer mPagamentoId, String pPagamentoId,
                                String rivaId, Integer transporteId, Cliente cliente) {
        cliente.setCodPostal(findCodPostal(codPostalId));
        cliente.setPais(findPais(paisId));
        cliente.setMoeda(findMoeda(moedaId));
        cliente.setMPagamento(findMPagamento(mPagamentoId));
        cliente.setPPagamento(findPPagamento(pPagamentoId));
        cliente.setRiva(findRIva(rivaId));
        cliente.setTransporte(findTransporte(transporteId));
    }

    private CodPostal findCodPostal(String id) {
        return codPostalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Código postal não encontrado: " + id));
    }

    private Pais findPais(String id) {
        return paisRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("País não encontrado: " + id));
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

    private PPagamento findPPagamento(String id) {
        if (id == null) return null;
        return pPagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prazo de pagamento não encontrado: " + id));
    }

    private RIva findRIva(String id) {
        String effectiveId = (id == null || id.isBlank()) ? RIVA_DEFAULT_ID : id;
        return rIvaRepository.findById(effectiveId)
                .orElseThrow(() -> new NotFoundException("Regime de IVA não encontrado: " + id));
    }

    private Transporte findTransporte(Integer id) {
        return transporteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transporte não encontrado: " + id));
    }

}
