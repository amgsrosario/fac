package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.ListagemDocumentoComercialDto;
import com.ar2lda.fac.controller.dto.ListagemLinhaComercialDto;
import com.ar2lda.fac.controller.dto.ListagemLinhaFinanceiraDto;
import com.ar2lda.fac.controller.dto.PendenteListagemDto;
import com.ar2lda.fac.controller.dto.PendenteListagemTotaisDto;
import com.ar2lda.fac.controller.dto.PendentesListagemDto;
import com.ar2lda.fac.mapper.DocumentoComercialMapper;
import com.ar2lda.fac.mapper.DocumentoFinanceiroMapper;
import com.ar2lda.fac.mapper.LinhaDocumentoComercialMapper;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.DocumentoFinanceiro;
import com.ar2lda.fac.model.LinhaDocumentoComercial;
import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.LinhaDocumentoComercialRepository;
import com.ar2lda.fac.repository.LinhaDocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.PendenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ListagensService {

    private final DocumentoComercialRepository documentoComercialRepository;
    private final LinhaDocumentoComercialRepository linhaDocumentoComercialRepository;
    private final DocumentoFinanceiroRepository documentoFinanceiroRepository;
    private final LinhaDocumentoFinanceiroRepository linhaDocumentoFinanceiroRepository;
    private final PendenteRepository pendenteRepository;
    private final DocumentoComercialMapper documentoComercialMapper;
    private final LinhaDocumentoComercialMapper linhaDocumentoComercialMapper;
    private final DocumentoFinanceiroMapper documentoFinanceiroMapper;

    @Transactional(readOnly = true)
    public Page<ListagemDocumentoComercialDto> documentosComerciais(LocalDate dataInicial, LocalDate dataFinal, Long clienteId, Pageable pageable) {
        return documentoComercialRepository.findAnaliticos(dataInicial, dataFinal, clienteId, pageable)
                .map(this::toDocumentoComercialDto);
    }

    @Transactional(readOnly = true)
    public Page<ListagemLinhaComercialDto> linhasComerciais(LocalDate dataInicial, LocalDate dataFinal, Long clienteId, String artigoId, Pageable pageable) {
        return linhaDocumentoComercialRepository.findAnaliticas(dataInicial, dataFinal, clienteId, blankToNull(artigoId), pageable)
                .map(this::toLinhaComercialDto);
    }

    @Transactional(readOnly = true)
    public Page<DocumentoFinanceiroDto> documentosFinanceiros(LocalDate dataInicial, LocalDate dataFinal, Long clienteId, Pageable pageable) {
        return documentoFinanceiroRepository.findAnaliticos(dataInicial, dataFinal, clienteId, pageable)
                .map(this::toDocumentoFinanceiroDto);
    }

    @Transactional(readOnly = true)
    public Page<ListagemLinhaFinanceiraDto> linhasFinanceiras(LocalDate dataInicial, LocalDate dataFinal, Long clienteId, Pageable pageable) {
        return linhaDocumentoFinanceiroRepository.findAnaliticas(dataInicial, dataFinal, clienteId, pageable)
                .map(this::toLinhaFinanceiraDto);
    }

    @Transactional(readOnly = true)
    public PendentesListagemDto pendentes(List<Long> clienteIds) {
        List<Long> filtroClientes = clienteIds == null ? List.of() : clienteIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        boolean filtrarClientes = !filtroClientes.isEmpty();
        List<PendenteListagemDto> linhas = pendenteRepository
                .findPendentesListagem(LocalDate.now(), filtrarClientes, filtrarClientes ? filtroClientes : List.of(-1L))
                .stream()
                .map(this::toPendenteListagemDto)
                .toList();
        return new PendentesListagemDto(linhas, pendentesTotais(linhas));
    }

    private ListagemLinhaComercialDto toLinhaComercialDto(LinhaDocumentoComercial linha) {
        return new ListagemLinhaComercialDto(
                documentoComercialMapper.toDTO(linha.getDocumentoComercial()),
                linhaDocumentoComercialMapper.toDTO(linha)
        );
    }

    private ListagemDocumentoComercialDto toDocumentoComercialDto(DocumentoComercial documento) {
        BigDecimal valorLiquido = nullToZero(documento.getValorBruto()).subtract(nullToZero(documento.getValorDesconto()));
        return new ListagemDocumentoComercialDto(documentoComercialMapper.toDTO(documento), valorLiquido);
    }

    private PendenteListagemDto toPendenteListagemDto(Pendente pendente) {
        BigDecimal total = nullToZero(pendente.getValorDocumento());
        BigDecimal valorPendente = nullToZero(pendente.getValorPendente());
        return new PendenteListagemDto(
                pendente.getDocumentoComercial().getId(),
                "%s %s/%s".formatted(
                        pendente.getTipoDocumento().getId(),
                        pendente.getSerieDocumento(),
                        pendente.getNumeroDocumento()
                ),
                pendente.getDataDocumento(),
                pendente.getDataVencimento(),
                pendente.getCliente().getId(),
                pendente.getCliente().getNif(),
                pendente.getCliente().getNome(),
                pendente.getMoeda().getId(),
                total,
                total.subtract(valorPendente),
                valorPendente
        );
    }

    private DocumentoFinanceiroDto toDocumentoFinanceiroDto(DocumentoFinanceiro documento) {
        return documentoFinanceiroMapper.toDTO(documento, List.of());
    }

    private ListagemLinhaFinanceiraDto toLinhaFinanceiraDto(LinhaDocumentoFinanceiro linha) {
        return new ListagemLinhaFinanceiraDto(
                documentoFinanceiroMapper.toDTO(linha.getDocumentoFinanceiro(), List.of()),
                documentoFinanceiroMapper.toLinhaDTO(linha)
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private PendenteListagemTotaisDto pendentesTotais(List<PendenteListagemDto> linhas) {
        return new PendenteListagemTotaisDto(
                sum(linhas.stream().map(PendenteListagemDto::total).toList()),
                sum(linhas.stream().map(PendenteListagemDto::recebido).toList()),
                sum(linhas.stream().map(PendenteListagemDto::pendente).toList())
        );
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().map(this::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
