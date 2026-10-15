package com.ar2lda.fac.financial;

import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import static com.ar2lda.fac.financial.FinancialCore.Scope;
import static com.ar2lda.fac.financial.FinancialQueries.Page;

/** Internal diagnostic only. No controller/cutover consumer. Missing amounts are never zero-filled. */
@Service
@Transactional(readOnly=true)
public class FinancialReconciliation {
    public enum Finding {
        MISSING_PENDING, MISSING_ORIGIN, UNSUPPORTED_DOCUMENT_TYPE, UNSUPPORTED_CURRENCY,
        IDENTITY_MISMATCH, ACTIVE_LEGACY_DOCUMENT, CORE_RECEIPT, HISTORICAL_VALUE_DATE_MISSING,
        REVERSED_CREATION, UNDATED_CORE_EFFECT, DIFFERENCE_OBSERVED
    }
    public record Entry(long documentId,long clientId,String clientName,String reference,String currency,
                        LocalDate issueDate,boolean cancelled,Long pendingId,Long positionId,
                        BigDecimal legacyResidual,BigDecimal coreResidual,BigDecimal difference,
                        boolean comparable,boolean historicalLimit,
                        long activeLegacyLines,long coreReceiptLines,long nullValueDateLines,
                        BigDecimal consumed,BigDecimal restored,Set<Finding> findings) {
        public Entry { findings=Set.copyOf(findings); }
        /** Observed context, never an assertion that the difference is an accounting error. */
        public String explanation() {
            if(!comparable) return "Comparação incompleta: consultar os sinalizadores e as identidades disponíveis; valores ausentes não representam zero.";
            if(difference.signum()==0) return "Igualdade numérica no saldo corrente observado; não prova reconciliação histórica nem autoriza corte.";
            if(findings.contains(Finding.ACTIVE_LEGACY_DOCUMENT) && findings.contains(Finding.CORE_RECEIPT))
                return "Existem documentos legados activos e vínculos RC do núcleo, incluindo possíveis anulados; consultar receipts e allocations, sem atribuir causa única.";
            if(findings.contains(Finding.ACTIVE_LEGACY_DOCUMENT))
                return "Existem documentos legados activos que não escrevem no núcleo; consultar receipts e os vínculos para explicar a diferença, sem presumir erro contabilístico.";
            if(findings.contains(Finding.CORE_RECEIPT))
                return "Existem RC internos e o Pendente não é actualizado pelo emissor novo; consultar receipts e allocations, mantendo a autoridade operacional legada.";
            return "Diferença quantificada, mas a sua causa não está demonstrada pelos vínculos documentais disponíveis.";
        }
    }
    /** Independent indicators: legacy/core may overlap; never add their counts/amounts as a partition. */
    public record DifferenceGroup(long count,BigDecimal amount) {}
    /** Totals cover ONLY comparable documents; null totals mean no comparable observations. */
    public record Summary(long documents,long comparable,long different,long historicallyLimited,
                          long unmappedCorePositions,BigDecimal comparableLegacy,BigDecimal comparableCore,
                          BigDecimal comparableDifference,DifferenceGroup legacyAssociated,DifferenceGroup coreAssociated,
                          DifferenceGroup unexplained) {
        public boolean differenceGroupsMayOverlap() { return true; }
        public long nonComparable() { return documents-comparable; }
        public boolean fullComparisonCoverage() { return comparable==documents && unmappedCorePositions==0; }
    }
    public record Report(Scope scope,Page<Entry> page,Summary summary) {
        public boolean operationalActivationPermitted() { return false; }
    }
    /** One row per financial line. Core allocation history/RESTORE remains available via FinancialQueries.allocations. */
    public record ReceiptEvidence(long lineId,long documentId,String reference,String fiscalType,
                                  LocalDate issueDate,LocalDate valueDate,boolean cancelled,boolean coreLinked,
                                  Long operationId,Long allocationId,BigDecimal gross,BigDecimal discount,BigDecimal cash) {}
    private final JdbcTemplate db;
    public FinancialReconciliation(JdbcTemplate db) { this.db=db; }
    private static void check(Scope s,int offset,int limit) {
        // Documentary tables belong to the singleton company; never attribute them to another company.
        if(s==null || s.empresa()!=1 || s.cliente()<=0 || s.moeda()==null || !s.moeda().matches("[A-Z]{3}")
                || offset<0 || limit<1 || limit>500) throw new BadRequestException("Invalid reconciliation scope/page (company 1, limit 1..500)");
    }
    private static final String SELECTED="""
        with documents as materialized (
          select d.id,d.id_cliente,c.nome as client_name,d.id_moeda,d.data_emissao,d.anulado,d.valor_total,
            concat(d.id_tipo_documento,' ',d.serie,'/',d.numero_documento) as reference,
            coalesce(d.tipo_documento_codigo_fiscal,t.codigo_fiscal) as fiscal_type,t.liquidacao_imediata,
            pending_identity.id is not null as pending_exists,position_identity.id is not null as position_exists,
            ((pending_identity.id is not null and (pending.id is null
               or (pending.id_tipo_documento,pending.serie_documento,pending.numero_documento,pending.valor_documento)
                 is distinct from (d.id_tipo_documento,d.serie,d.numero_documento,d.valor_total)
               or pending.valor_pendente not between 0 and pending.valor_documento))
             or (position_identity.id is not null and (p.id is null or p.nature is distinct from 'DEBT'
               or p.original is distinct from d.valor_total or p.cancelled is distinct from d.anulado))) as identity_mismatch,
            pending.id as pending_id,pending.valor_pendente as legacy_residual,
            p.id as position_id,p.available as core_residual,p.creation_id,
            (pending.id_cliente=d.id_cliente and pending.id_moeda=d.id_moeda
             and pending.id_tipo_documento=d.id_tipo_documento and pending.serie_documento=d.serie
             and pending.numero_documento=d.numero_documento and pending.valor_documento=d.valor_total
             and pending.valor_pendente between 0 and pending.valor_documento
             and p.nature='DEBT' and p.original=d.valor_total and p.cancelled=d.anulado) as identity_valid
          from documento_comercial d join cliente c on c.id=d.id_cliente
          join tipodocumento t on t.id=d.id_tipo_documento
          left join pendente pending_identity on pending_identity.id_documento_comercial=d.id
          left join pendente pending on pending.id=pending_identity.id
            and pending.id_cliente=d.id_cliente and pending.id_moeda=d.id_moeda
          left join financial_document_origin origin on origin.document_id=d.id
          left join financial_position position_identity on position_identity.id=origin.position_id
          left join financial_position p on p.id=position_identity.id and p.empresa_id=?
            and p.cliente_id=d.id_cliente and p.moeda_id=d.id_moeda
          where d.id_cliente=? and d.id_moeda=? and d.estado in ('EMITIDO','ANULADO')
        ), line_stats as (
          select s.id,
            count(*) filter(where not f.anulado and r.document_id is null and f.id_cliente=s.id_cliente and f.id_moeda=s.id_moeda) as legacy_lines,
            count(*) filter(where r.document_id is not null and f.id_cliente=s.id_cliente and f.id_moeda=s.id_moeda) as core_lines,
            count(*) filter(where ft.codigo_fiscal='RC' and f.data_valor is null and f.id_cliente=s.id_cliente and f.id_moeda=s.id_moeda) as null_dates,
            count(*) filter(where ft.codigo_fiscal is distinct from 'RC' and f.id_cliente=s.id_cliente and f.id_moeda=s.id_moeda) as non_rc_lines,
            count(*) filter(where f.id_cliente<>s.id_cliente or f.id_moeda<>s.id_moeda) as invalid_lines
          from documents s join linha_documento_financeiro l on l.id_pendente=s.pending_id
          join documento_financeiro f on f.id=l.id_documento_financeiro
          join tipodocumento ft on ft.id=f.id_tipo_documento
          left join financial_receipt r on r.document_id=f.id
          group by s.id
        ), core_stats as (
          select s.id,sum(a.amount) filter(where a.effect='CONSUME') as consumed,
            sum(a.amount) filter(where a.effect='RESTORE') as restored,
            count(*) filter(where e.effect_date is null) as undated
          from documents s join financial_allocation a on a.position_id=s.position_id
          join financial_operation_effect e on e.id=a.operation_id group by s.id
        ), observations as (
          select s.*,coalesce(l.legacy_lines,0) as legacy_lines,coalesce(l.core_lines,0) as core_lines,
            coalesce(l.null_dates,0) as null_dates,coalesce(l.non_rc_lines,0) as non_rc_lines,coalesce(l.invalid_lines,0) as invalid_lines,
            cs.consumed,cs.restored,coalesce(cs.undated,0) as undated,
            exists(select 1 from financial_operation op where op.reverse_of=s.creation_id) as reversed_creation,
            coalesce(s.fiscal_type='FT' and not s.liquidacao_imediata and s.id_moeda='EUR'
              and s.pending_id is not null and s.position_id is not null and s.identity_valid
              and coalesce(l.non_rc_lines,0)=0 and coalesce(l.invalid_lines,0)=0,false) as comparable
          from documents s left join line_stats l on l.id=s.id left join core_stats cs on cs.id=s.id
        ), selected as (
          select o.*,case when comparable then legacy_residual-core_residual end as difference,
            (not comparable or reversed_creation or undated>0 or null_dates>0 or non_rc_lines>0 or position_id is null) as historical_limit
          from observations o
        )
        """;

