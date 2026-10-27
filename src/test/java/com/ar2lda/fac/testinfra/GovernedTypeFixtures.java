package com.ar2lda.fac.testinfra;
import com.ar2lda.fac.model.TipoDocumento;
import org.springframework.jdbc.core.JdbcTemplate;

/** Synthetic regression-only definitions. Never imported into a production resource or seed. */
public final class GovernedTypeFixtures {
    public static final long REVISION=9000001L;
    private GovernedTypeFixtures() { }
    private static final String DEFINITION="""
        {"schemaVersion":1,"fiscalCode":"FT","revision":9000001,"semantics":{"allowedConfigurations":[
          {"codigoFiscal":"FT","areaGestao":1,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":false},
          {"codigoFiscal":"FT","areaGestao":2,"entidade":1,"sinalContabilistico":1,"liquidacaoImediata":false}]}}
        """;
    public static void publishSynthetic(JdbcTemplate db) {
        db.update("insert into fiscal_catalog_revision(fiscal_code,revision,definition) values('FT',?,?::jsonb) on conflict do nothing",REVISION,DEFINITION);
        Boolean equal=db.queryForObject("select definition=?::jsonb from fiscal_catalog_revision where fiscal_code='FT' and revision=?",Boolean.class,DEFINITION,REVISION);
        if(!Boolean.TRUE.equals(equal)) throw new IllegalStateException("Synthetic fixture identity conflict");
    }
    public static TipoDocumento bind(JdbcTemplate db,TipoDocumento type) {
        if(!"FT".equals(type.getCodigoFiscal()) || type.isLiquidacaoImediata()) return type;
        publishSynthetic(db);
        return TipoDocumento.governed(type,"FT",REVISION);
    }
}
