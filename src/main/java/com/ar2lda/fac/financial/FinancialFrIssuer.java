package com.ar2lda.fac.financial;

import com.ar2lda.fac.controller.dto.DocumentoComercialEmitirDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.MPagamentoRepository;
import com.ar2lda.fac.security.FunctionalAuthorization;
import com.ar2lda.fac.service.CurrentUserService;
import com.ar2lda.fac.service.DocumentoComercialService;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Internal experimental act, deliberately absent from the default application context.
 * No controller, consumer, scheduler or operational service calls this adapter.
 * The commercial document is the invoice AND quittance; no fiscal RC is generated.
 */
@Service
@Profile({"fr-inactive-lab", "demo"})
public class FinancialFrIssuer {
    public record Request(long draftId, BigDecimal received, String paymentMethod,
                          String paymentReference, boolean recalculationConfirmed) {}
    public record Issued(long documentId, long positionId, long settlementId) {}

    private final JdbcTemplate db;
    private final EntityManager entities;
    private final DocumentoComercialRepository documents;
    private final MPagamentoRepository payments;
    private final DocumentoComercialService emission;
    private final FinancialCore core;
    private final CurrentUserService users;
    private final FunctionalAuthorization permissions;

    public FinancialFrIssuer(JdbcTemplate db, EntityManager entities, DocumentoComercialRepository documents,
                             MPagamentoRepository payments, DocumentoComercialService emission,
                             FinancialCore core, CurrentUserService users, FunctionalAuthorization permissions) {
        this.db=db; this.entities=entities; this.documents=documents; this.payments=payments;
        this.emission=emission; this.core=core; this.users=users; this.permissions=permissions;
    }

    @Transactional
    public Issued issue(String key, Request request) { return issueAct(key,request,false); }

    @Transactional
    public Issued issueProjected(String key,Request request) { return issueAct(key,request,true); }

    private Issued issueAct(String key,Request request,boolean projected) {
        // Never accept an actor supplied by a payload, even if legacy security is disabled.
        permissions.require(PermissaoFuncional.DOCUMENTO_EMITIR);
        var authentication=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        require(authentication!=null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken),
                "Authenticated active issuer required");
        var actor=users.currentUserOrNull();
        require(actor!=null && !actor.isInativo(), "Authenticated active issuer required");
        require(key!=null && !key.isBlank() && key.length()<=120, "Invalid FR request key");
        require(request!=null && request.draftId()>0, "FR draft required");
        require(request.received()!=null && request.received().signum()>0
                && request.received().scale()<=2 && request.received().precision()-request.received().scale()<=17,
                "Exact positive EUR cents required");
        require(request.paymentMethod()!=null && request.paymentReference()!=null
                && !request.paymentReference().isBlank() && request.paymentReference().length()<=200,
                "Payment method and reference required");
        var cash=request.received().setScale(2);
        db.queryForList("select pg_advisory_xact_lock(hashtextextended(?,0))", "FR_REQUEST:1:"+key);
        var previous=db.queryForList("select f.*,d.projection_external_type from financial_immediate_invoice f join documento_comercial d on d.id=f.document_id where f.empresa_id=1 and f.request_key=?",key);
        if (!previous.isEmpty()) {
            var row=previous.getFirst();
            require(projected==(row.get("projection_external_type")!=null) && ((Number)row.get("document_id")).longValue()==request.draftId()
                    && ((BigDecimal)row.get("received")).compareTo(cash)==0
                    && request.paymentMethod().equals(row.get("payment_method"))
                    && request.paymentReference().equals(row.get("payment_reference"))
                    && actor.getCodigo().equals(row.get("actor"))
                    && request.recalculationConfirmed()==(Boolean)row.get("recalculation_confirmed"),
                    "FR idempotency key reused with different request");
            return new Issued(request.draftId(),((Number)row.get("position_id")).longValue(),
                    ((Number)row.get("operation_id")).longValue());
        }
        var document=documents.findByIdForUpdate(request.draftId())
                .orElseThrow(()->new BadRequestException("Unknown FR draft"));
        if (document.getEstado()!=EstadoDocumentoComercial.RASCUNHO)
            throw new ConflictException("FR requires an unissued draft");
        // The signature source must see the final payment/quittance identity.
        // These changes remain uncommitted and are rolled back with any later refusal.
        document.setMPagamento(payments.findById(request.paymentMethod())
                .orElseThrow(()->new BadRequestException("Unknown FR payment method")));
        document.setLiquidado(true);
        // The ordinary issuer locks the type before reading/revalidating its configuration.
        var emissionRequest=new DocumentoComercialEmitirDto(actor.getCodigo(),request.recalculationConfirmed());
        if(projected)emission.emitirFrProjectado(document.getId(),emissionRequest);
        else emission.emitir(document.getId(),emissionRequest);
        entities.flush();
        entities.refresh(document);
        var type=document.getTipoDocumento();
        require("FT".equals(type.getCodigoFiscal()) && type.isLiquidacaoImediata()
                && "EUR".equals(document.getMoeda().getId()), "FR requires internal FT/immediate, EUR");
        require(document.getValorTotal().compareTo(cash)==0, "FR must receive the full invoice total");
        var scope=new FinancialCore.Scope(Empresa.EMPRESA_ID,document.getCliente().getId(),"EUR");
        var position=core.create(scope,"FR_ORIGIN:"+document.getId(), FinancialCore.Nature.DEBT,
                "DOCUMENTO_COMERCIAL:"+document.getId(),document.getNumeroDocumentoCompleto(),cash,actor.getCodigo());
        db.update("insert into financial_document_origin(document_id,position_id) values(?,?)",document.getId(),position.id());
        long settlement=core.settle(scope,"FR_SETTLE:"+document.getId(),new FinancialCore.Settlement(
                List.of(new FinancialCore.Part(position.id(),cash)),cash,BigDecimal.ZERO,BigDecimal.ZERO),actor.getCodigo());
        db.update("""
            insert into financial_immediate_invoice(document_id,position_id,operation_id,empresa_id,cliente_id,
              moeda_id,request_key,received,payment_method,payment_reference,actor,recalculation_confirmed)
            values(?,?,?,1,?,'EUR',?,?,?,?,?,?)
            """,document.getId(),position.id(),settlement,document.getCliente().getId(),key,cash,
                request.paymentMethod(),request.paymentReference(),actor.getCodigo(),request.recalculationConfirmed());
        return new Issued(document.getId(),position.id(),settlement);
    }

    private static void require(boolean condition,String message) {
        if(!condition) throw new BadRequestException(message);
    }
}