    public Report inspect(Scope s,int offset,int limit) {
        check(s,offset,limit);
        return db.query(SELECTED+"""
            , totals as (
              select count(*) as document_count,count(*) filter(where comparable) as comparable_count,
                count(*) filter(where difference<>0) as different_count,
                count(*) filter(where historical_limit) as limited_count,
                sum(legacy_residual) filter(where comparable) as legacy_total,
                sum(core_residual) filter(where comparable) as core_total,
                sum(difference) filter(where comparable) as difference_total,
                count(*) filter(where difference<>0 and legacy_lines>0) as legacy_difference_count,
                sum(difference) filter(where difference<>0 and legacy_lines>0) as legacy_difference_amount,
                count(*) filter(where difference<>0 and core_lines>0) as core_difference_count,
                sum(difference) filter(where difference<>0 and core_lines>0) as core_difference_amount,
                count(*) filter(where difference<>0 and legacy_lines=0 and core_lines=0) as unexplained_count,
                sum(difference) filter(where difference<>0 and legacy_lines=0 and core_lines=0) as unexplained_amount
              from selected
            ), unmapped as (
              select count(*) as unmapped_count from financial_position p
              where p.empresa_id=? and p.cliente_id=? and p.moeda_id=?
                and not exists(select 1 from selected s where s.position_id=p.id)
            ), slice as (select * from selected order by id limit ? offset ?)
            select totals.*,unmapped.*,slice.* from totals cross join unmapped
              left join slice on true order by slice.id
            """,rs->{
                var entries=new ArrayList<Entry>();Summary summary=null;
                while(rs.next()) {
                    if(summary==null) summary=new Summary(rs.getLong("document_count"),rs.getLong("comparable_count"),
                        rs.getLong("different_count"),rs.getLong("limited_count"),rs.getLong("unmapped_count"),
                        rs.getBigDecimal("legacy_total"),rs.getBigDecimal("core_total"),rs.getBigDecimal("difference_total"),
                        new DifferenceGroup(rs.getLong("legacy_difference_count"),rs.getBigDecimal("legacy_difference_amount")),
                        new DifferenceGroup(rs.getLong("core_difference_count"),rs.getBigDecimal("core_difference_amount")),
                        new DifferenceGroup(rs.getLong("unexplained_count"),rs.getBigDecimal("unexplained_amount")));
                    if(rs.getObject("id")!=null) entries.add(entry(rs));
                }
                return new Report(s,new Page<>(entries,summary.documents(),offset,limit),summary);
            },s.empresa(),s.cliente(),s.moeda(),s.empresa(),s.cliente(),s.moeda(),limit,offset);
    }
    private static Entry entry(ResultSet rs) throws SQLException {
        var flags=EnumSet.noneOf(Finding.class);
        Long pending=rs.getObject("pending_id",Long.class),position=rs.getObject("position_id",Long.class);
        if(!rs.getBoolean("pending_exists")) flags.add(Finding.MISSING_PENDING);
        if(!rs.getBoolean("position_exists")) flags.add(Finding.MISSING_ORIGIN);
        if(!"FT".equals(rs.getString("fiscal_type")) || rs.getBoolean("liquidacao_imediata") || rs.getLong("non_rc_lines")>0)
            flags.add(Finding.UNSUPPORTED_DOCUMENT_TYPE);
        if(!"EUR".equals(rs.getString("id_moeda"))) flags.add(Finding.UNSUPPORTED_CURRENCY);
        if(rs.getBoolean("identity_mismatch") || rs.getLong("invalid_lines")>0) flags.add(Finding.IDENTITY_MISMATCH);
        if(rs.getLong("legacy_lines")>0) flags.add(Finding.ACTIVE_LEGACY_DOCUMENT);
        if(rs.getLong("core_lines")>0) flags.add(Finding.CORE_RECEIPT);
        if(rs.getLong("null_dates")>0) flags.add(Finding.HISTORICAL_VALUE_DATE_MISSING);
        if(rs.getBoolean("reversed_creation")) flags.add(Finding.REVERSED_CREATION);
        if(rs.getLong("undated")>0) flags.add(Finding.UNDATED_CORE_EFFECT);
        var diff=rs.getBigDecimal("difference");
        if(diff!=null && diff.signum()!=0) flags.add(Finding.DIFFERENCE_OBSERVED);
        return new Entry(rs.getLong("id"),rs.getLong("id_cliente"),rs.getString("client_name"),rs.getString("reference"),
                rs.getString("id_moeda"),rs.getObject("data_emissao",LocalDate.class),rs.getBoolean("anulado"),pending,position,
                rs.getBigDecimal("legacy_residual"),rs.getBigDecimal("core_residual"),diff,rs.getBoolean("comparable"),
                rs.getBoolean("historical_limit"),rs.getLong("legacy_lines"),rs.getLong("core_lines"),rs.getLong("null_dates"),
                position==null?null:Objects.requireNonNullElse(rs.getBigDecimal("consumed"),BigDecimal.ZERO),
                position==null?null:Objects.requireNonNullElse(rs.getBigDecimal("restored"),BigDecimal.ZERO),flags);
    }
    public Page<ReceiptEvidence> receipts(Scope s,long document,int offset,int limit) {
        check(s,offset,limit);
        if(document<=0) throw new BadRequestException("Invalid diagnostic document");
        return db.query("""
            with selected as (
              select l.id,f.id as receipt_id,concat(f.id_tipo_documento,' ',f.serie,'/',f.numero_documento) as reference,
                t.codigo_fiscal,f.data_emissao,f.data_valor,f.anulado,r.operation_id,link.allocation_id,
                l.valor_pagamento_bruto,l.valor_desconto_financeiro,l.valor_pagamento_liquido
              from documento_comercial d join pendente p on p.id_documento_comercial=d.id and p.id_cliente=d.id_cliente and p.id_moeda=d.id_moeda
              join linha_documento_financeiro l on l.id_pendente=p.id
              join documento_financeiro f on f.id=l.id_documento_financeiro
              join tipodocumento t on t.id=f.id_tipo_documento
              left join financial_receipt r on r.document_id=f.id
              left join financial_receipt_line link on link.line_id=l.id
              where d.id=? and d.id_cliente=? and d.id_moeda=? and d.estado in ('EMITIDO','ANULADO')
                and f.id_cliente=d.id_cliente and f.id_moeda=d.id_moeda
            ), total as (select count(*) as total from selected),
            slice as (select * from selected order by id limit ? offset ?)
            select total.total,slice.* from total left join slice on true order by slice.id
            """,rs->{
                long total=0;var rows=new ArrayList<ReceiptEvidence>();
                while(rs.next()) {
                    total=rs.getLong("total");if(rs.getObject("id")==null) continue;
                    rows.add(new ReceiptEvidence(rs.getLong("id"),rs.getLong("receipt_id"),rs.getString("reference"),
                        rs.getString("codigo_fiscal"),rs.getObject("data_emissao",LocalDate.class),rs.getObject("data_valor",LocalDate.class),
                        rs.getBoolean("anulado"),rs.getObject("operation_id")!=null,rs.getObject("operation_id",Long.class),
                        rs.getObject("allocation_id",Long.class),rs.getBigDecimal("valor_pagamento_bruto"),
                        rs.getBigDecimal("valor_desconto_financeiro"),rs.getBigDecimal("valor_pagamento_liquido")));
                }
                return new Page<>(rows,total,offset,limit);
            },document,s.cliente(),s.moeda(),limit,offset);
    }
}
