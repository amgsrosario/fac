package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroCreateDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDiagnosticoDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDiagnosticoTotaisDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoFinanceiroCreateDto;
import com.ar2lda.fac.controller.dto.LinhaDocumentoFinanceiroDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.DocumentoFinanceiroMapper;
import com.ar2lda.fac.mapper.EmpresaMapper;
import com.ar2lda.fac.mapper.ClienteMapper;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.DocumentoFinanceiro;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.model.LinhaDocumentoFinanceiro;
import com.ar2lda.fac.model.MPagamento;
import com.ar2lda.fac.model.Moeda;
import com.ar2lda.fac.model.Pendente;
import com.ar2lda.fac.model.SerieId;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.EmpresaRepository;
import com.ar2lda.fac.repository.LinhaDocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.PendenteRepository;
import com.ar2lda.fac.repository.SerieRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoFinanceiroService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

    private final DocumentoFinanceiroRepository documentoRepository;
    private final EmpresaRepository empresaRepository;
    private final LinhaDocumentoFinanceiroRepository linhaRepository;
    private final PendenteRepository pendenteRepository;
    private final ClienteRepository clienteRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SerieRepository serieRepository;
    private final MoedaRepository moedaRepository;
    private final MPagamentoRepository mPagamentoRepository;
    private final CurrentUserService currentUserService;
    private final SerieService serieService;
    private final AtcudService atcudService;
    private final DocumentoFinanceiroMapper mapper;
    private final EmpresaMapper empresaMapper;
    private final ClienteMapper clienteMapper;

    @Transactional
    public DocumentoFinanceiroDto create(DocumentoFinanceiroCreateDto dto) {
        Cliente cliente = findCliente(dto.clienteId());
        TipoDocumento tipoDocumento = findTipoDocumento(dto.tipoDocumentoId());
        validateSerie(dto.tipoDocumentoId(), dto.serie());
        validateDataEmissao(dto.tipoDocumentoId(), dto.serie(), dto.dataEmissao());
        Moeda moeda = findMoeda(dto.moedaId());
        MPagamento mPagamento = findMPagamento(dto.mPagamentoId());
        Utilizador emissor = currentUserService.resolve(dto.emissorId(), "emitir documento financeiro");

        DocumentoFinanceiro documento = new DocumentoFinanceiro();
        documento.setCliente(cliente);
        documento.setTipoDocumento(tipoDocumento);
        documento.setSerie(dto.serie());
        SerieNumeracao numeracao = serieService.proximoNumeroParaEmissao(dto.tipoDocumentoId(), dto.serie());
        documento.setNumeroDocumento(numeracao.numeroSequencial());
        documento.atribuirAtcud(
                numeracao.codigoValidacaoAt(),
                atcudService.gerar(numeracao.codigoValidacaoAt(), numeracao.numeroSequencial())
        );
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

    public DocumentoFinanceiroImpressaoDto getImpressao(Long id) {
        DocumentoFinanceiro documento = findDocumento(id);
        Empresa empresa = empresaRepository.findById(Empresa.EMPRESA_ID)
                .orElseThrow(() -> new NotFoundException("Empresa proprietaria nao encontrada"));

        return new DocumentoFinanceiroImpressaoDto(
                empresaMapper.toDTO(empresa),
                clienteMapper.toDTO(documento.getCliente()),
                toDTO(documento)
        );
    }

    @Transactional
    public void marcarComoImpresso(Long id) {
        DocumentoFinanceiro documento = findDocumento(id);
        documento.setImpresso(true);
    }

    public DocumentoFinanceiroDiagnosticoDto getDiagnostico(Long id) {
        DocumentoFinanceiro documento = findDocumento(id);
        List<LinhaDocumentoFinanceiro> linhas = linhaRepository.findByDocumentoFinanceiroIdOrderByNumeroLinha(documento.getId());

        BigDecimal linhasValorALiquidar = ZERO;
        BigDecimal linhasValorDesconto = ZERO;
        BigDecimal linhasValorPagamentoLiquido = ZERO;
        boolean temMovimentosPosteriores = false;

        for (LinhaDocumentoFinanceiro linha : linhas) {
            linhasValorALiquidar = linhasValorALiquidar.add(linha.getValorALiquidar());
            linhasValorDesconto = linhasValorDesconto.add(linha.getDescontoValor());
            linhasValorPagamentoLiquido = linhasValorPagamentoLiquido.add(linha.getValorPagamentoLiquido());
            if (linha.getPendente().getValorPendente().compareTo(linha.getNovoValorPendente()) != 0) {
                temMovimentosPosteriores = true;
            }
        }

        DocumentoFinanceiroDiagnosticoTotaisDto totais = new DocumentoFinanceiroDiagnosticoTotaisDto(
                scale6(documento.getValorPagamentoBruto()),
                scale6(linhasValorALiquidar),
                scale6(documento.getValorDescontoFinanceiro()),
                scale6(linhasValorDesconto),
                scale6(documento.getValorPagamentoLiquido()),
                scale6(linhasValorPagamentoLiquido),
                sameAmount(documento.getValorPagamentoBruto(), linhasValorALiquidar)
                        && sameAmount(documento.getValorDescontoFinanceiro(), linhasValorDesconto)
                        && sameAmount(documento.getValorPagamentoLiquido(), linhasValorPagamentoLiquido)
        );

        List<String> alertas = new ArrayList<>();
        List<String> bloqueios = new ArrayList<>();

        if (linhas.isEmpty()) {
            bloqueios.add("Documento financeiro sem linhas");
        }
        if (!totais.coerente()) {
            alertas.add("Totais do cabecalho diferem dos totais calculados pelas linhas");
        }
        if (documento.isAnulado()) {
            bloqueios.add("Documento financeiro anulado");
        }
        if (temMovimentosPosteriores && !documento.isAnulado()) {
            bloqueios.add("Documento financeiro nao pode ser anulado porque ha movimentos posteriores nos pendentes");
        }

        return new DocumentoFinanceiroDiagnosticoDto(
                documento.getId(),
                referencia(documento),
                documento.isAnulado(),
                documento.isImpresso(),
                !linhas.isEmpty(),
                bloqueios.isEmpty(),
                totais,
                alertas,
                bloqueios
        );
    }

    public String getDiagnosticoHtml(Long id) {
        DocumentoFinanceiroImpressaoDto impressao = getImpressao(id);
        DocumentoFinanceiroDiagnosticoDto diagnostico = getDiagnostico(id);

        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="pt">
                <head>
                  <meta charset="utf-8">
                  <title>Relatorio de conferencia do documento financeiro</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 32px; color: #222; }
                    h1 { margin-bottom: 4px; }
                    h2 { margin-top: 28px; border-bottom: 1px solid #ddd; padding-bottom: 6px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 12px; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background: #f3f3f3; }
                    .aviso { background: #fff3cd; border: 1px solid #ffe69c; padding: 12px; margin: 16px 0; }
                    .grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
                    .card { border: 1px solid #ddd; padding: 12px; }
                    .label { font-weight: bold; }
                    .ok { color: #146c43; font-weight: bold; }
                    .erro { color: #b02a37; font-weight: bold; }
                  </style>
                </head>
                <body>
                <h1>Relatorio de conferencia do documento financeiro</h1>
                <div class="aviso">Nao e documento fiscal. Este relatorio serve apenas para validar dados, linhas, totais e pendentes antes de implementar a impressao definitiva.</div>
                """);

        appendEmpresa(html, impressao.empresa());
        appendDocumento(html, impressao.documento(), diagnostico);
        appendLinhas(html, impressao.documento().linhas());
        appendTotais(html, diagnostico.totais());
        appendMensagens(html, "Alertas", diagnostico.alertas());
        appendMensagens(html, "Bloqueios", diagnostico.bloqueios());

        html.append("""
                </body>
                </html>
                """);
        return html.toString();
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

    private void validateDataEmissao(String tipoDocumentoId, String serie, java.time.LocalDate dataEmissao) {
        java.time.LocalDate ultimaData = documentoRepository.findUltimaDataEmissao(tipoDocumentoId, serie);
        if (ultimaData != null && dataEmissao.isBefore(ultimaData)) {
            throw new BadRequestException("Data de emissao nao pode ser anterior ao ultimo documento financeiro emitido da serie");
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

    private Pendente findPendente(Long id) {
        return pendenteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pendente nao encontrado: " + id));
    }

    private BigDecimal scale6(BigDecimal value) {
        return value.setScale(6, RoundingMode.HALF_UP);
    }

    private boolean sameAmount(BigDecimal first, BigDecimal second) {
        return scale6(first).compareTo(scale6(second)) == 0;
    }

    private String referencia(DocumentoFinanceiro documento) {
        return documento.getTipoDocumento().getId() + " " + documento.getSerie() + "/" + documento.getNumeroDocumento();
    }

    private void appendEmpresa(StringBuilder html, com.ar2lda.fac.controller.dto.EmpresaDto empresa) {
        html.append("<h2>Empresa</h2><div class=\"card\">");
        appendLine(html, "Nome", empresa.nome());
        appendLine(html, "NIF", empresa.nif());
        appendLine(html, "Morada", empresa.morada());
        appendLine(html, "Codigo postal", empresa.codPostalId());
        appendLine(html, "Localidade", empresa.localidade());
        appendLine(html, "Pais", empresa.paisId());
        html.append("</div>");
    }

    private void appendDocumento(StringBuilder html, DocumentoFinanceiroDto documento, DocumentoFinanceiroDiagnosticoDto diagnostico) {
        html.append("<h2>Documento</h2><div class=\"grid\"><div class=\"card\">");
        appendLine(html, "Referencia", diagnostico.referencia());
        appendLine(html, "Cliente", documento.clienteId());
        appendLine(html, "Data emissao", documento.dataEmissao());
        appendLine(html, "Moeda", documento.moedaId());
        appendLine(html, "Modo pagamento", documento.mPagamentoId());
        appendLine(html, "Emissor", documento.emissorId());
        html.append("</div><div class=\"card\">");
        appendLine(html, "Anulado", diagnostico.anulado() ? "Sim" : "Nao");
        appendLine(html, "Impresso", diagnostico.impresso() ? "Sim" : "Nao");
        appendLine(html, "Tem linhas", diagnostico.temLinhas() ? "Sim" : "Nao");
        appendLine(html, "Pode anular", diagnostico.podeAnular() ? "Sim" : "Nao");
        appendLine(html, "Data/hora operacao", documento.dataHoraOperacao());
        appendLine(html, "Momento emissao", documento.momentoEmissao());
        html.append("</div></div>");
    }

    private void appendLinhas(StringBuilder html, List<LinhaDocumentoFinanceiroDto> linhas) {
        html.append("<h2>Linhas</h2><table><thead><tr>");
        html.append("<th>#</th><th>Pendente</th><th>Documento</th><th>Data</th><th>Vencimento</th>");
        html.append("<th>Valor doc.</th><th>Pendente antes</th><th>A liquidar</th><th>Desconto</th><th>Liquido</th><th>Novo pendente</th>");
        html.append("</tr></thead><tbody>");
        for (LinhaDocumentoFinanceiroDto linha : linhas) {
            html.append("<tr>");
            html.append("<td>").append(escape(linha.numeroLinha())).append("</td>");
            html.append("<td>").append(escape(linha.pendenteId())).append("</td>");
            html.append("<td>").append(escape(linha.tipoDocumentoId())).append(" ")
                    .append(escape(linha.serieDocumento())).append("/")
                    .append(escape(linha.numeroDocumento())).append("</td>");
            html.append("<td>").append(escape(linha.dataDocumento())).append("</td>");
            html.append("<td>").append(escape(linha.dataVencimento())).append("</td>");
            html.append("<td>").append(escape(format(linha.valorDocumento()))).append("</td>");
            html.append("<td>").append(escape(format(linha.valorPendenteAntes()))).append("</td>");
            html.append("<td>").append(escape(format(linha.valorALiquidar()))).append("</td>");
            html.append("<td>").append(escape(format(linha.descontoValor()))).append("</td>");
            html.append("<td>").append(escape(format(linha.valorPagamentoLiquido()))).append("</td>");
            html.append("<td>").append(escape(format(linha.novoValorPendente()))).append("</td>");
            html.append("</tr>");
        }
        html.append("</tbody></table>");
    }

    private void appendTotais(StringBuilder html, DocumentoFinanceiroDiagnosticoTotaisDto totais) {
        html.append("<h2>Totais</h2><table><thead><tr><th>Campo</th><th>Cabecalho</th><th>Linhas</th></tr></thead><tbody>");
        appendTotalRow(html, "Valor pagamento bruto", totais.cabecalhoValorPagamentoBruto(), totais.linhasValorALiquidar());
        appendTotalRow(html, "Valor desconto financeiro", totais.cabecalhoValorDescontoFinanceiro(), totais.linhasValorDesconto());
        appendTotalRow(html, "Valor pagamento liquido", totais.cabecalhoValorPagamentoLiquido(), totais.linhasValorPagamentoLiquido());
        html.append("</tbody></table>");
        html.append("<p>Coerencia dos totais: <span class=\"")
                .append(totais.coerente() ? "ok" : "erro")
                .append("\">")
                .append(totais.coerente() ? "OK" : "DIFERENCAS")
                .append("</span></p>");
    }

    private void appendMensagens(StringBuilder html, String titulo, List<String> mensagens) {
        html.append("<h2>").append(escape(titulo)).append("</h2>");
        if (mensagens.isEmpty()) {
            html.append("<p class=\"ok\">Sem ").append(escape(titulo.toLowerCase())).append(".</p>");
            return;
        }
        html.append("<ul>");
        for (String mensagem : mensagens) {
            html.append("<li>").append(escape(mensagem)).append("</li>");
        }
        html.append("</ul>");
    }

    private void appendLine(StringBuilder html, String label, Object value) {
        html.append("<p><span class=\"label\">")
                .append(escape(label))
                .append(":</span> ")
                .append(escape(value))
                .append("</p>");
    }

    private void appendTotalRow(StringBuilder html, String label, BigDecimal cabecalho, BigDecimal linhas) {
        html.append("<tr><td>")
                .append(escape(label))
                .append("</td><td>")
                .append(escape(format(cabecalho)))
                .append("</td><td>")
                .append(escape(format(linhas)))
                .append("</td></tr>");
    }

    private String format(BigDecimal value) {
        return value == null ? "" : value.setScale(6, RoundingMode.HALF_UP).toPlainString();
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
}
