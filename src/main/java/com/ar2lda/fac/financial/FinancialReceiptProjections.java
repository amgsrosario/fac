package com.ar2lda.fac.financial;

import com.ar2lda.fac.controller.dto.PendenteDto;
import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.time.LocalDate;
import static com.ar2lda.fac.financial.FinancialCore.*;
import static com.ar2lda.fac.financial.FinancialQueries.*;

/** Isolated compatibility projection. Existing endpoints/dashboard/reporting are NOT switched in B1. */
@Service
@Transactional(readOnly=true)
public class FinancialReceiptProjections {
    private final JdbcTemplate db;
    private final FinancialQueries queries;
    public FinancialReceiptProjections(JdbcTemplate db,FinancialQueries queries) { this.db=db;this.queries=queries; }

    /** Same external debt DTO; only identifiers/metadata come from Pendente, NEVER its stored balance. */
    public Page<PendenteDto> debts(Scope s,Selection selection,int offset,int limit) {
        if(s==null || s.empresa()<=0 || s.cliente()<=0 || !"EUR".equals(s.moeda()) || selection==null || offset<0 || limit<1 || limit>500)
            throw new BadRequestException("Invalid projection scope/page");
        String state=switch(selection) {
            case ALL -> "";
            case OPEN -> " and not p.cancelled and p.available>0";
            case CLOSED -> " and (p.cancelled or p.available=0)";
        };
        String sql="""
            with selected as (
              select legacy.id, d.id as document_id,p.cliente_id,d.id_tipo_documento,d.numero_documento,d.serie,
                     p.original,p.available,d.data_emissao,d.data_vencimento,p.moeda_id
              from financial_position p join financial_document_origin o on o.position_id=p.id
              join documento_comercial d on d.id=o.document_id join pendente legacy on legacy.id_documento_comercial=d.id
              where p.empresa_id=? and p.cliente_id=? and p.moeda_id=? and p.nature='DEBT'
            """+state+"""
            ), total as (select count(*) as total from selected),
            slice as (select * from selected order by document_id,id limit ? offset ?)
            select total.total,slice.* from total left join slice on true order by slice.document_id,slice.id
            """;
        return db.query(sql,rs->{
            var result=new ArrayList<PendenteDto>();long total=0;
            while(rs.next()) {
                total=rs.getLong("total");if(rs.getObject("id")!=null) result.add(new PendenteDto(rs.getLong("id"),rs.getLong("document_id"),
                        rs.getLong("cliente_id"),rs.getString("id_tipo_documento"),rs.getLong("numero_documento"),rs.getString("serie"),
                        rs.getBigDecimal("original"),rs.getBigDecimal("available"),rs.getObject("data_emissao",LocalDate.class),
                        rs.getObject("data_vencimento",LocalDate.class),rs.getString("moeda_id")));
            }
            return new Page<>(result,total,offset,limit);
        },s.empresa(),s.cliente(),s.moeda(),limit,offset);
    }
    public Balance totals(Scope scope) { return queries.balance(scope); }

    public record Cutover(long missingOrigins,long activeLegacyReceipts,long unmappedPositions) {
        // Annullation effect and ALL operational consumers still need B2: data checks alone cannot enable B1.
        public boolean operationalActivationPermitted() { return false; }
        public boolean dataConsistent() { return missingOrigins==0 && activeLegacyReceipts==0 && unmappedPositions==0; }
    }
    public Cutover inspectCutover() {
        return db.queryForObject("""
            select
              (select count(*) from documento_comercial d join tipodocumento t on t.id=d.id_tipo_documento
               left join financial_document_origin o on o.document_id=d.id
               where d.estado='EMITIDO' and not d.anulado and t.codigo_fiscal='FT' and not t.liquidacao_imediata
                 and d.id_moeda='EUR' and o.document_id is null) as missing,
              (select count(distinct d.id) from documento_financeiro d join linha_documento_financeiro l on l.id_documento_financeiro=d.id
               left join financial_receipt r on r.document_id=d.id where not d.anulado and r.document_id is null) as legacy,
              (select count(*) from financial_position p left join financial_document_origin o on o.position_id=p.id
               left join pendente legacy on legacy.id_documento_comercial=o.document_id
               where p.empresa_id=1 and (o.position_id is null or legacy.id is null)) as unmapped
            """,(rs,n)->new Cutover(rs.getLong("missing"),rs.getLong("legacy"),rs.getLong("unmapped")));
    }
}
