package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/** Internal, inactive catalog reader. No startup hook, editor or operational callers. */
@Service
public final class FiscalCatalog {
    private final JdbcTemplate db;
    private final ObjectMapper json = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private final Map<Key, JsonNode> expected;

    public record Key(String fiscalCode, long revision) { }
    public record Revision(Key key, JsonNode definition) {
        public Revision { definition = definition.deepCopy(); }
        @Override public JsonNode definition() { return definition.deepCopy(); }
    }

    @org.springframework.beans.factory.annotation.Autowired
    public FiscalCatalog(JdbcTemplate db) {
        this(db, new ClassPathResource("fiscal/catalog.json"));
    }

    FiscalCatalog(JdbcTemplate db, Resource source) {
        this.db = db;
        try (var input = source.getInputStream()) {
            var manifest = json.readTree(input);
            if (!manifest.path("schemaVersion").isIntegralNumber()
                    || !manifest.path("schemaVersion").canConvertToInt()
                    || manifest.path("schemaVersion").intValue() != 1
                    || !manifest.path("revisions").isArray()) {
                throw new IllegalStateException("Invalid fiscal catalog manifest");
            }
            Map<Key, JsonNode> definitions = new HashMap<>();
            for (JsonNode entry : manifest.path("revisions")) {
                if (!entry.path("fiscalCode").isTextual() || entry.path("fiscalCode").asText().length() != 2
                        || !entry.path("revision").isIntegralNumber() || !entry.path("revision").canConvertToLong()
                        || entry.path("revision").longValue() <= 0 || !entry.path("semantics").isObject()
                        || !entry.path("schemaVersion").isIntegralNumber()
                        || !entry.path("schemaVersion").canConvertToInt()
                        || entry.path("schemaVersion").intValue() != 1) {
                    throw new IllegalStateException("Invalid fiscal catalog revision");
                }
                Key key = new Key(entry.path("fiscalCode").textValue(), entry.path("revision").longValue());
                if (definitions.putIfAbsent(key, entry.deepCopy()) != null) {
                    throw new IllegalStateException("Duplicate fiscal catalog revision");
                }
            }
            expected = Map.copyOf(definitions);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read fiscal catalog manifest", e);
        }
    }

    /** Checks the whole materialization in one query. Empty is valid while inactive. */
    public void verifyCoherence() { verifiedRevisions(); }

    public Revision revision(String code, long revision) {
        var found = verifiedRevisions().get(new Key(code, revision));
        if (found == null) throw new IllegalStateException("Fiscal catalog revision is not available");
        return found;
    }

    /** No fallback to current masters/latest revision for unbound historical types. */
    public Optional<Revision> forType(String typeId) {
        return db.query("select fiscal_catalog_code,fiscal_catalog_revision from tipodocumento where id=?",
                (rs, index) -> {
                    String code = rs.getString(1);
                    return code == null ? Optional.<Revision>empty()
                            : Optional.of(revision(code, rs.getLong(2)));
                }, typeId).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown document type"));
    }

    private Map<Key, Revision> verifiedRevisions() {
        Map<Key, Revision> actual = new HashMap<>();
        db.query("select fiscal_code,revision,definition::text from fiscal_catalog_revision", rs -> {
            Key key = new Key(rs.getString(1), rs.getLong(2));
            JsonNode definition;
            try { definition = json.readTree(rs.getString(3)); }
            catch (IOException e) { throw new IllegalStateException("Invalid persisted catalog definition", e); }
            var source = expected.get(key);
            // PostgreSQL jsonb ignores decimal scale; compare exact decimal values, never doubles.
            if (source == null || !source.equals((a, b) -> a.isNumber() && b.isNumber()
                    ? a.decimalValue().compareTo(b.decimalValue()) : a.equals(b) ? 0 : 1, definition)) {
                throw new IllegalStateException("Fiscal catalog materialization differs from governed manifest");
            }
            actual.put(key, new Revision(key, definition));
        });
        if (!actual.keySet().equals(expected.keySet())) {
            throw new IllegalStateException("Fiscal catalog materialization is incomplete");
        }
        return Map.copyOf(actual);
    }
}
