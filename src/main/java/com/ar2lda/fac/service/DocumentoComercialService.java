package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialEmitirDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.DocumentoComercialMapper;
import com.ar2lda.fac.model.Armazem;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.Morada;
import com.ar2lda.fac.model.PPagamento;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.SerieId;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.ArmazemRepository;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.LinhaDocumentoComercialRepository;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.MoradaRepository;
import com.ar2lda.fac.repository.PPagamentoRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.SerieRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import com.ar2lda.fac.repository.TransporteRepository;
import com.ar2lda.fac.repository.UtilizadorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class DocumentoComercialService {

    private final DocumentoComercialRepository documentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SerieRepository serieRepository;
    private final ClienteRepository clienteRepository;
    private final MoradaRepository moradaRepository;
    private final ArmazemRepository armazemRepository;
    private final MoedaRepository moedaRepository;
    private final RIvaRepository rIvaRepository;
    private final MPagamentoRepository mPagamentoRepository;
    private final PPagamentoRepository pPagamentoRepository;
    private final TransporteRepository transporteRepository;
    private final LinhaDocumentoComercialRepository linhaRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final SerieService serieService;
    private final PendenteService pendenteService;
    private final DocumentoComercialMapper mapper;

    @Transactional
    public DocumentoComercialDto create(DocumentoComercialCreateDto dto) {
        TipoDocumento tipoDocumento = findTipoDocumento(dto.tipoDocumentoId());
        validateSerie(dto.tipoDocumentoId(), dto.serie());
        Cliente cliente = findCliente(dto.clienteId());

        DocumentoComercial documento = new DocumentoComercial();
        documento.setTipoDocumento(tipoDocumento);
        documento.setSerie(dto.serie());
        documento.setEstado(EstadoDocumentoComercial.RASCUNHO);
        documento.setDataEmissao(dto.dataEmissao());
        documento.setCliente(cliente);

        applyEditableFields(documento, cliente, dto.moradaEnvioId(), dto.armazemCargaId(), dto.moedaId(),
                dto.rivaId(), dto.mPagamentoId(), dto.pPagamentoId(), dto.transporteId(), dto.dataCarga(),
                dto.horaCarga(), dto.matricula(), dto.dataDescarga(), dto.horaDescarga(), dto.peso(),
                dto.observacoes());
        snapshotCliente(documento, cliente);

        return mapper.toDTO(documentoRepository.save(documento));
    }

    public Page<DocumentoComercialDto> list(Pageable pageable) {
        return documentoRepository.findAll(pageable).map(mapper::toDTO);
    }

    public DocumentoComercialDto getById(Long id) {
        return mapper.toDTO(findDocumento(id));
    }

    @Transactional
    public DocumentoComercialDto update(Long id, DocumentoComercialUpdateDto dto) {
        DocumentoComercial documento = findDocumento(id);
        validateRascunho(documento);
        documento.setDataEmissao(dto.dataEmissao());
        applyEditableFields(documento, documento.getCliente(), dto.moradaEnvioId(), dto.armazemCargaId(), dto.moedaId(),
                dto.rivaId(), dto.mPagamentoId(), dto.pPagamentoId(), dto.transporteId(), dto.dataCarga(),
                dto.horaCarga(), dto.matricula(), dto.dataDescarga(), dto.horaDescarga(), dto.peso(),
                dto.observacoes());
        return mapper.toDTO(documentoRepository.save(documento));
    }

    @Transactional
    public void delete(Long id) {
        DocumentoComercial documento = findDocumento(id);
        validateRascunho(documento);
        documentoRepository.delete(documento);
    }

    @Transactional
    public DocumentoComercialDto emitir(Long id, DocumentoComercialEmitirDto dto) {
        DocumentoComercial documento = findDocumento(id);
        validateRascunho(documento);
        validateTemLinhas(documento);
        validateDataEmissao(documento);

        Utilizador emissor = findEmissor(dto.emissorId());
        documento.setNumeroDocumento(serieService.proximoNumero(
                documento.getTipoDocumento().getId(),
                documento.getSerie()
        ));
        documento.setEstado(EstadoDocumentoComercial.EMITIDO);
        documento.setMomentoEmissao(OffsetDateTime.now());
        documento.setEmissor(emissor);

        DocumentoComercial saved = documentoRepository.save(documento);
        pendenteService.criarDeDocumento(saved);
        return mapper.toDTO(saved);
    }

    private void applyEditableFields(DocumentoComercial documento, Cliente cliente, Long moradaEnvioId, Long armazemCargaId,
                                     String moedaId, String rivaId, Integer mPagamentoId, String pPagamentoId,
                                     Integer transporteId, java.time.LocalDate dataCarga, java.time.LocalTime horaCarga,
                                     String matricula, java.time.LocalDate dataDescarga, java.time.LocalTime horaDescarga,
                                     java.math.BigDecimal peso, String observacoes) {
        Morada moradaEnvio = findMoradaEnvio(moradaEnvioId, cliente);
        Armazem armazemCarga = findArmazem(armazemCargaId);

        documento.setMoradaEnvio(moradaEnvio);
        documento.setArmazemCarga(armazemCarga);
        documento.setMoeda(findMoedaOrDefault(moedaId, cliente.getMoeda()));
        documento.setRiva(findRIvaOrDefault(rivaId, cliente.getRiva()));
        documento.setMPagamento(findMPagamentoOrDefault(mPagamentoId, cliente.getMPagamento()));
        PPagamento pPagamento = findPPagamentoOrDefault(pPagamentoId, cliente.getPPagamento());
        documento.setPPagamento(pPagamento);
        documento.setDataVencimento(documento.getDataEmissao().plusDays(pPagamento.getDias()));
        documento.setTransporte(findTransporteOrDefault(transporteId, cliente.getTransporte()));
        documento.setDataCarga(dataCarga != null ? dataCarga : documento.getDataEmissao());
        documento.setHoraCarga(horaCarga);
        documento.setMatricula(matricula);
        documento.setDataDescarga(dataDescarga);
        documento.setHoraDescarga(horaDescarga);
        documento.setPeso(peso);
        documento.setObservacoes(observacoes);

        snapshotEnvioEDescarga(documento, cliente, moradaEnvio);
        snapshotCarga(documento, armazemCarga);
    }

    private void snapshotCliente(DocumentoComercial documento, Cliente cliente) {
        documento.setClienteNome(cliente.getNome());
        documento.setClienteNif(cliente.getNif());
        documento.setClienteMorada(cliente.getMorada());
        documento.setClienteMorada1(cliente.getMorada1());
        documento.setClienteCodPostal(cliente.getCodPostal().getId());
        documento.setClienteLocalidade(cliente.getLocalidade());
        documento.setClientePais(cliente.getPais().getId());
    }

    private void snapshotEnvioEDescarga(DocumentoComercial documento, Cliente cliente, Morada moradaEnvio) {
        if (moradaEnvio == null) {
            documento.setEnvioNome(null);
            documento.setEnvioMorada(null);
            documento.setEnvioMorada1(null);
            documento.setEnvioCodPostal(null);
            documento.setEnvioLocalidade(null);
            documento.setEnvioPais(null);
            documento.setDescargaMorada(cliente.getMorada());
            documento.setDescargaMorada1(cliente.getMorada1());
            documento.setDescargaCodPostal(cliente.getCodPostal().getId());
            documento.setDescargaLocalidade(cliente.getLocalidade());
            documento.setDescargaPais(cliente.getPais().getId());
            return;
        }
        documento.setEnvioNome(moradaEnvio.getNome());
        documento.setEnvioMorada(moradaEnvio.getMorada());
        documento.setEnvioMorada1(moradaEnvio.getMorada1());
        documento.setEnvioCodPostal(moradaEnvio.getCodPostal().getId());
        documento.setEnvioLocalidade(moradaEnvio.getLocalidade());
        documento.setEnvioPais(cliente.getPais().getId());
        documento.setDescargaMorada(moradaEnvio.getMorada());
        documento.setDescargaMorada1(moradaEnvio.getMorada1());
        documento.setDescargaCodPostal(moradaEnvio.getCodPostal().getId());
        documento.setDescargaLocalidade(moradaEnvio.getLocalidade());
        documento.setDescargaPais(cliente.getPais().getId());
    }

    private void snapshotCarga(DocumentoComercial documento, Armazem armazem) {
        documento.setCargaNome(armazem.getNome());
        documento.setCargaMorada(armazem.getMorada());
        documento.setCargaMorada1(armazem.getMorada1());
        documento.setCargaCodPostal(armazem.getCodPostal().getId());
        documento.setCargaLocalidade(armazem.getLocalidade());
        documento.setCargaPais(armazem.getPais().getId());
    }

    private DocumentoComercial findDocumento(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Documento comercial não encontrado: " + id));
    }

    private TipoDocumento findTipoDocumento(String id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de documento não encontrado: " + id));
    }

    private void validateSerie(String tipoDocumentoId, String serie) {
        if (!serieRepository.existsById(new SerieId(tipoDocumentoId, serie))) {
            throw new NotFoundException("Série não encontrada: " + tipoDocumentoId + "/" + serie);
        }
    }

    private Cliente findCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
    }

    private Morada findMoradaEnvio(Long id, Cliente cliente) {
        if (id == null) {
            return null;
        }
        Morada morada = moradaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Morada de envio não encontrada: " + id));
        if (!morada.getCliente().getId().equals(cliente.getId())) {
            throw new BadRequestException("Morada de envio não pertence ao cliente do documento");
        }
        return morada;
    }

    private Armazem findArmazem(Long id) {
        return armazemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Armazém de carga não encontrado: " + id));
    }

    private Moeda findMoedaOrDefault(String id, Moeda defaultValue) {
        if (id == null || id.isBlank()) {
            return defaultValue;
        }
        return moedaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moeda não encontrada: " + id));
    }

    private RIva findRIvaOrDefault(String id, RIva defaultValue) {
        if (id == null || id.isBlank()) {
            return defaultValue;
        }
        return rIvaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Regime de IVA não encontrado: " + id));
    }

    private MPagamento findMPagamentoOrDefault(Integer id, MPagamento defaultValue) {
        if (id == null) {
            return defaultValue;
        }
        return mPagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modo de pagamento não encontrado: " + id));
    }

    private PPagamento findPPagamentoOrDefault(String id, PPagamento defaultValue) {
        if (id == null || id.isBlank()) {
            if (defaultValue == null) {
                throw new BadRequestException("Prazo de pagamento é obrigatório no documento");
            }
            return defaultValue;
        }
        return pPagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prazo de pagamento não encontrado: " + id));
    }

    private Transporte findTransporteOrDefault(Integer id, Transporte defaultValue) {
        if (id == null) {
            return defaultValue;
        }
        return transporteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transporte não encontrado: " + id));
    }

    private void validateRascunho(DocumentoComercial documento) {
        if (documento.getEstado() != EstadoDocumentoComercial.RASCUNHO) {
            throw new BadRequestException("Documento emitido não pode ser alterado ou apagado");
        }
    }
    private void validateTemLinhas(DocumentoComercial documento) {
        if (!linhaRepository.existsByDocumentoComercialId(documento.getId())) {
            throw new BadRequestException("Documento comercial deve ter pelo menos uma linha para ser emitido");
        }
    }

    private void validateDataEmissao(DocumentoComercial documento) {
        LocalDate ultimaData = documentoRepository.findUltimaDataEmissao(
                documento.getTipoDocumento().getId(),
                documento.getSerie()
        );
        if (ultimaData != null && documento.getDataEmissao().isBefore(ultimaData)) {
            throw new BadRequestException("Data de emissao nao pode ser anterior ao ultimo documento emitido da serie");
        }
    }

    private Utilizador findEmissor(String id) {
        Utilizador emissor = utilizadorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Emissor nao encontrado: " + id));
        if (emissor.isInativo()) {
            throw new BadRequestException("Emissor inativo nao pode emitir documento comercial");
        }
        return emissor;
    }
}
