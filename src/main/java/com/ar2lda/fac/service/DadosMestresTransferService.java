package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.ArtigoCreateDto;
import com.ar2lda.fac.controller.dto.ClienteCreateDto;
import com.ar2lda.fac.controller.dto.ImportacaoErroDto;
import com.ar2lda.fac.controller.dto.ImportacaoResultadoDto;
import com.ar2lda.fac.controller.dto.ImportacaoResumoDto;
import com.ar2lda.fac.controller.dto.ImportacaoValidacaoDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.model.Artigo;
import com.ar2lda.fac.model.Cliente;
import com.ar2lda.fac.model.EstadoImportacaoDadosMestres;
import com.ar2lda.fac.model.ImportacaoDadosMestres;
import com.ar2lda.fac.model.ResultadoAuditoria;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import com.ar2lda.fac.model.TipoDadosMestres;
import com.ar2lda.fac.repository.ArtigoRepository;
import com.ar2lda.fac.repository.ClienteRepository;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.FamiliaRepository;
import com.ar2lda.fac.repository.ImportacaoDadosMestresRepository;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.repository.MoedaRepository;
import com.ar2lda.fac.repository.PaisRepository;
import com.ar2lda.fac.repository.PPagamentoRepository;
import com.ar2lda.fac.repository.RIvaRepository;
import com.ar2lda.fac.repository.TipoTaxaIvaRepository;
import com.ar2lda.fac.repository.TransporteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DadosMestresTransferService {

    private static final long MAX_BYTES = 10L * 1024L * 1024L;
    private static final int MAX_ROWS = 10_000;
    private static final int MAX_COLUMNS = 100;
    private static final int PREVIEW_LIMIT = 20;
    private static final int SESSION_TTL_MINUTES = 30;

    private static final List<String> CLIENTE_HEADERS = List.of("nome", "morada", "morada1", "localidade", "nif",
            "tel", "tm", "email", "email1", "tspiva", "iban", "retencao", "inativo", "observacoes",
            "codPostalId", "paisId", "moedaId", "mPagamentoId", "pPagamentoId", "rivaId", "transporteId");

    private static final List<String> ARTIGO_HEADERS = List.of("codigo", "abreviatura", "codigoIdentificacao",
            "descricao", "unidade", "familiaId", "peso", "ivaCompraId", "ivaVendaId", "pvp", "inativo",
            "retencao", "observacoes");

    private final ImportacaoDadosMestresRepository importacaoRepository;
    private final ClienteRepository clienteRepository;
    private final ArtigoRepository artigoRepository;
    private final PaisRepository paisRepository;
    private final CodPostalRepository codPostalRepository;
    private final MoedaRepository moedaRepository;
    private final MPagamentoRepository mPagamentoRepository;
    private final PPagamentoRepository pPagamentoRepository;
    private final RIvaRepository rIvaRepository;
    private final TransporteRepository transporteRepository;
    private final FamiliaRepository familiaRepository;
    private final TipoTaxaIvaRepository tipoTaxaIvaRepository;
    private final ClienteService clienteService;
    private final ArtigoService artigoService;
    private final AuditoriaService auditoriaService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public ImportacaoValidacaoDto validar(TipoDadosMestres tipo, MultipartFile file) {
        validateFile(file);
        String formato = format(file.getOriginalFilename(), file.getContentType());
        auditoriaService.registar(TipoAuditoriaEvento.IMPORTACAO_VALIDACAO_RECEBIDA, "IMPORTACAO", tipo,
                "Ficheiro recebido para validação", dados(tipo, file.getOriginalFilename(), 0, 0, 0));

        ParsedFile parsed = parse(file, formato);
        validateParsedHeaders(tipo, parsed);
        ValidationResult validation = validateRows(tipo, parsed.rows());
        OffsetDateTime now = OffsetDateTime.now(clock);

        ImportacaoDadosMestres entity = new ImportacaoDadosMestres();
        entity.setId(UUID.randomUUID());
        entity.setTipo(tipo);
        entity.setEstado(EstadoImportacaoDadosMestres.VALIDADA);
        entity.setNomeFicheiro(safeFilename(file.getOriginalFilename()));
        entity.setFormato(formato);
        entity.setPayloadJson(toJson(parsed.rows()));
        entity.setTotalLinhas(validation.resumo().totalLinhas());
        entity.setLinhasValidas(validation.resumo().linhasValidas());
        entity.setLinhasComErro(validation.resumo().linhasComErro());
        entity.setLinhasComAviso(validation.resumo().linhasComAviso());
        entity.setRegistosNovos(validation.resumo().registosNovos());
        entity.setDuplicados(validation.resumo().duplicados());
        entity.setLinhasIgnoradas(validation.resumo().linhasIgnoradas());
        entity.setCriadoEm(now);
        entity.setExpiraEm(now.plusMinutes(SESSION_TTL_MINUTES));
        entity.setCriadoPor(currentUserService.currentCodeOrSystem());
        importacaoRepository.save(entity);

        auditoriaService.registar(TipoAuditoriaEvento.IMPORTACAO_VALIDACAO_CONCLUIDA, "IMPORTACAO", entity.getId(),
                "Validação de importação concluída", dados(tipo, file.getOriginalFilename(), validation.resumo()));
        return toValidacaoDto(entity, validation);
    }

    @Transactional
    public ImportacaoResultadoDto confirmar(TipoDadosMestres tipo, UUID id) {
        ImportacaoDadosMestres entity = importacaoRepository.findWithLockById(id)
                .orElseThrow(() -> new NotFoundException("Importação não encontrada: " + id));
        if (entity.getTipo() != tipo) {
            throw new BadRequestException("Tipo de importação incompatível");
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (entity.getEstado() != EstadoImportacaoDadosMestres.VALIDADA) {
            throw new ConflictException("Importação já não está disponível para confirmação");
        }
        if (entity.getExpiraEm().isBefore(now)) {
            entity.setEstado(EstadoImportacaoDadosMestres.EXPIRADA);
            throw new ConflictException("Importação expirada");
        }

        List<Map<String, String>> rows = fromJson(entity.getPayloadJson());
        ValidationResult validation = validateRows(tipo, rows);
        if (!validation.errors().isEmpty()) {
            auditoriaService.registar(TipoAuditoriaEvento.IMPORTACAO_FALHADA, "IMPORTACAO", id,
                    "Confirmação recusada por erros de validação", dados(tipo, entity.getNomeFicheiro(), validation.resumo()));
            return new ImportacaoResultadoDto(id, tipo, entity.getEstado(), validation.resumo(), 0,
                    validation.resumo().linhasComErro(), validation.resumo().linhasIgnoradas(),
                    validation.errors(), validation.warnings());
        }

        int created = 0;
        for (ValidatedRow row : validation.validRows()) {
            if (tipo == TipoDadosMestres.CLIENTES) {
                clienteService.create(toCliente(row.values()));
            } else {
                artigoService.create(toArtigo(row.values()));
            }
            created++;
        }
        entity.setEstado(EstadoImportacaoDadosMestres.CONFIRMADA);
        entity.setConfirmadoEm(now);
        entity.setConfirmadoPor(currentUserService.currentCodeOrSystem());
        entity.setLinhasValidas(validation.resumo().linhasValidas());
        entity.setLinhasComErro(validation.resumo().linhasComErro());
        entity.setLinhasComAviso(validation.resumo().linhasComAviso());
        entity.setRegistosNovos(validation.resumo().registosNovos());
        entity.setDuplicados(validation.resumo().duplicados());
        entity.setLinhasIgnoradas(validation.resumo().linhasIgnoradas());

        TipoAuditoriaEvento evento = validation.resumo().linhasComErro() == 0
                ? TipoAuditoriaEvento.IMPORTACAO_CONCLUIDA
                : TipoAuditoriaEvento.IMPORTACAO_PARCIAL;
        auditoriaService.registar(evento, "IMPORTACAO", id, "Importação confirmada",
                dados(tipo, entity.getNomeFicheiro(), validation.resumo()));
        return new ImportacaoResultadoDto(id, tipo, entity.getEstado(), validation.resumo(), created,
                validation.resumo().linhasComErro(), validation.resumo().linhasIgnoradas(),
                validation.errors(), validation.warnings());
    }

    @Transactional
    public void cancelar(TipoDadosMestres tipo, UUID id) {
        ImportacaoDadosMestres entity = importacaoRepository.findWithLockById(id)
                .orElseThrow(() -> new NotFoundException("Importação não encontrada: " + id));
        if (entity.getTipo() != tipo) throw new BadRequestException("Tipo de importação incompatível");
        if (entity.getEstado() != EstadoImportacaoDadosMestres.VALIDADA) {
            throw new ConflictException("Importação já não pode ser cancelada");
        }
        entity.setEstado(EstadoImportacaoDadosMestres.CANCELADA);
        auditoriaService.registar(TipoAuditoriaEvento.IMPORTACAO_CANCELADA, "IMPORTACAO", id,
                "Importação cancelada", dados(tipo, entity.getNomeFicheiro(), entity.getTotalLinhas(), 0, 0));
    }

    @Transactional
    public ExportedFile modelo(TipoDadosMestres tipo, String formato) {
        List<String> headers = headers(tipo);
        List<Map<String, String>> rows = List.of(example(tipo));
        return exportRows("modelo-importacao-" + tipo.name().toLowerCase(Locale.ROOT), normalizedExportFormat(formato), headers, rows);
    }

    @Transactional
    public ExportedFile exportar(TipoDadosMestres tipo, String formato, Boolean ativos) {
        List<String> headers = headers(tipo);
        List<Map<String, String>> rows = tipo == TipoDadosMestres.CLIENTES ? exportClientes(ativos) : exportArtigos(ativos);
        auditoriaService.registar(TipoAuditoriaEvento.EXPORTACAO_DADOS_MESTRES, "EXPORTACAO", tipo,
                "Exportação de dados mestres", dados(tipo, "exportacao", rows.size(), rows.size(), 0));
        return exportRows("exportacao-" + tipo.name().toLowerCase(Locale.ROOT) + "-" + OffsetDateTime.now(clock).toLocalDate(),
                normalizedExportFormat(formato), headers, rows);
    }

    private ValidationResult validateRows(TipoDadosMestres tipo, List<Map<String, String>> rows) {
        List<ImportacaoErroDto> errors = new ArrayList<>();
        List<ImportacaoErroDto> warnings = new ArrayList<>();
        List<ValidatedRow> validRows = new ArrayList<>();
        Set<String> seenPrimary = new HashSet<>();
        Set<String> seenSecondary = new HashSet<>();
        int duplicates = 0;
        int ignored = 0;

        for (int i = 0; i < rows.size(); i++) {
            int line = i + 2;
            Map<String, String> row = rows.get(i);
            if (row.values().stream().allMatch(value -> value == null || value.isBlank())) {
                ignored++;
                continue;
            }
            int before = errors.size();
            if (tipo == TipoDadosMestres.CLIENTES) {
                duplicates += validateCliente(line, row, errors, warnings, seenPrimary);
            } else {
                duplicates += validateArtigo(line, row, errors, warnings, seenPrimary, seenSecondary);
            }
            if (errors.size() == before) {
                validRows.add(new ValidatedRow(line, row));
            }
        }
        int errorLines = (int) errors.stream().map(ImportacaoErroDto::linha).distinct().count();
        int warningLines = (int) warnings.stream().map(ImportacaoErroDto::linha).distinct().count();
        ImportacaoResumoDto resumo = new ImportacaoResumoDto(rows.size(), validRows.size(), errorLines,
                warningLines, validRows.size(), duplicates, ignored);
        return new ValidationResult(resumo, errors, warnings, validRows);
    }

    private int validateCliente(int line, Map<String, String> row, List<ImportacaoErroDto> errors,
                                List<ImportacaoErroDto> warnings, Set<String> seenNifs) {
        int duplicates = 0;
        required(line, row, errors, "nome", "CLIENTE_NOME_OBRIGATORIO");
        required(line, row, errors, "morada", "CLIENTE_MORADA_OBRIGATORIA");
        required(line, row, errors, "nif", "CLIENTE_NIF_OBRIGATORIO");
        required(line, row, errors, "email", "CLIENTE_EMAIL_OBRIGATORIO");
        required(line, row, errors, "codPostalId", "CLIENTE_CODPOSTAL_OBRIGATORIO");
        required(line, row, errors, "paisId", "CLIENTE_PAIS_OBRIGATORIO");
        required(line, row, errors, "moedaId", "CLIENTE_MOEDA_OBRIGATORIA");
        required(line, row, errors, "transporteId", "CLIENTE_TRANSPORTE_OBRIGATORIO");
        max(line, row, errors, "nome", 80, "CLIENTE_NOME_TAMANHO");
        max(line, row, errors, "morada", 60, "CLIENTE_MORADA_TAMANHO");
        max(line, row, errors, "email", 120, "CLIENTE_EMAIL_TAMANHO");
        validateEmail(line, row, errors, "email", "CLIENTE_EMAIL_INVALIDO");
        validateEmail(line, row, errors, "email1", "CLIENTE_EMAIL_INVALIDO");
        String nif = value(row, "nif");
        if (!nif.isBlank()) {
            if (nif.length() != 9 || !nif.chars().allMatch(Character::isDigit)
                    || ("PT".equalsIgnoreCase(value(row, "paisId")) && !validPtNif(nif))) {
                errors.add(issue(line, "nif", nif, "CLIENTE_NIF_INVALIDO", "NIF inválido"));
            } else if (!seenNifs.add(nif)) {
                duplicates++;
                errors.add(issue(line, "nif", nif, "CLIENTE_NIF_DUPLICADO_FICHEIRO", "NIF duplicado no ficheiro"));
            } else if (clienteRepository.existsByNif(nif)) {
                duplicates++;
                errors.add(issue(line, "nif", nif, "CLIENTE_NIF_DUPLICADO_BASE", "NIF já existe na base"));
            }
        }
        exists(line, row, errors, "paisId", "CLIENTE_PAIS_INEXISTENTE", id -> paisRepository.existsById(id));
        exists(line, row, errors, "codPostalId", "CLIENTE_CODPOSTAL_INEXISTENTE", id -> codPostalRepository.existsById(id));
        exists(line, row, errors, "moedaId", "CLIENTE_MOEDA_INEXISTENTE", id -> moedaRepository.existsById(id));
        exists(line, row, errors, "rivaId", "CLIENTE_RIVA_INEXISTENTE", id -> rIvaRepository.existsById(id));
        integerExists(line, row, errors, "mPagamentoId", "CLIENTE_MPAGAMENTO_INVALIDO", id -> mPagamentoRepository.existsById(id));
        exists(line, row, errors, "pPagamentoId", "CLIENTE_PPAGAMENTO_INEXISTENTE", id -> pPagamentoRepository.existsById(id));
        integerExists(line, row, errors, "transporteId", "CLIENTE_TRANSPORTE_INVALIDO", id -> transporteRepository.existsById(id));
        parseBoolean(line, row, errors, "retencao");
        parseBoolean(line, row, errors, "inativo");
        if (!value(row, "email1").isBlank() && value(row, "email1").equalsIgnoreCase(value(row, "email"))) {
            warnings.add(issue(line, "email1", value(row, "email1"), "CLIENTE_EMAIL_DUPLICADO_LINHA", "Email alternativo igual ao principal"));
        }
        return duplicates;
    }

    private int validateArtigo(int line, Map<String, String> row, List<ImportacaoErroDto> errors,
                               List<ImportacaoErroDto> warnings, Set<String> seenCodes, Set<String> seenIdentifiers) {
        int duplicates = 0;
        required(line, row, errors, "codigo", "ARTIGO_CODIGO_OBRIGATORIO");
        required(line, row, errors, "descricao", "ARTIGO_DESCRICAO_OBRIGATORIA");
        required(line, row, errors, "unidade", "ARTIGO_UNIDADE_OBRIGATORIA");
        required(line, row, errors, "familiaId", "ARTIGO_FAMILIA_OBRIGATORIA");
        required(line, row, errors, "ivaCompraId", "ARTIGO_IVA_COMPRA_OBRIGATORIO");
        required(line, row, errors, "ivaVendaId", "ARTIGO_IVA_VENDA_OBRIGATORIO");
        required(line, row, errors, "pvp", "ARTIGO_PVP_OBRIGATORIO");
        max(line, row, errors, "codigo", 50, "ARTIGO_CODIGO_TAMANHO");
        max(line, row, errors, "descricao", 80, "ARTIGO_DESCRICAO_TAMANHO");
        max(line, row, errors, "unidade", 3, "ARTIGO_UNIDADE_TAMANHO");
        String codigo = value(row, "codigo").toUpperCase(Locale.ROOT);
        if (!codigo.isBlank()) {
            if (!codigo.matches("^[A-Z0-9]{1,50}$")) {
                errors.add(issue(line, "codigo", codigo, "ARTIGO_CODIGO_INVALIDO", "Código deve conter apenas letras maiúsculas e números"));
            } else if (!seenCodes.add(codigo)) {
                duplicates++;
                errors.add(issue(line, "codigo", codigo, "ARTIGO_CODIGO_DUPLICADO_FICHEIRO", "Código duplicado no ficheiro"));
            } else if (artigoRepository.existsById(codigo)) {
                duplicates++;
                errors.add(issue(line, "codigo", codigo, "ARTIGO_CODIGO_DUPLICADO_BASE", "Código já existe na base"));
            }
        }
        String codigoIdentificacao = value(row, "codigoIdentificacao");
        if (!codigoIdentificacao.isBlank()) {
            if (!seenIdentifiers.add(codigoIdentificacao)) {
                duplicates++;
                errors.add(issue(line, "codigoIdentificacao", codigoIdentificacao, "ARTIGO_IDENTIFICACAO_DUPLICADA_FICHEIRO", "Código de identificação duplicado no ficheiro"));
            } else if (artigoRepository.existsByCodigoIdentificacao(codigoIdentificacao)) {
                duplicates++;
                errors.add(issue(line, "codigoIdentificacao", codigoIdentificacao, "ARTIGO_IDENTIFICACAO_DUPLICADA_BASE", "Código de identificação já existe na base"));
            }
        }
        integerExists(line, row, errors, "familiaId", "ARTIGO_FAMILIA_INVALIDA", id -> familiaRepository.existsById(Long.valueOf(id)));
        exists(line, row, errors, "ivaCompraId", "ARTIGO_IVA_INEXISTENTE", id -> tipoTaxaIvaRepository.existsById(id));
        exists(line, row, errors, "ivaVendaId", "ARTIGO_IVA_INEXISTENTE", id -> tipoTaxaIvaRepository.existsById(id));
        decimal(line, row, errors, "peso", 3, false, "ARTIGO_PESO_INVALIDO");
        decimal(line, row, errors, "pvp", 6, true, "ARTIGO_PRECO_INVALIDO");
        parseBoolean(line, row, errors, "retencao");
        parseBoolean(line, row, errors, "inativo");
        if (value(row, "descricao").equalsIgnoreCase(value(row, "abreviatura"))) {
            warnings.add(issue(line, "abreviatura", value(row, "abreviatura"), "ARTIGO_ABREVIATURA_IGUAL_DESCRICAO", "Abreviatura igual à descrição"));
        }
        return duplicates;
    }

    private ParsedFile parse(MultipartFile file, String formato) {
        try {
            return "xlsx".equals(formato) ? parseXlsx(file.getBytes()) : parseCsv(file.getBytes());
        } catch (IOException exception) {
            throw new BadRequestException("Ficheiro inválido ou ilegível");
        }
    }

    private ParsedFile parseCsv(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        if (text.startsWith("\uFEFF")) text = text.substring(1);
        List<List<String>> records = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(ch);
                }
            } else if (ch == '"') {
                quoted = true;
            } else if (ch == ';') {
                current.add(field.toString());
                field.setLength(0);
            } else if (ch == '\n') {
                current.add(trimCr(field.toString()));
                field.setLength(0);
                records.add(current);
                current = new ArrayList<>();
            } else {
                field.append(ch);
            }
        }
        if (quoted) throw new BadRequestException("CSV malformado: aspas não terminadas");
        current.add(trimCr(field.toString()));
        if (!(current.size() == 1 && current.getFirst().isBlank())) records.add(current);
        return rowsFromRecords(records);
    }

    private ParsedFile parseXlsx(byte[] bytes) throws IOException {
        ZipSecureFile.setMinInflateRatio(0.01d);
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            if (workbook.getNumberOfSheets() < 1) throw new BadRequestException("XLSX sem folhas");
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() > MAX_ROWS) throw new BadRequestException("Número máximo de linhas excedido");
            List<List<String>> records = new ArrayList<>();
            int lastRow = Math.min(sheet.getLastRowNum(), MAX_ROWS);
            for (int r = 0; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                List<String> values = new ArrayList<>();
                int max = row == null ? 0 : Math.min(row.getLastCellNum(), MAX_COLUMNS);
                for (int c = 0; c < max; c++) {
                    Cell cell = row.getCell(c);
                    values.add(cellValue(cell));
                }
                records.add(values);
            }
            return rowsFromRecords(records);
        }
    }

    private ParsedFile rowsFromRecords(List<List<String>> records) {
        if (records.isEmpty()) throw new BadRequestException("Ficheiro vazio");
        List<String> headers = records.getFirst().stream().map(String::trim).toList();
        if (headers.size() > MAX_COLUMNS) throw new BadRequestException("Número máximo de colunas excedido");
        if (records.size() - 1 > MAX_ROWS) throw new BadRequestException("Número máximo de linhas excedido");
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            List<String> record = records.get(i);
            Map<String, String> row = new LinkedHashMap<>();
            for (String header : headers) {
                int index = headers.indexOf(header);
                row.put(header, index < record.size() ? normalize(record.get(index)) : "");
            }
            rows.add(row);
        }
        return new ParsedFile(headers, rows);
    }

    private void validateHeaders(TipoDadosMestres tipo, List<String> actual) {
        List<String> expected = headers(tipo);
        for (String header : expected) {
            if (!actual.contains(header)) {
                throw new BadRequestException("Coluna obrigatória ausente: " + header);
            }
        }
        if (actual.stream().distinct().count() != actual.size()) {
            throw new BadRequestException("Cabeçalho contém colunas duplicadas");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("Ficheiro vazio");
        if (file.getSize() > MAX_BYTES) throw new BadRequestException("Ficheiro excede 10 MB");
        String name = safeFilename(file.getOriginalFilename());
        if (name.contains("/") || name.contains("\\") || name.contains("..")) throw new BadRequestException("Nome de ficheiro inválido");
        format(name, file.getContentType());
    }

    private String format(String originalFilename, String contentType) {
        String name = safeFilename(originalFilename).toLowerCase(Locale.ROOT);
        if (name.endsWith(".csv")) return "csv";
        if (name.endsWith(".xlsx")) return "xlsx";
        throw new BadRequestException("Formato não suportado. Use CSV ou XLSX");
    }

    private ImportacaoValidacaoDto toValidacaoDto(ImportacaoDadosMestres entity, ValidationResult validation) {
        return new ImportacaoValidacaoDto(entity.getId(), entity.getTipo(), entity.getNomeFicheiro(), entity.getFormato(),
                entity.getExpiraEm(), validation.resumo(), validation.errors(), validation.warnings(),
                validation.validRows().stream().limit(PREVIEW_LIMIT).map(ValidatedRow::values).toList());
    }

    private ClienteCreateDto toCliente(Map<String, String> row) {
        return new ClienteCreateDto(value(row, "nome"), value(row, "morada"), blankToNull(row, "morada1"),
                blankToNull(row, "localidade"), value(row, "nif"), blankToNull(row, "tel"), blankToNull(row, "tm"),
                value(row, "email"), blankToNull(row, "email1"), blankToNull(row, "tspiva"), blankToNull(row, "iban"),
                bool(row, "retencao"), bool(row, "inativo"), blankToNull(row, "observacoes"), value(row, "codPostalId"),
                value(row, "paisId"), value(row, "moedaId"), integerOrNull(row, "mPagamentoId"),
                blankToNull(row, "pPagamentoId"), blankToNull(row, "rivaId"), Integer.valueOf(value(row, "transporteId")));
    }

    private ArtigoCreateDto toArtigo(Map<String, String> row) {
        return new ArtigoCreateDto(value(row, "codigo").toUpperCase(Locale.ROOT), blankToNull(row, "abreviatura"),
                blankToNull(row, "codigoIdentificacao"), value(row, "descricao"), value(row, "unidade"),
                Long.valueOf(value(row, "familiaId")), decimalOrNull(row, "peso"), value(row, "ivaCompraId"),
                value(row, "ivaVendaId"), new BigDecimal(value(row, "pvp").replace(',', '.')),
                bool(row, "inativo"), bool(row, "retencao"), blankToNull(row, "observacoes"));
    }

    private List<Map<String, String>> exportClientes(Boolean ativos) {
        return clienteRepository.findAll().stream()
                .filter(c -> ativos == null || !ativos || !c.isInativo())
                .map(this::clienteRow)
                .toList();
    }

    private Map<String, String> clienteRow(Cliente c) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("nome", c.getNome());
        row.put("morada", c.getMorada());
        row.put("morada1", c.getMorada1());
        row.put("localidade", c.getLocalidade());
        row.put("nif", c.getNif());
        row.put("tel", c.getTel());
        row.put("tm", c.getTm());
        row.put("email", c.getEmail());
        row.put("email1", c.getEmail1());
        row.put("tspiva", c.getTspiva());
        row.put("iban", c.getIban());
        row.put("retencao", String.valueOf(c.isRetencao()));
        row.put("inativo", String.valueOf(c.isInativo()));
        row.put("observacoes", c.getObservacoes());
        row.put("codPostalId", c.getCodPostal().getId());
        row.put("paisId", c.getPais().getId());
        row.put("moedaId", c.getMoeda().getId());
        row.put("mPagamentoId", c.getMPagamento() == null ? "" : String.valueOf(c.getMPagamento().getId()));
        row.put("pPagamentoId", c.getPPagamento() == null ? "" : c.getPPagamento().getId());
        row.put("rivaId", c.getRiva().getId());
        row.put("transporteId", String.valueOf(c.getTransporte().getId()));
        return row;
    }

    private List<Map<String, String>> exportArtigos(Boolean ativos) {
        return artigoRepository.findAll().stream()
                .filter(a -> ativos == null || !ativos || !a.isInativo())
                .map(this::artigoRow)
                .toList();
    }

    private Map<String, String> artigoRow(Artigo a) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("codigo", a.getCodigo());
        row.put("abreviatura", a.getAbreviatura());
        row.put("codigoIdentificacao", a.getCodigoIdentificacao());
        row.put("descricao", a.getDescricao());
        row.put("unidade", a.getUnidade());
        row.put("familiaId", String.valueOf(a.getFamilia().getId()));
        row.put("peso", a.getPeso() == null ? "" : a.getPeso().toPlainString());
        row.put("ivaCompraId", a.getIvaCompra().getId());
        row.put("ivaVendaId", a.getIvaVenda().getId());
        row.put("pvp", a.getPvp().toPlainString());
        row.put("inativo", String.valueOf(a.isInativo()));
        row.put("retencao", String.valueOf(a.isRetencao()));
        row.put("observacoes", a.getObservacoes());
        return row;
    }

    private ExportedFile exportRows(String baseName, String formato, List<String> headers, List<Map<String, String>> rows) {
        return "xlsx".equals(formato) ? exportXlsx(baseName, headers, rows) : exportCsv(baseName, headers, rows);
    }

    private ExportedFile exportCsv(String baseName, List<String> headers, List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(headers.stream().map(this::csv).collect(Collectors.joining(";"))).append("\r\n");
        for (Map<String, String> row : rows) {
            sb.append(headers.stream().map(h -> csv(safeExcel(row.get(h)))).collect(Collectors.joining(";"))).append("\r\n");
        }
        return new ExportedFile(baseName + ".csv", "text/csv;charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private ExportedFile exportXlsx(String baseName, List<String> headers, List<Map<String, String>> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("dados");
            Row header = sheet.createRow(0);
            for (int c = 0; c < headers.size(); c++) header.createCell(c).setCellValue(headers.get(c));
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < headers.size(); c++) {
                    row.createCell(c).setCellValue(safeExcel(rows.get(r).get(headers.get(c))));
                }
            }
            workbook.write(out);
            return new ExportedFile(baseName + ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Erro ao gerar XLSX", exception);
        }
    }

    private List<String> headers(TipoDadosMestres tipo) {
        return tipo == TipoDadosMestres.CLIENTES ? CLIENTE_HEADERS : ARTIGO_HEADERS;
    }

    private Map<String, String> example(TipoDadosMestres tipo) {
        Map<String, String> row = new LinkedHashMap<>();
        headers(tipo).forEach(h -> row.put(h, ""));
        if (tipo == TipoDadosMestres.CLIENTES) {
            row.put("nome", "Cliente Exemplo");
            row.put("morada", "Rua Exemplo 1");
            row.put("localidade", "Lisboa");
            row.put("nif", "509999999");
            row.put("email", "cliente.exemplo@fac.demo");
            row.put("codPostalId", "1000-001");
            row.put("paisId", "PT");
            row.put("moedaId", "EUR");
            row.put("rivaId", "CON");
            row.put("transporteId", "1001");
        } else {
            row.put("codigo", "ARTEXEMPLO");
            row.put("descricao", "Artigo exemplo");
            row.put("unidade", "UN");
            row.put("familiaId", "1");
            row.put("ivaCompraId", "NOR");
            row.put("ivaVendaId", "NOR");
            row.put("pvp", "10.000000");
        }
        return row;
    }

    private void validateParsedHeaders(TipoDadosMestres tipo, ParsedFile parsed) {
        validateHeaders(tipo, parsed.headers());
    }

    private String toJson(List<Map<String, String>> rows) {
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Erro ao serializar importação", exception);
        }
    }

    private List<Map<String, String>> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Erro ao ler sessão de importação", exception);
        }
    }

    private String dados(TipoDadosMestres tipo, String ficheiro, ImportacaoResumoDto resumo) {
        return dados(tipo, ficheiro, resumo.totalLinhas(), resumo.registosNovos(), resumo.linhasComErro());
    }

    private String dados(TipoDadosMestres tipo, String ficheiro, int linhas, int criados, int rejeitados) {
        return "{\"tipo\":\"%s\",\"ficheiro\":\"%s\",\"linhas\":%d,\"criados\":%d,\"rejeitados\":%d}"
                .formatted(tipo, safeJson(ficheiro), linhas, criados, rejeitados);
    }

    private void required(int line, Map<String, String> row, List<ImportacaoErroDto> errors, String column, String code) {
        if (value(row, column).isBlank()) errors.add(issue(line, column, "", code, "Campo obrigatório"));
    }

    private void max(int line, Map<String, String> row, List<ImportacaoErroDto> errors, String column, int max, String code) {
        String value = value(row, column);
        if (value.length() > max) errors.add(issue(line, column, value, code, "Campo excede " + max + " caracteres"));
    }

    private void validateEmail(int line, Map<String, String> row, List<ImportacaoErroDto> errors, String column, String code) {
        String value = value(row, column);
        if (!value.isBlank() && !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.add(issue(line, column, value, code, "Email inválido"));
        }
    }

    private void exists(int line, Map<String, String> row, List<ImportacaoErroDto> errors, String column, String code,
                        java.util.function.Predicate<String> predicate) {
        String value = value(row, column);
        if (!value.isBlank() && !predicate.test(value)) {
            errors.add(issue(line, column, value, code, "Código inexistente"));
        }
    }

    private void integerExists(int line, Map<String, String> row, List<ImportacaoErroDto> errors, String column, String code,
                               java.util.function.IntPredicate predicate) {
        String value = value(row, column);
        if (value.isBlank()) return;
        try {
            int id = Integer.parseInt(value);
            if (!predicate.test(id)) errors.add(issue(line, column, value, code, "Código inexistente"));
        } catch (NumberFormatException exception) {
            errors.add(issue(line, column, value, code, "Valor inteiro inválido"));
        }
    }

    private void decimal(int line, Map<String, String> row, List<ImportacaoErroDto> errors, String column, int scale,
                         boolean required, String code) {
        String value = value(row, column);
        if (value.isBlank()) {
            if (required) errors.add(issue(line, column, value, code, "Valor decimal obrigatório"));
            return;
        }
        try {
            BigDecimal parsed = new BigDecimal(value.replace(',', '.'));
            if (parsed.signum() < 0 || parsed.scale() > scale) {
                errors.add(issue(line, column, value, code, "Valor decimal inválido"));
            }
        } catch (NumberFormatException exception) {
            errors.add(issue(line, column, value, code, "Valor decimal inválido"));
        }
    }

    private Boolean parseBoolean(int line, Map<String, String> row, List<ImportacaoErroDto> errors, String column) {
        String value = value(row, column);
        if (value.isBlank()) return false;
        if (List.of("true", "false", "sim", "nao", "não", "1", "0").contains(value.toLowerCase(Locale.ROOT))) {
            return bool(row, column);
        }
        errors.add(issue(line, column, value, "VALOR_BOOLEANO_INVALIDO", "Use true/false, sim/não ou 1/0"));
        return false;
    }

    private ImportacaoErroDto issue(int line, String column, String value, String code, String message) {
        return new ImportacaoErroDto(line, column, value, code, message);
    }

    private boolean validPtNif(String nif) {
        int sum = 0;
        for (int i = 0; i < 8; i++) sum += Character.digit(nif.charAt(i), 10) * (9 - i);
        int check = 11 - (sum % 11);
        if (check >= 10) check = 0;
        return check == Character.digit(nif.charAt(8), 10);
    }

    private String cellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.FORMULA) throw new BadRequestException("XLSX contém fórmula; apenas valores são permitidos");
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case BLANK -> "";
            default -> "";
        };
    }

    private String value(Map<String, String> row, String key) {
        return normalize(row.get(key));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimCr(String value) {
        return value.endsWith("\r") ? value.substring(0, value.length() - 1) : value;
    }

    private String blankToNull(Map<String, String> row, String key) {
        String value = value(row, key);
        return value.isBlank() ? null : value;
    }

    private boolean bool(Map<String, String> row, String key) {
        String value = value(row, key).toLowerCase(Locale.ROOT);
        return value.equals("true") || value.equals("sim") || value.equals("1");
    }

    private Integer integerOrNull(Map<String, String> row, String key) {
        String value = value(row, key);
        return value.isBlank() ? null : Integer.valueOf(value);
    }

    private BigDecimal decimalOrNull(Map<String, String> row, String key) {
        String value = value(row, key);
        return value.isBlank() ? null : new BigDecimal(value.replace(',', '.'));
    }

    private String csv(String value) {
        String v = Objects.toString(value, "");
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private String safeExcel(String value) {
        String v = Objects.toString(value, "");
        return v.matches("^[=+\\-@].*") ? "'" + v : v;
    }

    private String safeFilename(String filename) {
        return filename == null || filename.isBlank() ? "ficheiro" : filename.replaceAll("[\\r\\n]", "").trim();
    }

    private String safeJson(String value) {
        return Objects.toString(value, "").replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String normalizedExportFormat(String formato) {
        if (formato == null || formato.isBlank() || "csv".equalsIgnoreCase(formato)) return "csv";
        if ("xlsx".equalsIgnoreCase(formato)) return "xlsx";
        throw new BadRequestException("Formato de exportação não suportado");
    }

    public record ExportedFile(String filename, String mediaType, byte[] content) {
    }

    private record ParsedFile(List<String> headers, List<Map<String, String>> rows) {
    }

    private record ValidatedRow(int line, Map<String, String> values) {
    }

    private record ValidationResult(ImportacaoResumoDto resumo, List<ImportacaoErroDto> errors,
                                    List<ImportacaoErroDto> warnings, List<ValidatedRow> validRows) {
    }
}
