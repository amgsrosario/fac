package com.ar2lda.fac.financial;

import com.ar2lda.fac.model.DocumentoComercial;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.model.Utilizador;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Origination only. Pending balances and legacy receipts remain authoritative. */
@Service
public class FinancialFtOrigin {
    private final FinancialCore core;
    private final JdbcTemplate db;
    private final EntityManager entities;

    public FinancialFtOrigin(FinancialCore core, JdbcTemplate db, EntityManager entities) {
        this.core = core;
        this.db = db;
        this.entities = entities;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void originateNewEmission(DocumentoComercial document, Utilizador actor) {
        if (document.getEstado() != EstadoDocumentoComercial.EMITIDO
                || !"FT".equals(document.getTipoDocumento().getCodigoFiscal())
                || document.getTipoDocumento().isLiquidacaoImediata()
                || !"EUR".equals(document.getMoeda().getId())) {
            return;
        }
        entities.flush();
        var scope = new FinancialCore.Scope(Empresa.EMPRESA_ID, document.getCliente().getId(), document.getMoeda().getId());
        String origin = "DOCUMENTO_COMERCIAL:" + document.getId();
        var position = core.create(scope, "FT_ORIGIN:" + document.getId(), FinancialCore.Nature.DEBT,
                origin, document.getNumeroDocumentoCompleto(), document.getValorTotal(), actor.getCodigo());
        db.update("insert into financial_document_origin(document_id,position_id) values(?,?) on conflict(document_id) do nothing",
                document.getId(), position.id());
        Long linked = db.queryForObject("select position_id from financial_document_origin where document_id=?", Long.class, document.getId());
        if (!linked.equals(position.id())) {
            throw new IllegalStateException("Financial document origin mismatch");
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void reverseLinkedOrigin(DocumentoComercial document, Utilizador actor, String reason) {
        var positions = db.queryForList("select p.empresa_id,p.cliente_id,p.moeda_id,p.creation_id from financial_document_origin o join financial_position p on p.id=o.position_id where o.document_id=?", document.getId());
        if (positions.isEmpty()) return; // Legacy and excluded documents have no link. Never backfill here.
        var position = positions.getFirst();
        var scope = new FinancialCore.Scope(((Number) position.get("empresa_id")).longValue(),
                ((Number) position.get("cliente_id")).longValue(), (String) position.get("moeda_id"));
        core.reverse(scope, "FT_CANCEL:" + document.getId(), ((Number) position.get("creation_id")).longValue(), reason, actor.getCodigo());
    }
}
