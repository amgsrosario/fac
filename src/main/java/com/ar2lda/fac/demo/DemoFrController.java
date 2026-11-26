package com.ar2lda.fac.demo;

import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.financial.FinancialFrIssuer;
import com.ar2lda.fac.service.DocumentoComercialService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/** The projected invoice and its payment are one demo-only transaction. */
@RestController
@Profile("demo")
@ConditionalOnProperty(name="fac.demo.fr.enabled",havingValue="true")
@RequestMapping("/demo-fr")
@PreAuthorize("@functionalAuthorization.has(T(com.ar2lda.fac.model.PermissaoFuncional).DOCUMENTO_EMITIR)")
public class DemoFrController {
    private final FinancialFrIssuer issuer;
    private final DocumentoComercialService documents;
    private final Environment environment;
    private final JdbcTemplate db;
    public DemoFrController(FinancialFrIssuer issuer, DocumentoComercialService documents, Environment environment, JdbcTemplate db) {
        this.issuer=issuer; this.documents=documents; this.environment=environment; this.db=db;
    }
    public record Request(BigDecimal total, String modoPagamento, boolean recalculoConfirmado) {}

    @PostMapping("/{id}/emitir")
    public DocumentoComercialDto issue(@PathVariable long id, @RequestBody Request request) {
        DemoSignatureSource.validateIdentity(environment,db);
        DemoSignatureSource.validateSeries(db,environment);
        if (request==null || request.modoPagamento()==null || request.modoPagamento().isBlank())
            throw new com.ar2lda.fac.exception.BadRequestException("Indica o modo de pagamento da factura-recibo.");
        issuer.issueProjected("DEMO-FR:"+id,new FinancialFrIssuer.Request(id,request.total(),request.modoPagamento(),
                "Pagamento integral sintético da demo",request.recalculoConfirmado()));
        return documents.getById(id);
    }
}
