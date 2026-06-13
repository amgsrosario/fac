package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialCreateDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialDiagnosticoDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialDiagnosticoPendenteDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialDiagnosticoTotaisDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialEmitirDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialUpdateDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoComercialDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.DocumentoComercialMapper;
import com.ar2lda.fac.mapper.EmpresaMapper;
import com.ar2lda.fac.mapper.LinhaDocumentoComercialMapper;
import com.ar2lda.fac.model.Armazem;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.LinhaDocumentoComercial;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.Morada;
import com.ar2lda.fac.model.PPagamento;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.SerieId;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.ArmazemRepository;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.EmpresaRepository;
import com.ar2lda.fac.repository.LinhaDocumentoComercialRepository;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.MoradaRepository;
import com.ar2lda.fac.repository.PPagamentoRepository;
import com.ar2lda.fac.repository.PendenteRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.SerieRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import com.ar2lda.fac.repository.TransporteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentoComercialService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

    private final DocumentoComercialRepository documentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SerieRepository serieRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final MoradaRepository moradaRepository;
    private final ArmazemRepository armazemRepository;
    private final MoedaRepository moedaRepository;
    private final RIvaRepository rIvaRepository;
    private final MPagamentoRepository mPagamentoRepository;
    private final PPagamentoRepository pPagamentoRepository;
    private final TransporteRepository transporteRepository;
    private final LinhaDocumentoComercialRepository linhaRepository;
    private final CurrentUserService currentUserService;
    private final PendenteRepository pendenteRepository;
    private final SerieService serieService;
    private final PendenteService pendenteService;
    private final DocumentoComercialMapper mapper;
    private final LinhaDocumentoComercialMapper linhaMapper;
    private final EmpresaMapper empresaMapper;

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

    public DocumentoComercialImpressaoDto getImpressao(Long id) {
        DocumentoComercial documento = findDocumento(id);
        Empresa empresa = empresaRepository.findById(Empresa.EMPRESA_ID)
                .orElseThrow(() -> new NotFoundException("Empresa proprietaria nao encontrada"));
        List<LinhaDocumentoComercialDto> linhas = linhaRepository
                .findByDocumentoComercialIdOrderByNumeroLinha(id)
                .stream()
                .map(linhaMapper::toDTO)
                .toList();

        return new DocumentoComercialImpressaoDto(
                empresaMapper.toDTO(empresa),
                mapper.toDTO(documento),
                linhas
        );
    }

    @Transactional
    public void marcarComoImpresso(Long id) {
        DocumentoComercial documento = findDocumento(id);
        documento.setImpresso(true);
    }

    public DocumentoComercialDiagnosticoDto getDiagnostico(Long id) {
        DocumentoComercial documento = findDocumento(id);
        List<LinhaDocumentoComercial> linhas = linhaRepository.findByDocumentoComercialIdOrderByNumeroLinha(id);
        Optional<Pendente> pendente = pendenteRepository.findByDocumentoComercialId(id);

        List<String> alertas = new ArrayList<>();
        List<String> bloqueios = new ArrayList<>();

        if (linhas.isEmpty()) {
            bloqueios.add("Documento comercial nao tem linhas");
        }
        if (documento.getEstado() == EstadoDocumentoComercial.EMITIDO) {
            bloqueios.add("Documento comercial ja se encontra emitido");
        }
        if (documento.isAnulado()) {
            bloqueios.add("Documento comercial encontra-se anulado");
        }
        if (documento.isLiquidado()) {
            bloqueios.add("Documento comercial ja tem liquidacao associada");
        }
        if (documento.getEstado() == EstadoDocumentoComercial.EMITIDO && pendente.isEmpty()
                && !documento.getTipoDocumento().isLiquidacaoImediata() && !documento.isAnulado()) {
            alertas.add("Documento emitido sem pendente associado");
        }

        DocumentoComercialDiagnosticoTotaisDto totais = buildDiagnosticoTotais(documento, linhas);
        if (!totais.coerente()) {
            alertas.add("Totais do cabecalho nao coincidem com os totais calculados pelas linhas");
        }

        boolean podeEmitir = documento.getEstado() == EstadoDocumentoComercial.RASCUNHO
                && !documento.isAnulado()
                && !linhas.isEmpty()
                && totais.coerente();
        boolean podeAnular = documento.getEstado() == EstadoDocumentoComercial.EMITIDO
                && !documento.isAnulado()
                && !documento.isLiquidado()
                && pendente.map(p -> p.getValorPendente().compareTo(p.getValorDocumento()) == 0).orElse(true);

        return new DocumentoComercialDiagnosticoDto(
                documento.getId(),
                buildReferencia(documento),
                documento.getEstado(),
                documento.isAnulado(),
                documento.isImpresso(),
                documento.isLiquidado(),
                !linhas.isEmpty(),
                podeEmitir,
                podeAnular,
                buildDiagnosticoPendente(pendente),
                totais,
                alertas,
                bloqueios
        );
    }

    public String getDiagnosticoHtml(Long id) {
        DocumentoComercialImpressaoDto impressao = getImpressao(id);
        DocumentoComercialDiagnosticoDto diagnostico = getDiagnostico(id);

        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="pt">
                <head>
                  <meta charset="utf-8">
                  <title>Relatorio de conferencia</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 32px; color: #222; }
                    h1, h2 { margin-bottom: 8px; }
                    .aviso { padding: 12px; border: 1px solid #b45309; background: #fff7ed; margin-bottom: 24px; }
                    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
                    .card { border: 1px solid #ddd; padding: 12px; border-radius: 4px; }
                    table { border-collapse: collapse; width: 100%; margin-bottom: 24px; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background: #f3f4f6; }
                    .ok { color: #166534; font-weight: bold; }
                    .erro { color: #991b1b; font-weight: bold; }
                    .muted { color: #666; }
                  </style>
                </head>
                <body>
                """);
        html.append("<h1>Relatorio de conferencia do documento comercial</h1>");
        html.append("<div class=\"aviso\"><strong>Nao e documento fiscal.</strong> Este relatorio serve apenas para conferir dados, linhas, totais, alertas e bloqueios antes do reporting final.</div>");

        html.append("<div class=\"grid\">");
        appendEmpresa(html, impressao.empresa());
        appendDocumento(html, impressao.documento(), diagnostico);
        html.append("</div>");

        appendCliente(html, impressao.documento());
        appendLinhas(html, impressao.linhas());
        appendTotais(html, diagnostico.totais());
        appendPendente(html, diagnostico.pendente());
        appendMensagens(html, "Alertas", diagnostico.alertas());
        appendMensagens(html, "Bloqueios", diagnostico.bloqueios());

        html.append("""
                </body>
                </html>
                """);
        return html.toString();
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
        validateNaoAnulado(documento);
        validateTemLinhas(documento);
        validateDataEmissao(documento);

        Utilizador emissor = currentUserService.resolve(dto.emissorId(), "emitir documento comercial");
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

    @Transactional
    public DocumentoComercialDto anular(Long id) {
        DocumentoComercial documento = findDocumento(id);
        validatePodeAnular(documento);

        pendenteRepository.findByDocumentoComercialId(documento.getId()).ifPresent(pendente -> {
            if (pendente.getValorPendente().compareTo(pendente.getValorDocumento()) != 0) {
                throw new BadRequestException("Documento com pendente movimentado nao pode ser anulado");
            }
            pendenteRepository.delete(pendente);
        });

        documento.setAnulado(true);
        return mapper.toDTO(documentoRepository.save(documento));
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

    private DocumentoComercialDiagnosticoPendenteDto buildDiagnosticoPendente(Optional<Pendente> pendente) {
        return pendente
                .map(p -> new DocumentoComercialDiagnosticoPendenteDto(
                        true,
                        p.getId(),
                        scale6(p.getValorDocumento()),
                        scale6(p.getValorPendente())
                ))
                .orElseGet(() -> new DocumentoComercialDiagnosticoPendenteDto(false, null, ZERO, ZERO));
    }

    private DocumentoComercialDiagnosticoTotaisDto buildDiagnosticoTotais(
            DocumentoComercial documento,
            List<LinhaDocumentoComercial> linhas
    ) {
        BigDecimal linhasValorBruto = ZERO;
        BigDecimal linhasValorDesconto = ZERO;
        BigDecimal linhasValorLinha = ZERO;
        BigDecimal linhasValorIvaTotal = ZERO;
        BigDecimal linhasValorTotal = ZERO;

        for (LinhaDocumentoComercial linha : linhas) {
            BigDecimal valorLinha = scale6(linha.getValorLinha());
            BigDecimal valorIva = valorLinha
                    .multiply(linha.getPercentagemIva())
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

            linhasValorBruto = linhasValorBruto.add(scale6(linha.getValorBruto()));
            linhasValorDesconto = linhasValorDesconto.add(scale6(linha.getValorDesconto()));
            linhasValorLinha = linhasValorLinha.add(valorLinha);
            linhasValorIvaTotal = linhasValorIvaTotal.add(valorIva);
            linhasValorTotal = linhasValorTotal.add(valorLinha).add(valorIva);
        }

        BigDecimal cabecalhoValorLinha = scale6(documento.getValorBruto()).subtract(scale6(documento.getValorDesconto()));
        boolean coerente = same(documento.getValorBruto(), linhasValorBruto)
                && same(documento.getValorDesconto(), linhasValorDesconto)
                && same(cabecalhoValorLinha, linhasValorLinha)
                && same(documento.getValorIvaTotal(), linhasValorIvaTotal)
                && same(documento.getValorTotal(), linhasValorTotal);

        return new DocumentoComercialDiagnosticoTotaisDto(
                scale6(documento.getValorBruto()),
                scale6(linhasValorBruto),
                scale6(documento.getValorDesconto()),
                scale6(linhasValorDesconto),
                scale6(cabecalhoValorLinha),
                scale6(linhasValorLinha),
                scale6(documento.getValorIvaTotal()),
                scale6(linhasValorIvaTotal),
                scale6(documento.getValorTotal()),
                scale6(linhasValorTotal),
                coerente
        );
    }

    private String buildReferencia(DocumentoComercial documento) {
        if (documento.getNumeroDocumento() == null) {
            return documento.getTipoDocumento().getId() + " " + documento.getSerie() + "/RASCUNHO-" + documento.getId();
        }
        return documento.getTipoDocumento().getId() + " " + documento.getSerie() + "/" + documento.getNumeroDocumento();
    }

    private boolean same(BigDecimal left, BigDecimal right) {
        return scale6(left).compareTo(scale6(right)) == 0;
    }

    private BigDecimal scale6(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(6, RoundingMode.HALF_UP);
    }

    private void appendEmpresa(StringBuilder html, com.ar2lda.fac.controller.dto.EmpresaDto empresa) {
        html.append("<section class=\"card\"><h2>Empresa</h2>");
        appendLine(html, "Nome", empresa.nome());
        appendLine(html, "NIF", empresa.nif());
        appendLine(html, "Morada", empresa.morada());
        appendLine(html, "Codigo postal", empresa.codPostalId());
        appendLine(html, "Localidade", empresa.localidade());
        appendLine(html, "Pais", empresa.paisId());
        appendLine(html, "Email", empresa.email());
        html.append("</section>");
    }

    private void appendDocumento(StringBuilder html, DocumentoComercialDto documento, DocumentoComercialDiagnosticoDto diagnostico) {
        html.append("<section class=\"card\"><h2>Documento</h2>");
        appendLine(html, "Referencia", diagnostico.referencia());
        appendLine(html, "Estado", documento.estado());
        appendLine(html, "Data emissao", documento.dataEmissao());
        appendLine(html, "Data vencimento", documento.dataVencimento());
        appendLine(html, "Moeda", documento.moedaId());
        appendLine(html, "Regime IVA", documento.rivaId());
        appendLine(html, "Emissor", documento.emissorId());
        appendLine(html, "Pode emitir", diagnostico.podeEmitir() ? "Sim" : "Nao");
        appendLine(html, "Pode anular", diagnostico.podeAnular() ? "Sim" : "Nao");
        html.append("</section>");
    }

    private void appendCliente(StringBuilder html, DocumentoComercialDto documento) {
        html.append("<section class=\"card\"><h2>Cliente</h2>");
        appendLine(html, "Nome", documento.clienteNome());
        appendLine(html, "NIF", documento.clienteNif());
        appendLine(html, "Morada", documento.clienteMorada());
        appendLine(html, "Codigo postal", documento.clienteCodPostal());
        appendLine(html, "Localidade", documento.clienteLocalidade());
        appendLine(html, "Pais", documento.clientePais());
        html.append("</section>");
    }

    private void appendLinhas(StringBuilder html, List<LinhaDocumentoComercialDto> linhas) {
        html.append("<h2>Linhas</h2>");
        html.append("<table><thead><tr>");
        html.append("<th>#</th><th>Artigo</th><th>Descricao</th><th>Qtd.</th><th>Preco</th><th>Valor linha</th><th>IVA</th>");
        html.append("</tr></thead><tbody>");
        for (LinhaDocumentoComercialDto linha : linhas) {
            html.append("<tr>");
            html.append("<td>").append(escape(linha.numeroLinha())).append("</td>");
            html.append("<td>").append(escape(linha.artigoId())).append("</td>");
            html.append("<td>").append(escape(linha.descricao())).append("</td>");
            html.append("<td>").append(format(linha.quantidade())).append("</td>");
            html.append("<td>").append(format(linha.precoUnitario())).append("</td>");
            html.append("<td>").append(format(linha.valorLinha())).append("</td>");
            html.append("<td>").append(escape(linha.tipoTaxaIvaId())).append(" ").append(format(linha.percentagemIva())).append("%</td>");
            html.append("</tr>");
        }
        html.append("</tbody></table>");
    }

    private void appendTotais(StringBuilder html, DocumentoComercialDiagnosticoTotaisDto totais) {
        html.append("<h2>Totais</h2>");
        html.append("<table><thead><tr><th>Campo</th><th>Cabecalho</th><th>Linhas calculadas</th></tr></thead><tbody>");
        appendTotalRow(html, "Valor bruto", totais.cabecalhoValorBruto(), totais.linhasValorBruto());
        appendTotalRow(html, "Valor desconto", totais.cabecalhoValorDesconto(), totais.linhasValorDesconto());
        appendTotalRow(html, "Valor linha", totais.cabecalhoValorLinha(), totais.linhasValorLinha());
        appendTotalRow(html, "IVA total", totais.cabecalhoValorIvaTotal(), totais.linhasValorIvaTotal());
        appendTotalRow(html, "Total documento", totais.cabecalhoValorTotal(), totais.linhasValorTotal());
        html.append("</tbody></table>");
        html.append("<p>Coerencia dos totais: <span class=\"")
                .append(totais.coerente() ? "ok" : "erro")
                .append("\">")
                .append(totais.coerente() ? "OK" : "Com diferencas")
                .append("</span></p>");
    }

    private void appendPendente(StringBuilder html, DocumentoComercialDiagnosticoPendenteDto pendente) {
        html.append("<section class=\"card\"><h2>Pendente</h2>");
        appendLine(html, "Existe", pendente.existe() ? "Sim" : "Nao");
        appendLine(html, "ID", pendente.id());
        appendLine(html, "Valor documento", format(pendente.valorDocumento()));
        appendLine(html, "Valor pendente", format(pendente.valorPendente()));
        html.append("</section>");
    }

    private void appendMensagens(StringBuilder html, String titulo, List<String> mensagens) {
        html.append("<section class=\"card\"><h2>").append(escape(titulo)).append("</h2>");
        if (mensagens.isEmpty()) {
            html.append("<p class=\"muted\">Sem ").append(escape(titulo.toLowerCase())).append(".</p>");
        } else {
            html.append("<ul>");
            for (String mensagem : mensagens) {
                html.append("<li>").append(escape(mensagem)).append("</li>");
            }
            html.append("</ul>");
        }
        html.append("</section>");
    }

    private void appendLine(StringBuilder html, String label, Object value) {
        html.append("<p><strong>")
                .append(escape(label))
                .append(":</strong> ")
                .append(escape(value))
                .append("</p>");
    }

    private void appendTotalRow(StringBuilder html, String label, BigDecimal cabecalho, BigDecimal linhas) {
        html.append("<tr><td>")
                .append(escape(label))
                .append("</td><td>")
                .append(format(cabecalho))
                .append("</td><td>")
                .append(format(linhas))
                .append("</td></tr>");
    }

    private String format(BigDecimal value) {
        return scale6(value).toPlainString();
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void validateNaoAnulado(DocumentoComercial documento) {
        if (documento.isAnulado()) {
            throw new BadRequestException("Documento comercial anulado nao pode ser alterado");
        }
    }

    private void validatePodeAnular(DocumentoComercial documento) {
        if (documento.getEstado() != EstadoDocumentoComercial.EMITIDO) {
            throw new BadRequestException("Apenas documentos emitidos podem ser anulados");
        }
        if (documento.isAnulado()) {
            throw new BadRequestException("Documento comercial ja se encontra anulado");
        }
        if (documento.isLiquidado()) {
            throw new BadRequestException("Documento liquidado nao pode ser anulado");
        }
    }
}
