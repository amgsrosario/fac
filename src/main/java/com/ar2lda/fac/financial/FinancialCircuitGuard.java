package com.ar2lda.fac.financial;

import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Called under the shared Pendente write lock, before reserving a receipt number. */
@Service
public class FinancialCircuitGuard {
    private final JdbcTemplate db;
    public FinancialCircuitGuard(JdbcTemplate db) { this.db=db; }
    public void requireLegacyEligible(long pending) {
        if(Boolean.TRUE.equals(db.queryForObject("""
            select exists(select 1 from pendente p
              join financial_document_origin o on o.document_id=p.id_documento_comercial
              join financial_allocation a on a.position_id=o.position_id
              where p.id=? and a.effect='CONSUME'
                and not exists(select 1 from financial_allocation r where r.reverse_of=a.id))
            """,Boolean.class,pending)))
            throw new BadRequestException("Debt has active FinancialCore consumption; legacy receipt forbidden");
    }
    public void requireCoreEligible(long pending) {
        if(Boolean.TRUE.equals(db.queryForObject("""
            select exists(select 1 from linha_documento_financeiro l
              join documento_financeiro d on d.id=l.id_documento_financeiro
              left join financial_receipt r on r.document_id=d.id
              where l.id_pendente=? and not d.anulado and r.document_id is null)
            """,Boolean.class,pending)))
            throw new BadRequestException("Invoice has unreconciled legacy receipts");
    }
}
