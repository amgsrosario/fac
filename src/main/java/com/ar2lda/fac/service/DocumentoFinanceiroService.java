package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroCreateDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoFinanceiroCreateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.DocumentoFinanceiroMapper;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.DocumentoFinanceiro;
import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.model.SerieId;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.LinhaDocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.PendenteRepository;
import com.ar2lda.fac.repository.SerieRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import com.ar2lda.fac.repository.UtilizadorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoFinanceiroService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

    private final DocumentoFinanceiroRepository documentoRepository;
    private final LinhaDocumentoFinanceiroRepository linhaRepository;
    private final PendenteRepository pendenteRepository;
    private final ClienteRepository clienteRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SerieRepository serieRepository;
    private final MoedaRepository moedaRepository;
    private final MPagamentoRepository mPagamentoRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final SerieService serieService;
    private final DocumentoFinanceiroMapper mapper;

    @Transactional
    public DocumentoFinanceiroDto create(DocumentoFinanceiroCreateDto dto) {
        Cliente cliente = findCliente(dto.clienteId());
        TipoDocumento tipoDocumento = findTipoDocumento(dto.tipoDocumentoId());
        validateSerie(dto.tipoDocumentoId(), dto.serie());
        Moeda moeda = findMoeda(dto.moedaId());
        MPagamento mPagamento = findMPagamento(dto.mPagamentoId());
        Utilizador emissor = findEmissor(dto.emissorId());

        DocumentoFinanceiro documento = new DocumentoFinanceiro();
        documento.setCliente(cliente);
        documento.setTipoDocumento(tipoDocumento);
        documento.setSerie(dto.serie());
        documento.setNumeroDocumento(serieService.proximoNumero(dto.tipoDocumentoId(), dto.serie()));
        documento.setDataEmissao(dto.dataEmissao());
        documento.setMoeda(moeda);
        documento.setMPagamento(mPagamento);
        documento.setDataHoraOperacao(dto.dataHoraOperacao() != null ? dto.dataHoraOperacao() : OffsetDateTime.now());
        documento.setEmissor(emissor);
        documento.setMomentoEmissao(OffsetDateTime.now());
        documento.setObservacoes(dto.observacoes());
        documento = documentoRepository.save(documento);

        BigDecimal valorPagamentoBruto = ZERO;
        BigDecimal valorDescontoFinanceiro = ZERO;
        BigDecimal valorPagamentoLiquido = ZERO;

        int numeroLinha = 1;
        for (LinhaDocumentoFinanceiroCreateDto linhaDto : dto.linhas()) {
            LinhaDocumentoFinanceiro linha = criarLinha(documento, cliente, moeda, linhaDto, numeroLinha++);
            valorPagamentoBruto = valorPagamentoBruto.add(linha.getValorALiquidar());
            valorDescontoFinanceiro = valorDescontoFinanceiro.add(linha.getDescontoValor());
            valorPagamentoLiquido = valorPagamentoLiquido.add(linha.getValorPagamentoLiquido());
        }

        documento.setValorPagamentoBruto(valorPagamentoBruto.setScale(6, RoundingMode.HALF_UP));
        documento.setValorDescontoFinanceiro(valorDescontoFinanceiro.setScale(6, RoundingMode.HALF_UP));
        documento.setValorPagamentoLiquido(valorPagamentoLiquido.setScale(6, RoundingMode.HALF_UP));
        documento = documentoRepository.save(documento);

        return toDTO(documento);
    }

    public Page<DocumentoFinanceiroDto> list(Pageable pageable) {
        return documentoRepository.findAll(pageable).map(this::toDTO);
    }

    public DocumentoFinanceiroDto getById(Long id) {
        return toDTO(findDocumento(id));
    }

    @Transactional
    public DocumentoFinanceiroDto anular(Long id) {
        DocumentoFinanceiro documento = findDocumento(id);
        if (documento.isAnulado()) {
            throw new BadRequestException("Documento financeiro ja se encontra anulado");
        }

        List<LinhaDocumentoFinanceiro> linhas = linhaRepository.findByDocumentoFinanceiroIdOrderByNumeroLinha(documento.getId());
        for (LinhaDocumentoFinanceiro linha : linhas) {
            Pendente pendente = linha.getPendente();
            if (pendente.getValorPendente().compareTo(linha.getNovoValorPendente()) != 0) {
                throw new BadRequestException("Documento financeiro nao pode ser anulado porque o pendente teve movimentos posteriores");
            }
        }

        documento.setAnulado(true);
        documento = documentoRepository.save(documento);

        for (LinhaDocumentoFinanceiro linha : linhas) {
            Pendente pendente = linha.getPendente();
            pendente.setValorPendente(linha.getValorPendenteAntes());
            boolean temOutrasLiquidacoes = linhaRepository.existsOtherActiveLinesForDocumentoComercial(
                    pendente.getDocumentoComercial().getId(),
                    documento.getId()
            );
            pendente.getDocumentoComercial().setLiquidado(temOutrasLiquidacoes);
            pendenteRepository.save(pendente);
        }

        return toDTO(documento);
    }

    private LinhaDocumentoFinanceiro criarLinha(DocumentoFinanceiro documento, Cliente cliente, Moeda moeda,
                                                LinhaDocumentoFinanceiroCreateDto dto, int numeroLinha) {
        Pendente pendente = findPendente(dto.pendenteId());
        validatePendente(cliente, moeda, pendente);

        BigDecimal valorALiquidar = scale6(dto.valorALiquidar());
        BigDecimal descontoPercentual = dto.descontoPercentual() != null ? scale6(dto.descontoPercentual()) : null;
        BigDecimal descontoValor = calcularDescontoValor(valorALiquidar, descontoPercentual, dto.descontoValor());
        if (descontoValor.compareTo(valorALiquidar) > 0) {
            throw new BadRequestException("Desconto financeiro nao pode ser superior ao valor a liquidar");
        }
        if (valorALiquidar.compareTo(pendente.getValorPendente()) > 0) {
            throw new BadRequestException("Valor a liquidar nao pode ser superior ao valor pendente");
        }

        BigDecimal valorPendenteAntes = pendente.getValorPendente().setScale(6, RoundingMode.HALF_UP);
        BigDecimal novoValorPendente = valorPendenteAntes.subtract(valorALiquidar).setScale(6, RoundingMode.HALF_UP);

        LinhaDocumentoFinanceiro linha = new LinhaDocumentoFinanceiro();
        linha.setDocumentoFinanceiro(documento);
        linha.setNumeroLinha(numeroLinha);
        linha.setPendente(pendente);
        linha.setDataDocumento(pendente.getDataDocumento());
        linha.setDataVencimento(pendente.getDataVencimento());
        linha.setTipoDocumento(pendente.getTipoDocumento());
        linha.setNumeroDocumento(pendente.getNumeroDocumento());
        linha.setSerieDocumento(pendente.getSerieDocumento());
        linha.setValorDocumento(pendente.getValorDocumento());
        linha.setValorPendenteAntes(valorPendenteAntes);
        linha.setValorALiquidar(valorALiquidar);
        linha.setDescontoPercentual(descontoPercentual);
        linha.setDescontoValor(descontoValor);
        linha.setValorPagamentoLiquido(valorALiquidar.subtract(descontoValor).setScale(6, RoundingMode.HALF_UP));
        linha.setNovoValorPendente(novoValorPendente);
        linha.setMoeda(pendente.getMoeda());
        linha = linhaRepository.save(linha);

        pendente.setValorPendente(novoValorPendente);
        pendente.getDocumentoComercial().setLiquidado(true);
        pendenteRepository.save(pendente);

        return linha;
    }

    private BigDecimal calcularDescontoValor(BigDecimal valorALiquidar, BigDecimal descontoPercentual, BigDecimal descontoValor) {
        if (descontoValor != null) {
            return scale6(descontoValor);
        }
        if (descontoPercentual == null) {
            return ZERO;
        }
        if (descontoPercentual.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Desconto percentual nao pode ser superior a 100");
        }
        return valorALiquidar.multiply(descontoPercentual)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }

    private void validatePendente(Cliente cliente, Moeda moeda, Pendente pendente) {
        if (!pendente.getCliente().getId().equals(cliente.getId())) {
            throw new BadRequestException("Pendente nao pertence ao cliente do documento financeiro");
        }
        if (!pendente.getMoeda().getId().equals(moeda.getId())) {
            throw new BadRequestException("Pendente tem moeda diferente do documento financeiro");
        }
        if (pendente.getValorPendente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Pendente ja se encontra liquidado");
        }
    }

    private DocumentoFinanceiroDto toDTO(DocumentoFinanceiro documento) {
        List<LinhaDocumentoFinanceiro> linhas = linhaRepository.findByDocumentoFinanceiroIdOrderByNumeroLinha(documento.getId());
        return mapper.toDTO(documento, linhas);
    }

    private DocumentoFinanceiro findDocumento(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Documento financeiro nao encontrado: " + id));
    }

    private Cliente findCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado: " + id));
    }

    private TipoDocumento findTipoDocumento(String id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de documento nao encontrado: " + id));
    }

    private void validateSerie(String tipoDocumentoId, String serie) {
        if (!serieRepository.existsById(new SerieId(tipoDocumentoId, serie))) {
            throw new NotFoundException("Serie nao encontrada: " + tipoDocumentoId + "/" + serie);
        }
    }

    private Moeda findMoeda(String id) {
        return moedaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moeda nao encontrada: " + id));
    }

    private MPagamento findMPagamento(Integer id) {
        return mPagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modo de pagamento nao encontrado: " + id));
    }

    private Utilizador findEmissor(String id) {
        Utilizador emissor = utilizadorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Emissor nao encontrado: " + id));
        if (emissor.isInativo()) {
            throw new BadRequestException("Emissor inativo nao pode emitir documento financeiro");
        }
        return emissor;
    }

    private Pendente findPendente(Long id) {
        return pendenteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pendente nao encontrado: " + id));
    }

    private BigDecimal scale6(BigDecimal value) {
        return value.setScale(6, RoundingMode.HALF_UP);
    }
}
