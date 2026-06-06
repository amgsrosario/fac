package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.PendenteDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.PendenteMapper;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.repository.PendenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PendenteService {

    private final PendenteRepository repository;
    private final PendenteMapper mapper;

    public Page<PendenteDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public PendenteDto getById(Long id) {
        return mapper.toDTO(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pendente nÃ£o encontrado: " + id)));
    }

    public Pendente criarDeDocumento(DocumentoComercial documento) {
        if (documento.getTipoDocumento().isLiquidacaoImediata()) {
            return null;
        }
        if (documento.getEstado() != EstadoDocumentoComercial.EMITIDO || documento.getNumeroDocumento() == null) {
            throw new BadRequestException("Pendente sÃ³ pode ser criado para documento emitido");
        }
        return repository.findByDocumentoComercialId(documento.getId())
                .orElseGet(() -> repository.save(novoPendente(documento)));
    }

    private Pendente novoPendente(DocumentoComercial documento) {
        BigDecimal valorTotal = documento.getValorTotal();
        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Documento sem valor nÃ£o gera pendente");
        }

        Pendente pendente = new Pendente();
        pendente.setDocumentoComercial(documento);
        pendente.setCliente(documento.getCliente());
        pendente.setTipoDocumento(documento.getTipoDocumento());
        pendente.setNumeroDocumento(documento.getNumeroDocumento());
        pendente.setSerieDocumento(documento.getSerie());
        pendente.setValorDocumento(valorTotal);
        pendente.setValorPendente(valorTotal);
        pendente.setDataDocumento(documento.getDataEmissao());
        pendente.setDataVencimento(documento.getDataVencimento());
        pendente.setMoeda(documento.getMoeda());
        return pendente;
    }
}
