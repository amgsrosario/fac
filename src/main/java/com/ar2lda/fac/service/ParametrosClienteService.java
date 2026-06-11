package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ParametrosClienteCreateDto;
import com.ar2lda.fac.controller.dto.ParametrosClienteDto;
import com.ar2lda.fac.controller.dto.ParametrosClienteUpdateDto;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.ParametrosClienteMapper;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.PPagamento;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.model.ParametrosCliente;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.PPagamentoRepository;
import com.ar2lda.fac.repository.PaisRepository;
import com.ar2lda.fac.repository.ParametrosClienteRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.TransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParametrosClienteService {

    private final ParametrosClienteRepository repository;
    private final PaisRepository paisRepository;
    private final MoedaRepository moedaRepository;
    private final RIvaRepository rIvaRepository;
    private final MPagamentoRepository mPagamentoRepository;
    private final PPagamentoRepository pPagamentoRepository;
    private final TransporteRepository transporteRepository;
    private final ParametrosClienteMapper mapper;

    @Transactional
    public ParametrosClienteDto create(ParametrosClienteCreateDto dto) {
        if (repository.existsById(ParametrosCliente.PARAMETROS_ID)) {
            throw new ConflictException("Parâmetros de cliente já existem");
        }
        ParametrosCliente entity = mapper.fromCreateDTO(dto);
        applyRelations(dto.paisId(), dto.moedaId(), dto.rivaId(), dto.mPagamentoId(), dto.pPagamentoId(),
                dto.transporteId(), entity);
        return mapper.toDTO(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ParametrosClienteDto get() {
        return mapper.toDTO(findParametros());
    }

    @Transactional
    public void update(ParametrosClienteUpdateDto dto) {
        ParametrosCliente entity = findParametros();
        mapper.applyUpdate(dto, entity);
        applyRelations(dto.paisId(), dto.moedaId(), dto.rivaId(), dto.mPagamentoId(), dto.pPagamentoId(),
                dto.transporteId(), entity);
        repository.save(entity);
    }

    private ParametrosCliente findParametros() {
        return repository.findById(ParametrosCliente.PARAMETROS_ID)
                .orElseThrow(() -> new NotFoundException("Parâmetros de cliente não encontrados"));
    }

    private void applyRelations(String paisId, String moedaId, String rivaId, Integer mPagamentoId,
                                String pPagamentoId, Integer transporteId, ParametrosCliente entity) {
        entity.setPais(findPais(paisId));
        entity.setMoeda(findMoeda(moedaId));
        entity.setRiva(findRIva(rivaId));
        entity.setMPagamento(findMPagamento(mPagamentoId));
        entity.setPPagamento(findPPagamento(pPagamentoId));
        entity.setTransporte(findTransporte(transporteId));
    }

    private Pais findPais(String id) {
        if (id == null || id.isBlank()) return null;
        return paisRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("País não encontrado: " + id));
    }

    private Moeda findMoeda(String id) {
        if (id == null || id.isBlank()) return null;
        return moedaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moeda não encontrada: " + id));
    }

    private RIva findRIva(String id) {
        if (id == null || id.isBlank()) return null;
        return rIvaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Regime de IVA não encontrado: " + id));
    }

    private MPagamento findMPagamento(Integer id) {
        if (id == null) return null;
        return mPagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modo de pagamento não encontrado: " + id));
    }

    private PPagamento findPPagamento(String id) {
        if (id == null || id.isBlank()) return null;
        return pPagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prazo de pagamento não encontrado: " + id));
    }

    private Transporte findTransporte(Integer id) {
        if (id == null) return null;
        return transporteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transporte não encontrado: " + id));
    }
}
