package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;

/** Shape/tuple validation only. This class supplies no approved fiscal combination. */
public final class FtDeferredSemantics {
    private FtDeferredSemantics() { }
    public record Configuration(int areaGestao, int entidade, int sinalContabilistico) { }

    public static Set<Configuration> configurations(JsonNode definition) {
        fields(definition, Set.of("schemaVersion", "fiscalCode", "revision", "semantics"));
        require(integer(definition.get("schemaVersion"), 1, 1)
                && definition.path("fiscalCode").isTextual()
                && definition.path("fiscalCode").textValue().equals("FT")
                && definition.path("revision").isIntegralNumber()
                && definition.path("revision").canConvertToLong()
                && definition.path("revision").longValue() > 0);
        var semantics = definition.get("semantics");
        fields(semantics, Set.of("allowedConfigurations"));
        var entries = semantics.get("allowedConfigurations");
        require(entries.isArray() && !entries.isEmpty());
        Set<Configuration> result = new HashSet<>();
        for (var entry : entries) {
            fields(entry, Set.of("codigoFiscal", "areaGestao", "entidade", "sinalContabilistico", "liquidacaoImediata"));
            require(entry.path("codigoFiscal").isTextual() && entry.path("codigoFiscal").textValue().equals("FT")
                    && integer(entry.get("areaGestao"), 1, 99) && integer(entry.get("entidade"), 1, 9)
                    && integer(entry.get("sinalContabilistico"), 1, 2)
                    && entry.path("liquidacaoImediata").isBoolean() && !entry.path("liquidacaoImediata").booleanValue());
            require(result.add(new Configuration(entry.get("areaGestao").intValue(),
                    entry.get("entidade").intValue(), entry.get("sinalContabilistico").intValue())));
        }
        return Set.copyOf(result);
    }

    public static boolean permits(JsonNode definition, String code, Integer area, Integer entity, Integer sign, boolean immediate) {
        var allowed = configurations(definition);
        return "FT".equals(code) && !immediate && area != null && entity != null && sign != null
                && allowed.contains(new Configuration(area, entity, sign));
    }

    static void fields(JsonNode node, Set<String> expected) {
        require(node != null && node.isObject());
        Set<String> actual = new HashSet<>();
        node.fieldNames().forEachRemaining(actual::add);
        require(actual.equals(expected));
    }
    private static boolean integer(JsonNode node, int min, int max) {
        return node != null && node.isIntegralNumber() && node.canConvertToInt()
                && node.intValue() >= min && node.intValue() <= max;
    }
    private static void require(boolean valid) {
        if (!valid) throw new IllegalStateException("Invalid governed FT deferred semantics");
    }
}
