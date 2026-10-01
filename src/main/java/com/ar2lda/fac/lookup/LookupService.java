package com.ar2lda.fac.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LookupService {
    private final NamedParameterJdbcTemplate jdbc;
    private static final Pattern IVA_NUMBER = Pattern.compile("(\\d+(?:[,.]\\d+)?)");

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> find(boolean article, MultiValueMap<String, String> request) {
        int page = number(request.getFirst("page"), 0, Integer.MAX_VALUE);
        int size = number(request.getFirst("size"), 20, 50);
        if (size == 0) throw bad("size deve ser positivo");
        String context = Optional.ofNullable(request.getFirst("context")).orElse("editor");
        if (!Set.of("editor", "global", "list", "listSimple", "statement").contains(context)) throw bad("Contexto desconhecido");
        Map<String, Object> params = new HashMap<>();
        Map<String, String> fields = article ? articleFields() : clientFields();
        Map<String, List<String>> aliases = new LinkedHashMap<>();
        List<String> global;
        if (article) {
            aliases.put("codigo", List.of(fields.get("codigo"))); aliases.put("id", aliases.get("codigo"));
            for (String name : List.of("descricao", "unidade", "pvp")) aliases.put(name, List.of(fields.get(name)));
            aliases.put("familia", List.of(fields.get("familiaId")));
            if (context.equals("editor")) aliases.put("iva", List.of(ivaExpression(params)));
            global = context.equals("global") ? List.of(fields.get("codigo"), fields.get("descricao"), fields.get("familiaId"), fields.get("unidade"))
                    : aliases.entrySet().stream().filter(e -> !e.getKey().equals("id")).flatMap(e -> e.getValue().stream()).toList();
        } else {
            for (String name : List.of("nome", "nif", "localidade", "email", "id")) aliases.put(name, List.of(fields.get(name)));
            aliases.put("telefone", List.of(fields.get("tel"), fields.get("tm")));
            aliases.put("telemovel", aliases.get("telefone"));
            global = context.equals("global") ? List.of(fields.get("id"), fields.get("nome"), fields.get("nif"), fields.get("localidade"), fields.get("email"))
                    : List.of(fields.get("nome"), fields.get("nif"), fields.get("localidade"), fields.get("email"), fields.get("tel"), fields.get("tm"), fields.get("id"));
        }
        List<String> where = new ArrayList<>();
        String inactive = request.getFirst("inativo");
        if (inactive != null) {
            if (!Set.of("true", "false").contains(inactive)) throw bad("Estado inválido");
            params.put("inactive", Boolean.valueOf(inactive)); where.add("e.inativo = :inactive");
        }
        List<String> ids = request.get("ids");
        if (ids != null && !ids.isEmpty()) {
            if (ids.size() > 50) throw bad("Máximo de 50 identificadores por pedido");
            params.put("ids", ids); where.add("cast(" + fields.get(article ? "codigo" : "id") + " as text) in (:ids)");
        }
        String search = Optional.ofNullable(request.getFirst("search")).orElse("");
        if (context.startsWith("list") || context.equals("statement")) {
            // MultiSelectFilter uses literal contains over its complete searchText/label, without accent folding.
            String label = article ? "concat_ws(' ', nullif(e.codigo,''), nullif(e.descricao,''), nullif(e.unidade,''), nullif(cast(e.id_familia as text),'0'), nullif(e.id_iva_venda,''))"
                    : context.equals("list") ? "concat_ws(' ', cast(e.id as text), nullif(e.nome,''), nullif(e.nif,''), nullif(e.localidade,''), nullif(e.email,''), nullif(e.tel,''), nullif(e.tm,''))"
                    : context.equals("statement") ? "concat(e.id, ' - ', e.nome)"
                    : "concat(e.id, ' - ', e.nome, case when coalesce(e.nif,'') <> '' then concat(' - NIF ',e.nif) else '' end)";
            params.put("literal", "%" + escape(search.strip().toLowerCase(Locale.ROOT)) + "%");
            where.add("lower(" + label + ") like :literal escape '\\'");
        } else {
            for (LookupQuery.Term term : LookupQuery.parse(search, aliases.keySet(), context.equals("editor"))) {
                where.add(predicate(term.field() == null ? global : aliases.get(term.field()), term.value(), params));
            }
        }
        for (String key : request.keySet()) {
            if (key.startsWith("filter.")) {
                String field = fields.get(key.substring(7));
                if (field == null) throw bad("Campo de filtro inválido: " + key);
                where.add(predicate(List.of(field), request.getFirst(key), params));
            } else if (!Set.of("search", "page", "size", "sort", "inativo", "context", "ids").contains(key)) throw bad("Parâmetro inválido: " + key);
        }
        List<String> sorts = new ArrayList<>();
        for (String sort : request.getOrDefault("sort", List.of(article ? "descricao,asc" : "nome,asc"))) {
            String[] parts = sort.split(",", -1);
            String expression = fields.get(parts[0]);
            if (expression == null || parts.length > 2 || (parts.length == 2 && !Set.of("asc", "desc").contains(parts[1]))) throw bad("Ordenação inválida");
            boolean natural = context.equals("editor") && request.containsKey("sort");
            boolean numeric = article ? Set.of("familiaId", "pvp", "inativo", "retencao").contains(parts[0]) : Set.of("id", "inativo").contains(parts[0]);
            if (natural && !numeric) expression = "nullif(" + expression + ", '') collate lookup_pt_natural";
            sorts.add(expression + (parts.length == 2 && parts[1].equals("desc") ? " desc" : " asc") + (natural ? " nulls last" : " nulls first"));
        }
        sorts.add(fields.get(article ? "descricao" : "nome") + " asc");
        sorts.add(fields.get(article ? "codigo" : "id") + " asc");
        String from = " from " + (article ? "artigo" : "cliente") + " e";
        String conditions = where.isEmpty() ? "" : " where " + String.join(" and ", where);
        Long total = jdbc.queryForObject("select count(*)" + from + conditions, params, Long.class);
        params.put("limit", size); params.put("offset", (long) page * size);
        String select = fields.entrySet().stream().map(e -> e.getValue() + " as \"" + e.getKey() + "\"").collect(java.util.stream.Collectors.joining(", "));
        List<Map<String, Object>> rows = jdbc.queryForList("select " + select + from + conditions + " order by " + String.join(", ", sorts) + " limit :limit offset :offset", params);
        return new PageImpl<>(rows, PageRequest.of(page, size), total == null ? 0 : total);
    }

    private String ivaExpression(Map<String, Object> params) {
        // Small existing tax catalogue; never one query per article. Labels match ivaCompactLabel.
        StringBuilder sql = new StringBuilder("case e.id_iva_venda ");
        jdbc.query("select id, descricao from tipotaxaiva where inativo = false", Map.of(), rs -> {
            String id = rs.getString("id"), description = rs.getString("descricao");
            var matcher = IVA_NUMBER.matcher(description);
            String label = id.length() <= 3 ? id : id.substring(0, 3).toUpperCase(Locale.ROOT);
            if (matcher.find()) {
                NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-PT"));
                format.setMaximumFractionDigits(2); format.setRoundingMode(RoundingMode.HALF_UP);
                label = format.format(new BigDecimal(matcher.group(1).replace(',', '.'))) + "%";
            }
            String key = "iva" + params.size();
            params.put(key, id); params.put(key + "label", label);
            sql.append("when :").append(key).append(" then :").append(key).append("label ");
        });
        return sql.toString().equals("case e.id_iva_venda ") ? "e.id_iva_venda" : sql.append("else e.id_iva_venda end").toString();
    }
    private static String predicate(List<String> fields, String raw, Map<String, Object> params) {
        String text = raw == null ? "" : raw.strip();
        char op = !text.isEmpty() && "^*=!$".indexOf(text.charAt(0)) >= 0 ? text.charAt(0) : '*';
        String value = LookupQuery.normalize(!text.isEmpty() && "^*=!$".indexOf(text.charAt(0)) >= 0 ? text.substring(1) : text);
        if (value.isEmpty()) return "true";
        String key = "term" + params.size();
        String escaped = escape(value);
        params.put(key, op == '=' ? value : (op == '^' ? "" : "%") + escaped + (op == '$' ? "" : "%"));
        return "(" + fields.stream().map(field -> normalized(field) + (op == '=' ? " = :" + key : (op == '!' ? " not like :" : " like :") + key + " escape '\\'")).collect(java.util.stream.Collectors.joining(" or ")) + ")";
    }
    private static String normalized(String expression) {
        return "lower(btrim(regexp_replace(normalize(coalesce(cast(" + expression + " as text), ''), NFD), '[̀-ͯ]', '', 'g')))";
    }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_"); }
    private static int number(String value, int fallback, int maximum) {
        try { int n = value == null ? fallback : Integer.parseInt(value); if (n < 0 || n > maximum) throw bad("Paginação inválida"); return n; }
        catch (NumberFormatException ex) { throw bad("Paginação inválida"); }
    }
    private static ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private static Map<String, String> clientFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String name : List.of("id", "nome", "nif", "localidade", "tel", "tm", "email", "inativo")) fields.put(name, "e." + name);
        for (String[] pair : new String[][]{{"codPostalId","id_codpostal"},{"paisId","id_pais"},{"moedaId","id_moeda"},{"rivaId","id_riva"},{"mPagamentoId","id_mpagamento"},{"pPagamentoId","id_ppagamento"},{"transporteId","id_transporte"}}) fields.put(pair[0], "e." + pair[1]);
        return fields;
    }
    private static Map<String, String> articleFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String name : List.of("codigo", "descricao", "unidade", "inativo", "retencao", "observacoes")) fields.put(name, "e." + name);
        fields.put("pvp", "trim_scale(e.pvp)");
        fields.put("familiaId", "e.id_familia"); fields.put("ivaVendaId", "e.id_iva_venda"); fields.put("tipoArtigo", "e.tipo_artigo");
        return fields;
    }
}
