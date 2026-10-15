package com.ar2lda.fac.financial;

import com.ar2lda.fac.controller.dto.*;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.mapper.*;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import com.ar2lda.fac.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

/** Internal preparation only: no controller/legacy service invokes this adapter. Operational activation is prohibited in B1. */
@Service
@RequiredArgsConstructor
public class FinancialReceiptIssuer {
    private final FinancialCore core;
    private final JdbcTemplate db;
    private final EntityManager entities;
    private final Validator validator;
    private final ObjectMapper json;
    private final DocumentoFinanceiroRepository documents;
    private final LinhaDocumentoFinanceiroRepository lines;
    private final PendenteRepository pendings;
    private final ClienteRepository clients;
    private final TipoDocumentoRepository types;
    private final MoedaRepository currencies;
    private final MPagamentoRepository payments;
    private final EmpresaRepository companies;
    private final CurrentUserService users;
    private final SerieService series;
    private final AtcudService atcud;
    private final FiscalQrService qr;
    private final DocumentoFinanceiroMapper mapper;
    private final EmpresaMapper companyMapper;
    private final ClienteMapper clientMapper;
    private final AuditoriaService audit;
    private final FinancialCircuitGuard circuitGuard;

    private static void require(boolean value,String message) { if(!value) throw new BadRequestException(message); }
    public static BigDecimal exactCents(BigDecimal value) {
        require(value!=null && value.signum()>=0 && value.precision()-value.scale()<=17,"Invalid receipt amount");
        try { return value.setScale(2,RoundingMode.UNNECESSARY); }
        catch(ArithmeticException e) { throw new BadRequestException("Receipt requires exact cents; no implicit rounding"); }
    }
    private static boolean nonzero(BigDecimal value) { return value!=null && value.signum()!=0; }
    private record Candidate(Pendente pending, long positionId, BigDecimal amount) {}
    private record Checked(Pendente pending, FinancialCore.Position position, BigDecimal amount) {}

