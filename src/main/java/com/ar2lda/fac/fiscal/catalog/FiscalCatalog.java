package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/** Governed catalog reader. Lazy validation; no startup activation or fiscal definitions supplied. */
@Service
public final class FiscalCatalog {
    private final JdbcTemplate db;
    private final ObjectMapper json = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private final Resource source;

    public record Key(String fiscalCode, long revision) { }
    public record Revision(Key key, JsonNode definition) {
        public Revision { definition = definition.deepCopy(); }
        @Override public JsonNode definition() { return definition.deepCopy(); }
    }

    @org.springframework.beans.factory.annotation.Autowired
    public FiscalCatalog(JdbcTemplate db, org.springframework.core.env.Environment environment) {
        this(db, new ClassPathResource(demoCatalog(environment) ? "demo/saft-synthetic-catalog.json" : "fiscal/catalog.json"));
    }

    private static boolean demoCatalog(org.springframework.core.env.Environment environment) {
        boolean enabled=environment.getProperty("fac.demo.saft.synthetic-catalog",Boolean.class,false);
        if (enabled && (!java.util.Arrays.equals(environment.getActiveProfiles(),new String[]{"demo"})
                || !environment.getProperty("fac.demo.enabled",Boolean.class,false)))
            throw new IllegalStateException("O catálogo sintético SAF-T exige o perfil demo exclusivo");
        return enabled;
    }

    FiscalCatalog(JdbcTemplate db) { this(db, new ClassPathResource("fiscal/catalog.json")); }

    FiscalCatalog(JdbcTemplate db, Resource source) {
        this.db = db;
        this.source = source;
    }

    /** Parse only on use: an inactive catalog cannot prevent legacy bean initialization. */
    private record Manifest(Map<Key, JsonNode> revisions, Optional<Key> ftDeferred, Optional<Key> frImmediate) { }

    private Manifest loadManifest() {
        try (var input = source.getInputStream()) {
            var manifest = json.readTree(input);
            if (manifest == null || !manifest.path("schemaVersion").isIntegralNumber()
                    || !manifest.path("schemaVersion").canConvertToInt()
                    || manifest.path("schemaVersion").intValue() != 1
                    || !manifest.path("revisions").isArray()) {
                throw new IllegalStateException("Invalid fiscal catalog manifest");
            }
            Set<String> rootFields = new HashSet<>();
            manifest.fieldNames().forEachRemaining(rootFields::add);
            if (!Set.of("schemaVersion", "revisions", "creationBindings").containsAll(rootFields))
                throw new IllegalStateException("Invalid fiscal catalog manifest fields");
            Map<Key, JsonNode> definitions = new HashMap<>();
            for (JsonNode entry : manifest.path("revisions")) {
                if (!entry.path("fiscalCode").isTextual() || entry.path("fiscalCode").asText().length() != 2
                        || !entry.path("revision").isIntegralNumber() || !entry.path("revision").canConvertToLong()
                        || entry.path("revision").longValue() <= 0 || !entry.path("semantics").isObject()
                        || !entry.path("schemaVersion").isIntegralNumber()
                        || !entry.path("schemaVersion").canConvertToInt()
                        || (entry.path("schemaVersion").intValue() != 1 && entry.path("schemaVersion").intValue() != 2)) {
                    throw new IllegalStateException("Invalid fiscal catalog revision");
                }
                if (entry.path("schemaVersion").intValue()==2) FrFiscalSemantics.validate(entry);
                else if ("FT".equals(entry.path("fiscalCode").textValue())) FtDeferredSemantics.configurations(entry);
                Key key = new Key(entry.path("fiscalCode").textValue(), entry.path("revision").longValue());
                if (definitions.putIfAbsent(key, entry.deepCopy()) != null) {
                    throw new IllegalStateException("Duplicate fiscal catalog revision");
                }
            }
            Optional<Key> creation=Optional.empty(), immediate=Optional.empty();
            if(manifest.has("creationBindings")) {
                var bindings=manifest.get("creationBindings");
                if(!bindings.isObject() || bindings.isEmpty())throw new IllegalStateException("Invalid creation bindings");
                var names=bindings.fieldNames();
                while(names.hasNext()) {
                    String name=names.next();if(!Set.of("ftDeferred","frImmediate").contains(name))throw new IllegalStateException("Unknown creation binding");
                    var key=bindings.get(name);FtDeferredSemantics.fields(key,Set.of("fiscalCode","revision"));
                    if(!"FT".equals(key.path("fiscalCode").asText())||!key.path("revision").isIntegralNumber()
                        ||!key.path("revision").canConvertToLong()||key.path("revision").longValue()<=0)throw new IllegalStateException("Invalid creation binding");
                    var selected=new Key("FT",key.path("revision").longValue());var definition=definitions.get(selected);
                    if(definition==null || definition.path("schemaVersion").intValue()!=(name.equals("frImmediate")?2:1))throw new IllegalStateException("Creation binding has incompatible semantics");
                    if(name.equals("frImmediate"))immediate=Optional.of(selected);else creation=Optional.of(selected);
                }
            }
            return new Manifest(Map.copyOf(definitions),creation,immediate);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read fiscal catalog manifest", e);
        }
    }

