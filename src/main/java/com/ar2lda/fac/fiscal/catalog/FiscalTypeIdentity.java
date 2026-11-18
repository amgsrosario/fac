package com.ar2lda.fac.fiscal.catalog;

import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

/** Java preflight plus the same transaction identity lock; V31 remains independent final authority. */
@Service
public class FiscalTypeIdentity {
    private final JdbcTemplate db;
    public FiscalTypeIdentity(JdbcTemplate db){this.db=db;}
    @Transactional(propagation=Propagation.MANDATORY)
    public void requireCompatibleCode(String code,String external) {
        var known=db.queryForList("select external_type from fiscal_type_identity where internal_code=?",String.class,code);
        if(known.stream().anyMatch(t->!t.equals(external)))throw new com.ar2lda.fac.exception.ConflictException("Internal code already identifies a different external fiscal type");
    }
    @Transactional(propagation=Propagation.MANDATORY)
    public void requireNewFrCode(String code){
        db.queryForList("select pg_advisory_xact_lock(hashtextextended(?,0))","FISCAL_CODE:"+code);
        var known=db.queryForList("select external_type from fiscal_type_identity where internal_code=?",String.class,code);
        if(known.stream().anyMatch(t->!"FR".equals(t)))throw new BadRequestException("Internal code already identifies a different external fiscal type");
        var history=db.queryForList("select coalesce(projection_external_type,tipo_documento_codigo_fiscal) as fiscal from documento_comercial where coalesce(tipo_documento_codigo,id_tipo_documento)=? and (estado in ('EMITIDO','ANULADO') or momento_emissao is not null)",code);
        if(history.stream().anyMatch(r->!"FR".equals(r.get("fiscal"))) || Boolean.TRUE.equals(db.queryForObject("select exists(select 1 from documento_financeiro where id_tipo_documento=?)",Boolean.class,code)))
            throw new BadRequestException("Internal code historical identity is not proven for FR");
    }
}
