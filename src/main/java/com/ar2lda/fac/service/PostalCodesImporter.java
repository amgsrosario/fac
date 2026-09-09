package com.ar2lda.fac.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class PostalCodesImporter {
    private static final Pattern POSTAL_CODE = Pattern.compile("\\d{4}-\\d{3}");
    private static final int EXPECTED_COLUMNS = 12;
    private static final int BATCH_SIZE = 1000;
    private final JdbcTemplate jdbc;

    public PostalCodesImporter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Report inspect(Path zip) {
        return parse(zip);
    }

    @Transactional
    public Report importData(Path zip) {
        Report report = parse(zip);
        if (!report.errors().isEmpty()) {
            throw new IllegalArgumentException("PT.zip contém erros estruturais: " + report.errors().get(0));
        }
        String database = jdbc.queryForObject("select current_database()", String.class);
        if (!"fac_demo".equals(database)) {
            throw new IllegalStateException("Importação postal recusada fora de fac_demo: " + database);
        }
        jdbc.queryForObject("select pg_advisory_xact_lock(hashtext('fac.postal-import'))", Object.class);
        List<Group> groups = report.groups();
        for (int offset = 0; offset < groups.size(); offset += BATCH_SIZE) {
            List<Group> batch = groups.subList(offset, Math.min(offset + BATCH_SIZE, groups.size()));
            jdbc.batchUpdate("""
                    insert into codpostal(id, nome, distrito, concelho, freguesia)
                    values (?, ?, ?, ?, ?)
                    on conflict (id) do update set nome=excluded.nome,
                        distrito=excluded.distrito, concelho=excluded.concelho, freguesia=excluded.freguesia
                    """, batch, batch.size(), (PreparedStatement ps, Group g) -> {
                ps.setString(1, g.code()); ps.setString(2, g.primaryLocality());
                ps.setString(3, g.district()); ps.setString(4, g.municipality()); ps.setString(5, g.parish());
            });
            jdbc.batchUpdate("delete from codpostal_localidade where codpostal_id = ?", batch, batch.size(),
                    (PreparedStatement ps, Group g) -> ps.setString(1, g.code()));
            List<LocalityRow> localities = batch.stream().flatMap(g -> g.localities().stream()
                    .map(name -> new LocalityRow(g.code(), name))).toList();
            jdbc.batchUpdate("insert into codpostal_localidade(codpostal_id, nome) values (?, ?) on conflict do nothing",
                    localities, localities.size(), (PreparedStatement ps, LocalityRow row) -> {
                ps.setString(1, row.code()); ps.setString(2, row.name());
            });
        }
        return report;
    }

    private Report parse(Path zipPath) {
        if (zipPath == null || !Files.isRegularFile(zipPath)) throw new IllegalArgumentException("Ficheiro não encontrado: " + zipPath);
        if (!zipPath.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".zip"))
            throw new IllegalArgumentException("O ficheiro de entrada deve ter extensão .zip");
        Map<String, MutableGroup> groups = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        long lines = 0, valid = 0, malformed = 0, invalidCodes = 0, nonPortugal = 0, emptyNames = 0, duplicateLocalities = 0;
        int maxLocalityLength = 0, maxDistrictLength = 0, maxMunicipalityLength = 0, maxParishLength = 0;
        try (ZipFile zip = new ZipFile(zipPath.toFile(), StandardCharsets.UTF_8)) {
            ZipEntry entry = zip.getEntry("PT.txt");
            if (entry == null) throw new IllegalArgumentException("O ZIP não contém PT.txt");
            try (InputStream in = zip.getInputStream(entry);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, decoder()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines++;
                    String[] c = line.split("\\t", -1);
                    if (c.length != EXPECTED_COLUMNS) { malformed++; errors.add("linha " + lines + ": esperadas 12 colunas"); continue; }
                    String country = clean(c[0]), code = clean(c[1]), locality = clean(c[2]);
                    if (!"PT".equalsIgnoreCase(country)) { nonPortugal++; errors.add("linha " + lines + ": país diferente de PT"); continue; }
                    if (!POSTAL_CODE.matcher(code).matches()) { invalidCodes++; errors.add("linha " + lines + ": código postal inválido " + code); continue; }
                    if (locality.isBlank()) { emptyNames++; errors.add("linha " + lines + ": localidade vazia"); continue; }
                    if (locality.length() > 180) errors.add("linha " + lines + ": localidade excede 180 caracteres");
                    maxLocalityLength = Math.max(maxLocalityLength, locality.length());
                    maxDistrictLength = Math.max(maxDistrictLength, clean(c[3]).length());
                    maxMunicipalityLength = Math.max(maxMunicipalityLength, clean(c[5]).length());
                    maxParishLength = Math.max(maxParishLength, clean(c[7]).length());
                    MutableGroup group = groups.computeIfAbsent(code, MutableGroup::new);
                    if (!group.localities.add(locality)) duplicateLocalities++;
                    group.districts.add(clean(c[3])); group.municipalities.add(clean(c[5])); group.parishes.add(clean(c[7]));
                    valid++;
                }
            } catch (CharacterCodingException e) {
                throw new IllegalArgumentException("PT.txt não está em UTF-8", e);
            }
        } catch (IOException e) { throw new IllegalArgumentException("Não foi possível ler PT.zip", e); }
        List<Group> result = groups.values().stream().map(MutableGroup::freeze).sorted(Comparator.comparing(Group::code)).toList();
        List<String> collisions = result.stream().filter(g -> g.districts().size() > 1 || g.municipalities().size() > 1 || g.parishes().size() > 1)
                .map(g -> g.code() + " distrito=" + g.districts() + " concelho=" + g.municipalities() + " freguesia=" + g.parishes()).toList();
        errors.addAll(collisions.stream().map(s -> "COLISÃO GEOGRÁFICA: " + s).toList());
        long localityCount = result.stream().mapToLong(g -> g.localities().size()).sum();
        long multiLocality = result.stream().filter(g -> g.localities().size() > 1).count();
        long maxMultiplicity = result.stream().mapToLong(g -> g.localities().size()).max().orElse(0);
        long overlongPrincipal = result.stream().filter(g -> g.primaryLocality().length() > 50).count();
        if (overlongPrincipal > 0) errors.add(overlongPrincipal + " localidades principais excedem 50 caracteres");
        return new Report(lines, valid, lines - valid, result.size(), multiLocality, maxMultiplicity, localityCount, duplicateLocalities,
                result.stream().flatMap(g -> g.districts().stream()).collect(java.util.stream.Collectors.toCollection(TreeSet::new)).size(),
                result.stream().flatMap(g -> g.municipalities().stream()).collect(java.util.stream.Collectors.toCollection(TreeSet::new)).size(),
                result.stream().flatMap(g -> g.parishes().stream()).collect(java.util.stream.Collectors.toCollection(TreeSet::new)).size(),
                maxLocalityLength, maxDistrictLength, maxMunicipalityLength, maxParishLength,
                malformed, invalidCodes, nonPortugal, emptyNames, collisions, errors, result);
    }

    private static java.nio.charset.CharsetDecoder decoder() {
        return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
    }
    private static String clean(String value) { return value == null ? "" : value.strip(); }

    public record Report(long linesRead, long validLines, long invalidLines, long uniqueCodes, long multiLocalityCodes,
                         long maxLocalityMultiplicity, long localityAssociations, long duplicateLocalities, long districts, long municipalities,
                         long parishes, int maxLocalityLength, int maxDistrictLength, int maxMunicipalityLength, int maxParishLength,
                         long malformedLines, long invalidCodes, long nonPortugalLines, long emptyNames,
                         List<String> geographicCollisions, List<String> errors, List<Group> groups) {
        public boolean importable() { return errors.isEmpty(); }
    }
    public record Group(String code, String primaryLocality, String district, String municipality, String parish, Set<String> localities,
                        Set<String> districts, Set<String> municipalities, Set<String> parishes) {}
    private record LocalityRow(String code, String name) {}
    private static final class MutableGroup {
        private final String code; private final Set<String> localities = new LinkedHashSet<>();
        private final Set<String> districts = new LinkedHashSet<>(), municipalities = new LinkedHashSet<>(), parishes = new LinkedHashSet<>();
        private MutableGroup(String code) { this.code = code; }
        private Group freeze() {
            Set<String> l = Collections.unmodifiableSet(new TreeSet<>(localities));
            Set<String> d = Collections.unmodifiableSet(new TreeSet<>(districts)), m = Collections.unmodifiableSet(new TreeSet<>(municipalities)), p = Collections.unmodifiableSet(new TreeSet<>(parishes));
            return new Group(code, l.iterator().next(), d.iterator().next(), m.iterator().next(), p.iterator().next(), l, d, m, p);
        }
    }
}
