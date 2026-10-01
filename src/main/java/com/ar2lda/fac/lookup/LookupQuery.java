package com.ar2lda.fac.lookup;

import java.text.Normalizer;
import java.util.*;

/** The existing EntityLookup grammar: known field qualifiers ANDed with an optional global term.
 * Unknown qualifiers/operators remain literal text, exactly as in the browser. */
public final class LookupQuery {
    private LookupQuery() {}
    public record Term(String field, String value) {}
    public static List<Term> parse(String search, Set<String> aliases, boolean qualified) {
        List<Term> result = new ArrayList<>();
        List<String> global = new ArrayList<>();
        String text = search == null ? "" : search.strip();
        if (!qualified) return text.isEmpty() ? result : List.of(new Term(null, text));
        for (String token : text.split("(?U)\\s+")) {
            int colon = token.indexOf(':');
            String alias = colon > 0 ? normalize(token.substring(0, colon)) : "";
            if (aliases.contains(alias)) result.add(new Term(alias, token.substring(colon + 1)));
            else if (!token.isEmpty()) global.add(token);
        }
        if (!global.isEmpty()) result.add(new Term(null, String.join(" ", global)));
        return result;
    }
    public static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[\\u0300-\\u036f]", "").strip().toLowerCase(Locale.ROOT);
    }
}
