package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

/** Schema v2 is explicit FR projection, never inferred from an immediate flag alone. */
public final class FrFiscalSemantics {
    private FrFiscalSemantics() { }
    public static void validate(JsonNode d) {
        FtDeferredSemantics.fields(d, Set.of("schemaVersion","fiscalCode","revision","semantics"));
        check(d.path("schemaVersion").isIntegralNumber() && d.path("schemaVersion").canConvertToInt() && d.path("schemaVersion").intValue()==2
            && "FT".equals(d.path("fiscalCode").asText()) && d.path("revision").isIntegralNumber()
            && d.path("revision").canConvertToLong() && d.path("revision").longValue()>0);
        var s=d.get("semantics");
        FtDeferredSemantics.fields(s,Set.of("specialization","externalType","documentLabel","allowedConfigurations"));
        check("FR".equals(s.path("specialization").asText()) && "FR".equals(s.path("externalType").asText())
            && "Fatura-recibo".equals(s.path("documentLabel").asText()));
        var rows=s.get("allowedConfigurations");check(rows.isArray() && !rows.isEmpty());
        Set<List<Integer>> tuples=new HashSet<>();
        for(var r:rows) {
            FtDeferredSemantics.fields(r,Set.of("codigoFiscal","areaGestao","entidade","sinalContabilistico","liquidacaoImediata"));
            check("FT".equals(r.path("codigoFiscal").asText()) && r.path("liquidacaoImediata").isBoolean()
                && r.path("liquidacaoImediata").booleanValue());
            check(integer(r.get("areaGestao"),1,99) && integer(r.get("entidade"),1,9) && integer(r.get("sinalContabilistico"),1,2));
            check(tuples.add(List.of(r.get("areaGestao").intValue(),r.get("entidade").intValue(),r.get("sinalContabilistico").intValue())));
        }
    }
    public static boolean permits(JsonNode d,String code,Integer area,Integer entity,Integer sign,boolean immediate) {
        validate(d);
        if(!"FT".equals(code)||!immediate||area==null||entity==null||sign==null)return false;
        for(var r:d.path("semantics").path("allowedConfigurations"))
            if(r.path("areaGestao").intValue()==area && r.path("entidade").intValue()==entity && r.path("sinalContabilistico").intValue()==sign)return true;
        return false;
    }
    private static boolean integer(JsonNode n,int min,int max){return n!=null&&n.isIntegralNumber()&&n.canConvertToInt()&&n.intValue()>=min&&n.intValue()<=max;}
    private static void check(boolean b){if(!b)throw new IllegalStateException("Invalid governed FR projection");}
}