    /** Checks the whole materialization in one query. Empty is valid while inactive. */
    public void verifyCoherence() { verifiedRevisions(); }

    /** A reviewed release pointer, never latest/max and never chosen by an API client. */
    public Revision ftDeferredCreation() {
        var manifest = loadManifest();
        var actual = verifiedRevisions(manifest);
        return actual.get(manifest.ftDeferred().orElseThrow(() ->
                new IllegalStateException("Governed FT creation is blocked: approved creation binding unavailable")));
    }

    public Revision frImmediateCreation() {
        var manifest=loadManifest();var actual=verifiedRevisions(manifest);
        return actual.get(manifest.frImmediate().orElseThrow(()->new IllegalStateException("Governed FR creation binding unavailable")));
    }

    /** Existing v1/unbound consumers do not start using the inactive catalog by accident. */
    public Optional<Revision> frForType(String typeId) {
        Boolean projected=db.queryForObject("select exists(select 1 from tipodocumento t join fiscal_catalog_revision r on r.fiscal_code=t.fiscal_catalog_code and r.revision=t.fiscal_catalog_revision where t.id=? and r.definition->>'schemaVersion'='2')",Boolean.class,typeId);
        return Boolean.TRUE.equals(projected)?forType(typeId):Optional.empty();
    }

    public Revision revision(String code, long revision) {
        var found = verifiedRevisions().get(new Key(code, revision));
        if (found == null) throw new IllegalStateException("Fiscal catalog revision is not available");
        return found;
    }

    /** No fallback to current masters/latest revision for unbound historical types. */
    public Optional<Revision> forType(String typeId) {
        var revisions = verifiedRevisions();
        return db.query("select fiscal_catalog_code,fiscal_catalog_revision from tipodocumento where id=?",
                (rs, index) -> {
                    String code = rs.getString(1);
                    return code == null ? Optional.<Revision>empty()
                            : Optional.ofNullable(revisions.get(new Key(code, rs.getLong(2))))
                                .or(() -> { throw new IllegalStateException("Fiscal catalog binding has no governed revision"); });
                }, typeId).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown document type"));
    }

    Map<Key, Revision> verifiedSnapshot() { return Map.copyOf(verifiedRevisions()); }

    private Map<Key, Revision> verifiedRevisions() {
        return verifiedRevisions(loadManifest());
    }

    private Map<Key, Revision> verifiedRevisions(Manifest manifest) {
        var expected = manifest.revisions();
        Map<Key, Revision> actual = new HashMap<>();
        db.query("select fiscal_code,revision,definition::text from fiscal_catalog_revision", rs -> {
            Key key = new Key(rs.getString(1), rs.getLong(2));
            JsonNode definition;
            try { definition = json.readTree(rs.getString(3)); }
            catch (IOException e) { throw new IllegalStateException("Invalid persisted catalog definition", e); }
            var source = expected.get(key);
            // jsonb numeric equality compares values independently of decimal scale;
            // its text rendering may retain trailing zeros. Never compare through doubles.
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
