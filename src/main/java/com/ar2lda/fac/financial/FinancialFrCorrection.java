package com.ar2lda.fac.financial;

import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.model.PermissaoFuncional;
import com.ar2lda.fac.model.Utilizador;
import com.ar2lda.fac.model.ResultadoAuditoria;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import com.ar2lda.fac.service.AuditoriaService;
import com.ar2lda.fac.service.AuditoriaIsoladaService;
import org.springframework.security.access.AccessDeniedException;
import com.ar2lda.fac.security.FunctionalAuthorization;
import com.ar2lda.fac.service.CurrentUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Internal correction of a synthetic or erroneous FR receipt, without a cash disbursement. */
@Service
@Profile("fr-inactive-lab")
public class FinancialFrCorrection {
    public enum MovementKind { INVOICE, RECEIPT, RECEIPT_VOID, INVOICE_VOID }
    public record Movement(MovementKind kind, BigDecimal debtDelta, FinancialQueries.OperationView operation) {}

    private record Act(long creation, long settlement, BigDecimal amount, FinancialCore.Scope scope) {}
    private final JdbcTemplate db;
    private final FinancialCore core;
    private final FinancialQueries queries;
    private final FunctionalAuthorization permissions;
    private final CurrentUserService users;

    private final AuditoriaService audit;
    private final AuditoriaIsoladaService isolatedAudit;

    public FinancialFrCorrection(JdbcTemplate db, FinancialCore core, FinancialQueries queries,
                                 FunctionalAuthorization permissions, CurrentUserService users,
                                 AuditoriaService audit, AuditoriaIsoladaService isolatedAudit) {
        this.db = db;
        this.core = core;
        this.queries = queries;
        this.permissions = permissions;
        this.users = users;
        this.audit = audit;
        this.isolatedAudit = isolatedAudit;
    }

    /** Explicit operator attestation that the recorded receipt never occurred. */
    @Transactional
    public long voidReceiptForFundsNeverReceived(long document, String reason, boolean fundsNeverReceived) {
        return audited(document, "QUITACAO", actor -> {
            if (!fundsNeverReceived) throw new BadRequestException("Confirme que os fundos nunca foram recebidos");
            var act = act(document, true);
            boolean replay = reversalExists(act.settlement(), "FR_VOID_RECEIPT:" + document);
            long reversal = core.voidFrReceipt(act.scope(), document, act.settlement(), reason(reason), actor.getCodigo());
            return new AuditResult(reversal, !replay);
        });
    }

    @Transactional
    public long voidInvoice(long document, String reason) {
        return audited(document, "FACTURA", actor -> {
            var act = act(document, true);
            if (!reversalExists(act.settlement(), "FR_VOID_RECEIPT:" + document)) {
                throw new ConflictException("Anule primeiro a quitação associada à FR");
            }
            String justification = reason(reason);
            long reversal = core.reverse(act.scope(), "FR_VOID_INVOICE:" + document, act.creation(),
                    justification, actor.getCodigo());
            int updated = db.update("""
                update documento_comercial d set estado='ANULADO', anulado=true, motivo_anulacao=?,
                    data_hora_anulacao=r.created_at, anulado_por_utilizador_id=?, anulado_por_nome=?
                from financial_operation r where d.id=? and d.estado='EMITIDO' and r.id=?
                """, justification, actor.getCodigo(), actor.getNome(), document, reversal);
            return new AuditResult(reversal, updated == 1);
        });
    }

    private boolean reversalExists(long original, String key) {
        return Boolean.TRUE.equals(db.queryForObject("""
            select exists(select 1 from financial_operation where reverse_of=? and request_key=?)
            """, Boolean.class, original, key));
    }

    private record AuditResult(long operation, boolean fresh) {}

    private long audited(long document, String kind, java.util.function.Function<Utilizador, AuditResult> action) {
        Utilizador actor = null;
        try {
            actor = authorisedActor(PermissaoFuncional.DOCUMENTO_ANULAR);
            var result = action.apply(actor);
            if (result.fresh()) {
                audit.registarComo(TipoAuditoriaEvento.DOCUMENTO_ANULADO, "FR_" + kind, document, actor,
                        ResultadoAuditoria.SUCESSO, null, "Anulação de " + kind + " da FR",
                        "{\"versao\":1,\"operacao\":" + result.operation() + "}");
            }
            return result.operation();
        } catch (AccessDeniedException | ConflictException | BadRequestException denied) {
            isolatedAudit.registar(TipoAuditoriaEvento.TENTATIVA_ANULACAO_NEGADA, "FR_" + kind, document,
                    actor != null ? actor : users.currentUserOrNull(), ResultadoAuditoria.FALHA, null,
                    denied.getMessage(), "{\"versao\":1}");
            throw denied;
        }
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public List<Movement> statement(long document) {
        authorisedActor(PermissaoFuncional.DOCUMENTO_ANULAR);
        var act = act(document, false);
        var invoice = queries.operation(act.scope(), act.creation());
        var receipt = queries.operation(act.scope(), act.settlement());
        var movements = new java.util.ArrayList<>(List.of(new Movement(MovementKind.INVOICE, act.amount(), invoice),
                new Movement(MovementKind.RECEIPT, act.amount().negate(), receipt)));
        if (receipt.reversedByOperationId() != null) movements.add(new Movement(MovementKind.RECEIPT_VOID, act.amount(),
                queries.operation(act.scope(), receipt.reversedByOperationId())));
        if (invoice.reversedByOperationId() != null) movements.add(new Movement(MovementKind.INVOICE_VOID, act.amount().negate(),
                queries.operation(act.scope(), invoice.reversedByOperationId())));
        return List.copyOf(movements);
    }

    private Act act(long document, boolean lock) {
        var rows = db.query("""
            select p.creation_id,f.operation_id,f.empresa_id,f.cliente_id,f.moeda_id,f.received
            from financial_immediate_invoice f join financial_position p on p.id=f.position_id
            join documento_comercial d on d.id=f.document_id
            where d.id=? and d.projection_external_type='FR' and d.estado in ('EMITIDO','ANULADO')
            """ + (lock ? " for update of d" : ""), (rs, row) -> new Act(rs.getLong(1),rs.getLong(2),rs.getBigDecimal(6),
                new FinancialCore.Scope(rs.getLong(3),rs.getLong(4),rs.getString(5))), document);
        if (rows.isEmpty()) throw new BadRequestException("FR governada desconhecida");
        return rows.getFirst();
    }

    private Utilizador authorisedActor(PermissaoFuncional permission) {
        permissions.require(permission);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var actor = users.currentUserOrNull();
        if (authentication == null || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken || actor == null || actor.isInativo()) {
            throw new BadRequestException("Utilizador activo autenticado obrigatório");
        }
        return actor;
    }

    private static String reason(String value) {
        if (value == null || value.strip().length() < 5 || value.strip().length() > 500) {
            throw new BadRequestException("Indique um motivo entre 5 e 500 caracteres");
        }
        return value.strip();
    }
}
