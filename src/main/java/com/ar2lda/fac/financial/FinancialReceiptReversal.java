package com.ar2lda.fac.financial;

import com.ar2lda.fac.controller.dto.DocumentoFinanceiroCreateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.ar2lda.fac.financial.FinancialCore.*;

/** Inactive internal adapter. No endpoint/legacy caller. Counter-entry is NOT a refund.
 * Autonomous cancellation is limited to a documented declaration of money NOT received.
 * Actual money received needs atomic replacement; unallocated cash/refunds remain out of scope.
 */
@Service
public class FinancialReceiptReversal {
    public record Result(long documentId, long reversalOperationId, Long replacementDocumentId) {}
    private final JdbcTemplate db;
    private final FinancialCore core;
    private final FinancialReceiptIssuer issuer;
    private final CurrentUserService users;
    private final AuditoriaService audit;
    private final ObjectMapper json;
    public FinancialReceiptReversal(JdbcTemplate db,FinancialCore core,FinancialReceiptIssuer issuer,
                                    CurrentUserService users,AuditoriaService audit,ObjectMapper json) {
        this.db=db;this.core=core;this.issuer=issuer;this.users=users;this.audit=audit;this.json=json;
    }
    private static void require(boolean ok,String message) { if(!ok) throw new BadRequestException(message); }

    /** The caller must establish the factual declaration; this bean cannot verify bank/cash reality. */
    @Transactional
    public Result cancelUnreceived(Scope scope,String key,long document,String reason,String evidence,String actor) {
        return execute(scope,key,document,reason,evidence,actor,null);
    }
    @Transactional
    public Result replace(Scope scope,String key,long document,String reason,String evidence,String actor,
                          DocumentoFinanceiroCreateDto replacement) {
        require(replacement!=null,"Replacement required");
        return execute(scope,key,document,reason,evidence,actor,replacement);
    }
    private Result execute(Scope scope,String key,long document,String reason,String evidence,String actorCode,
                           DocumentoFinanceiroCreateDto replacement) {
        require(scope!=null && scope.empresa()==1 && scope.cliente()>0 && "EUR".equals(scope.moeda()),"Invalid reversal scope");
        require(key!=null && !key.isBlank() && key.length()<=120,"Persistent reversal identity required");
        require(reason!=null && !reason.isBlank() && reason.length()<=500,"Reversal reason required");
        require(evidence!=null && !evidence.isBlank() && evidence.length()<=500,"Money treatment evidence required");
        var actor=users.resolve(actorCode,"anular recibo financeiro interno");
        require(replacement==null || (replacement.clienteId()!=null && replacement.clienteId()==scope.cliente() && scope.moeda().equals(replacement.moedaId())),"Replacement outside scope");
        long original=operationFor(scope,document);
        var originalValueDate=db.queryForObject("select data_valor from documento_financeiro where id=?",java.time.LocalDate.class,document);
        require(originalValueDate!=null,"Historical receipt has no recorded value date");
        if(replacement!=null) replacement=new DocumentoFinanceiroCreateDto(replacement.tipoDocumentoId(),replacement.serie(),
                replacement.dataEmissao(),replacement.clienteId(),replacement.moedaId(),replacement.mPagamentoId(),
                replacement.dataHoraOperacao(),actor.getCodigo(),replacement.observacoes(),replacement.linhas(),
                replacement.dataValor()==null?originalValueDate:replacement.dataValor());
        String payload;
        try { payload=json.writeValueAsString(Arrays.asList(document,reason,evidence,actor.getCodigo(),replacement)); }
        catch(JsonProcessingException e) { throw new IllegalStateException(e); }
        String operationKey="RC_REVERSE:"+key;
        require(operationKey.length()<=120,"Reversal identity too long");
        // Same key namespace/lock as core.reverse; replay precedes numbering and all mutations.
        db.queryForList("select pg_advisory_xact_lock(hashtextextended(?,0))",scope.empresa()+":"+operationKey);
        var previous=db.queryForList("""
            select op.cliente_id,op.moeda_id,a.* from financial_operation op
            left join financial_receipt_reversal a on a.operation_id=op.id
            where op.empresa_id=? and op.request_key=?
            """,scope.empresa(),operationKey);
        if(!previous.isEmpty()) {
            var old=previous.getFirst();
            require(old.get("document_id")!=null && ((Number)old.get("cliente_id")).longValue()==scope.cliente()
                    && scope.moeda().equals(old.get("moeda_id"))
                    && payload.equals(old.get("request_payload")) && ((Number)old.get("document_id")).longValue()==document,"Reversal identity reused with different request/context");
            return new Result(((Number)old.get("document_id")).longValue(),((Number)old.get("operation_id")).longValue(),
                    old.get("replacement_document_id")==null?null:((Number)old.get("replacement_document_id")).longValue());
        }
        var pendingIds=new TreeSet<Long>(db.queryForList("select id_pendente from linha_documento_financeiro where id_documento_financeiro=?",Long.class,document));
        if(replacement!=null && replacement.linhas()!=null)
            for(var line:replacement.linhas()) { require(line!=null && line.pendenteId()!=null,"Invalid replacement debt");pendingIds.add(line.pendenteId()); }
        for(long pending:pendingIds) db.queryForList("select id from pendente where id=? order by id for update",pending);
        // Union of original and replacement resources, in the same order as issuance.
        for(long position:db.queryForList("select distinct o.position_id from financial_document_origin o join pendente p on p.id_documento_comercial=o.document_id where p.id in ("
                +String.join(",",Collections.nCopies(pendingIds.size(),"?"))+") order by o.position_id",Long.class,pendingIds.toArray()))
            db.queryForList("select id from financial_position where id=? for update",position);
        var docs=db.queryForList("select anulado,data_valor from documento_financeiro where id=? for update",document);
        require(!docs.isEmpty() && Boolean.FALSE.equals(docs.getFirst().get("anulado")),"Receipt already cancelled");
        require(docs.getFirst().get("data_valor")!=null,"Historical receipt has no recorded value date");
        long reversed=core.reverse(scope,operationKey,original,reason,actor.getCodigo());
        Long substitute=null;
        if(replacement!=null) substitute=issuer.issue("RC_REPLACEMENT:"+reversed,replacement).id();
        db.update("""
            insert into financial_receipt_reversal(document_id,operation_id,replacement_document_id,money_treatment,request_payload,evidence,actor)
            values(?,?,?,?,?,?,?)
            """,document,reversed,substitute,substitute==null?"NOT_RECEIVED":"REPLACEMENT",payload,evidence,actor.getCodigo());
        db.update("update documento_financeiro set anulado=true where id=?",document);
        audit.registarComo(TipoAuditoriaEvento.DOCUMENTO_ANULADO,"DOCUMENTO_FINANCEIRO",document,actor,ResultadoAuditoria.SUCESSO,
                "RC "+document,reason,"{\"versao\":1,\"reversalOperation\":"+reversed+"}");
        return new Result(document,reversed,substitute);
    }
    private long operationFor(Scope scope,long document) {
        var ids=db.queryForList("select operation_id from financial_receipt where document_id=? and empresa_id=? and cliente_id=? and moeda_id=?",
                Long.class,document,scope.empresa(),scope.cliente(),scope.moeda());
        require(ids.size()==1,"Receipt outside scope or without FinancialCore origin");return ids.getFirst();
    }
}