    @Transactional
    public DocumentoFinanceiroDto issue(String requestKey, DocumentoFinanceiroCreateDto dto) {
        require(requestKey!=null && !requestKey.isBlank() && requestKey.length()<=120,"Persistent receipt request identity required");
        require(dto!=null && validator.validate(dto).isEmpty(),"Invalid receipt request");
        require("EUR".equals(dto.moedaId()),"New receipt circuit supports EUR only");
        var actor=users.resolve(dto.emissorId(),"emitir recibo financeiro interno");
        var normalised=new ArrayList<LinhaDocumentoFinanceiroCreateDto>();
        var seen=new HashSet<Long>();
        for(var line:dto.linhas()) {
            require(line!=null && line.pendenteId()!=null && line.pendenteId()>0 && seen.add(line.pendenteId()),"Duplicate/invalid receipt debt");
            require(!nonzero(line.descontoValor()) && !nonzero(line.descontoPercentual()),"Financial discounts are not enabled in new receipt circuit");
            var amount=exactCents(line.valorALiquidar()); require(amount.signum()>0,"Positive settlement required");
            normalised.add(new LinhaDocumentoFinanceiroCreateDto(line.pendenteId(),amount,BigDecimal.ZERO,BigDecimal.ZERO));
        }
        var canonical=new DocumentoFinanceiroCreateDto(dto.tipoDocumentoId(),dto.serie(),dto.dataEmissao(),dto.clienteId(),
                dto.moedaId(),dto.mPagamentoId(),dto.dataHoraOperacao(),actor.getCodigo(),dto.observacoes(),normalised,ReceiptValueDate.effective(dto));
        String payload;
        try { payload=json.writeValueAsString(canonical); } catch(JsonProcessingException e) { throw new IllegalStateException(e); }
        // Serialize missing keys too. Do this BEFORE series reservation or any documentary write.
        db.queryForList("select pg_advisory_xact_lock(hashtextextended(?,0))","RC_REQUEST:1:"+requestKey);
        var replay=db.queryForList("select document_id,request_payload from financial_receipt where empresa_id=1 and request_key=?",requestKey);
        if(!replay.isEmpty()) {
            require(samePayload(json,payload,(String)replay.getFirst().get("request_payload")),"Receipt request identity reused with different content/context");
            return response(((Number)replay.getFirst().get("document_id")).longValue());
        }
        var client=clients.findById(dto.clienteId()).orElseThrow(()->new BadRequestException("Unknown customer"));
        var type=types.findById(dto.tipoDocumentoId()).orElseThrow(()->new BadRequestException("Unknown document type"));
        require("RC".equals(type.getCodigoFiscal()),"Only fiscal RC supported by receipt adapter");
        var currency=currencies.findById("EUR").orElseThrow();
        var payment=payments.findById(dto.mPagamentoId().toUpperCase(Locale.ROOT)).orElseThrow(()->new BadRequestException("Unknown payment method"));
        var scope=new FinancialCore.Scope(1,client.getId(),"EUR");
        var candidates=new ArrayList<Candidate>();
        // Technical Pendente identities are locked in stable order; their balance is never used or updated.
        var ordered=normalised.stream().sorted(Comparator.comparing(LinhaDocumentoFinanceiroCreateDto::pendenteId)).toList();
        for(var line:ordered) {
            var pending=pendings.findByIdForUpdate(line.pendenteId()).orElseThrow(()->new BadRequestException("Unknown debt identity"));
            ReceiptValueDate.validate(dto,pending);
            var doc=pending.getDocumentoComercial();
            require(doc.getCliente().getId().equals(client.getId()) && "EUR".equals(doc.getMoeda().getId()),"Debt outside receipt scope");
            require(doc.getEstado()==EstadoDocumentoComercial.EMITIDO && !doc.isAnulado(),"Invoice is not eligible");
            String fiscalType=doc.getTipoDocumentoCodigoFiscal()!=null?doc.getTipoDocumentoCodigoFiscal():doc.getTipoDocumento().getCodigoFiscal();
            require("FT".equals(fiscalType),"Only FT positions are eligible for the new receipt circuit");
            var linked=db.queryForList("select position_id from financial_document_origin where document_id=?",doc.getId());
            require(linked.size()==1,"Invoice has no FinancialCore origin; no legacy fallback or backfill");
            circuitGuard.requireCoreEligible(pending.getId());
            long position=((Number)linked.getFirst().get("position_id")).longValue();
            candidates.add(new Candidate(pending,position,line.valorALiquidar()));
        }
        var byIdentity=new HashMap<Long,Checked>();
        for(var candidate:candidates.stream().sorted(Comparator.comparingLong(Candidate::positionId)).toList()) {
            db.queryForList("select id from financial_position where id=? for update",candidate.positionId());
            var p=core.position(scope,candidate.positionId());
            var doc=candidate.pending().getDocumentoComercial();
            require(p.nature()==FinancialCore.Nature.DEBT && !p.cancelled(),"Invalid debt position");
            require(p.original().compareTo(doc.getValorTotal())==0 && p.available().compareTo(candidate.amount())>=0,"Inconsistent/insufficient invoice residual");
            byIdentity.put(candidate.pending().getId(),new Checked(candidate.pending(),p,candidate.amount()));
        }
        var checked=normalised.stream().map(l->byIdentity.get(l.pendenteId())).toList();
        var number=series.proximoNumeroParaEmissao(dto.tipoDocumentoId(),dto.serie());
        // The series lock is held until this transaction commits: revalidate chronological order here.
        var last=documents.findUltimaDataEmissao(dto.tipoDocumentoId(),dto.serie());
        require(last==null || !dto.dataEmissao().isBefore(last),"Receipt predates last issued document of series");
        var document=new DocumentoFinanceiro(); document.setCliente(client); document.setTipoDocumento(type); document.setSerie(dto.serie());
        document.setNumeroDocumento(number.numeroSequencial()); document.atribuirAtcud(number.codigoValidacaoAt(),atcud.gerar(number.codigoValidacaoAt(),number.numeroSequencial()));
        document.setDataEmissao(dto.dataEmissao()); document.setDataValor(ReceiptValueDate.effective(dto)); document.setMoeda(currency); document.setMPagamento(payment);
        document.setDataHoraOperacao(dto.dataHoraOperacao()!=null?dto.dataHoraOperacao():OffsetDateTime.now());
        document.setMomentoEmissao(OffsetDateTime.now()); document.setEmissor(actor); document.setObservacoes(dto.observacoes());
        var total=checked.stream().map(Checked::amount).reduce(BigDecimal.ZERO,BigDecimal::add);
        require(total.precision()-total.scale()<=13,"Receipt total exceeds existing documentary precision");
        document.setValorPagamentoBruto(total); document.setValorPagamentoLiquido(total); document.setValorDescontoFinanceiro(BigDecimal.ZERO);
        document=documents.saveAndFlush(document);
        long operation=core.settle(scope,"RC_DOCUMENT:"+document.getId(),new FinancialCore.Settlement(
                checked.stream().map(c->new FinancialCore.Part(c.position().id(),c.amount())).toList(),total,BigDecimal.ZERO,BigDecimal.ZERO),actor.getCodigo());
        var savedLines=new ArrayList<LinhaDocumentoFinanceiro>();
        int index=1;
        for(var c:checked) {
            var pending=c.pending(); var origin=pending.getDocumentoComercial(); var line=new LinhaDocumentoFinanceiro();
            line.setDocumentoFinanceiro(document); line.setNumeroLinha(index++); line.setPendente(pending);
            line.setDataDocumento(origin.getDataEmissao()); line.setDataVencimento(origin.getDataVencimento());
            line.setTipoDocumento(origin.getTipoDocumento()); line.setNumeroDocumento(origin.getNumeroDocumento()); line.setSerieDocumento(origin.getSerie());
            line.setValorDocumento(c.position().original()); line.setValorPendenteAntes(c.position().available());
            line.setValorALiquidar(c.amount()); line.setValorPagamentoLiquido(c.amount()); line.setDescontoValor(BigDecimal.ZERO); line.setDescontoPercentual(BigDecimal.ZERO);
            line.setNovoValorPendente(c.position().available().subtract(c.amount())); line.setMoeda(currency);
            savedLines.add(lines.save(line));
        }
        entities.flush();
        var company=companies.findById(Empresa.EMPRESA_ID).orElseThrow();
        qr.buildDocumentoFinanceiro(new DocumentoFinanceiroImpressaoDto(companyMapper.toDTO(company),clientMapper.toDTO(client),mapper.toDTO(document,savedLines)))
                .ifPresent(q->documents.getReferenceById(savedLines.getFirst().getDocumentoFinanceiro().getId()).atribuirQrFiscal(q.payload(),q.version()));
        entities.flush();
        db.update("insert into financial_receipt(document_id,operation_id,empresa_id,cliente_id,moeda_id,request_key,request_payload) values(?,?,1,?,'EUR',?,?)",
                document.getId(),operation,client.getId(),requestKey,payload);
        for(var line:savedLines) {
            long pos=byIdentity.get(line.getPendente().getId()).position().id();
            long allocation=db.queryForObject("select id from financial_allocation where operation_id=? and position_id=?",Long.class,operation,pos);
            db.update("insert into financial_receipt_line(line_id,allocation_id) values(?,?)",line.getId(),allocation);
        }
        audit.registarComo(TipoAuditoriaEvento.RECEBIMENTO_REGISTADO,"DOCUMENTO_FINANCEIRO",document.getId(),actor,ResultadoAuditoria.SUCESSO,
                type.getId()+" "+document.getSerie()+"/"+document.getNumeroDocumento(),"Recebimento registado","{\"versao\":1,\"valor\":"+total.toPlainString()+"}");
        return mapper.toDTO(document,savedLines);
    }
    static boolean samePayload(ObjectMapper json, String current, String stored) {
        try {
            var reader=json.reader().with(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
            com.fasterxml.jackson.databind.JsonNode expected=reader.readTree(current);
            com.fasterxml.jackson.databind.JsonNode previous=reader.readTree(stored);
            if (!previous.hasNonNull("dataValor")) {
                ((com.fasterxml.jackson.databind.node.ObjectNode)previous).set("dataValor",previous.get("dataEmissao"));
                return expected.get("dataValor").equals(previous.get("dataValor"))
                    && withoutValueDate(json,current).equals(withoutValueDate(json,stored));
            }
            return current.equals(stored);
        } catch (java.io.IOException invalid) {
            throw new IllegalStateException("Invalid persisted receipt request",invalid);
        }
    }
    /** Remove only the new field; retain every original character of all monetary fields. */
    private static String withoutValueDate(ObjectMapper json, String payload) throws java.io.IOException {
        try (var parser=json.getFactory().createParser(payload)) {
            parser.nextToken();
            while (parser.nextToken()==com.fasterxml.jackson.core.JsonToken.FIELD_NAME) {
                String name=parser.currentName();
                int start=(int)parser.currentTokenLocation().getCharOffset();
                parser.nextToken(); parser.skipChildren(); parser.finishToken();
                int end=(int)parser.currentLocation().getCharOffset();
                if ("dataValor".equals(name)) {
                    int before=start-1; while(before>=0 && Character.isWhitespace(payload.charAt(before))) before--;
                    int after=end; while(after<payload.length() && Character.isWhitespace(payload.charAt(after))) after++;
                    if(before>=0 && payload.charAt(before)==',') start=before;
                    else if(after<payload.length() && payload.charAt(after)==',') end=after+1;
                    return payload.substring(0,start)+payload.substring(end);
                }
            }
            return payload;
        }
    }
    private DocumentoFinanceiroDto response(long id) {
        return mapper.toDTO(documents.findById(id).orElseThrow(),lines.findByDocumentoFinanceiroIdOrderByNumeroLinha(id));
    }
}
