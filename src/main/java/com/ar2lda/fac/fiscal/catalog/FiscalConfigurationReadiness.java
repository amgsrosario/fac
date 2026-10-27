package com.ar2lda.fac.fiscal.catalog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Internal post-migrate/restore inventory, not an endpoint, an emission guard or fiscal approval. */
@Service
@Transactional(readOnly=true)
public class FiscalConfigurationReadiness {
    public enum Finding { UNBOUND_FT_DEFERRED, UNCLASSIFIED_EMISSION_PATH, EMISSION_HISTORY_WITHOUT_IDENTITY,
        CATALOG_UNAVAILABLE, INCOMPATIBLE_BINDING }
    public record Row(String id, String fiscalCode, Integer area, boolean immediate,
                      String catalogCode, Long catalogRevision, long commercialHistory, long financialHistory,
                      Set<Finding> findings) {
        public Row { findings=Set.copyOf(findings); }
    }
    public record Report(List<Row> rows, boolean creationBindingAvailable) {
        public Report { rows=List.copyOf(rows); }
        public boolean configurationReady() { return creationBindingAvailable && rows.stream().allMatch(r->r.findings().isEmpty()); }
        public boolean operationalActivationPermitted() { return false; }
    }
    private final JdbcTemplate db;
    private final FiscalCatalog catalog;
    public FiscalConfigurationReadiness(JdbcTemplate db, FiscalCatalog catalog) { this.db=db; this.catalog=catalog; }
    public Report inspect() {
        Map<FiscalCatalog.Key,FiscalCatalog.Revision> revisions;
        boolean coherent=true, creation=false;
        try { revisions=catalog.verifiedSnapshot(); }
        catch (IllegalStateException error) { revisions=Map.of(); coherent=false; }
        if (coherent) {
            try { catalog.ftDeferredCreation(); creation=true; }
            catch (IllegalStateException error) { /* Gate closed, inventory remains available. */ }
        }
        final var snapshot=revisions;
        final boolean available=coherent;
        var rows=db.query("""
            with commercial as (
              select id_tipo_documento,count(*) n from documento_comercial
              where estado in ('EMITIDO','ANULADO') or momento_emissao is not null group by id_tipo_documento
            ), financial as (
              select id_tipo_documento,count(*) n from documento_financeiro group by id_tipo_documento
            )
            select t.*,coalesce(c.n,0) commercial_history,coalesce(f.n,0) financial_history
            from tipodocumento t left join commercial c on c.id_tipo_documento=t.id
              left join financial f on f.id_tipo_documento=t.id order by t.id
            """,(rs,index)->{
            String id=rs.getString("id"), code=rs.getString("codigo_fiscal"), binding=rs.getString("fiscal_catalog_code");
            Long revision=rs.getObject("fiscal_catalog_revision",Long.class);
            int area=rs.getInt("area_gestao");boolean immediate=rs.getBoolean("liquidacao_imediata");
            long commercial=rs.getLong("commercial_history"), financial=rs.getLong("financial_history");
            Set<Finding> findings=EnumSet.noneOf(Finding.class);
            if (binding==null && "FT".equals(code) && !immediate) findings.add(Finding.UNBOUND_FT_DEFERRED);
            // Areas identify implemented paths only; never infer a fiscal type from ID/name/area.
            if (code==null || code.isBlank()) {
                if (area>=1 && area<=3) findings.add(Finding.UNCLASSIFIED_EMISSION_PATH);
                if (commercial>0 || financial>0) findings.add(Finding.EMISSION_HISTORY_WITHOUT_IDENTITY);
            }
            if (binding!=null) {
                var governed=snapshot.get(new FiscalCatalog.Key(binding,revision));
                if (!available || governed==null) findings.add(Finding.CATALOG_UNAVAILABLE);
                else {
                    try {
                        if (!FtDeferredSemantics.permits(governed.definition(),code,area,rs.getInt("entidade"),rs.getInt("sinal_contabilistico"),immediate))
                            findings.add(Finding.INCOMPATIBLE_BINDING);
                    } catch (IllegalStateException error) { findings.add(Finding.INCOMPATIBLE_BINDING); }
                }
            }
            return new Row(id,code,area,immediate,binding,revision,commercial,financial,findings);
        });
        return new Report(rows,creation);
    }
}
