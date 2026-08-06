package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DashboardComercialDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardComercialService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(6);
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    @Transactional(readOnly = true)
    public DashboardComercialDto consultar(LocalDate dataInicio, LocalDate dataFim) {
        String moedaId = moedaPrincipal();
        MapSqlParameterSource periodo = new MapSqlParameterSource()
                .addValue("dataInicio", dataInicio)
                .addValue("dataFim", dataFim)
                .addValue("moedaId", moedaId);
        LocalDate hoje = LocalDate.now(clock);

        BigDecimal vendas = valor("""
                select coalesce(sum(case when t.sinal_contabilistico = 2 then -d.valor_total else d.valor_total end), 0)
                from documento_comercial d
                join tipodocumento t on t.id = d.id_tipo_documento
                where d.estado = 'EMITIDO' and d.anulado = false
                  and d.data_emissao between :dataInicio and :dataFim
                  and d.id_moeda = :moedaId
                """, periodo);
        BigDecimal recebimentos = valor("""
                select coalesce(sum(d.valor_pagamento_liquido), 0)
                from documento_financeiro d
                where d.anulado = false and d.data_emissao between :dataInicio and :dataFim
                  and d.id_moeda = :moedaId
                """, periodo);
        BigDecimal valorEmAberto = valor("""
                select coalesce(sum(p.valor_pendente), 0)
                from pendente p
                join documento_comercial d on d.id = p.id_documento_comercial
                where d.estado = 'EMITIDO' and d.anulado = false and p.valor_pendente > 0
                  and p.id_moeda = :moedaId
                """, periodo);

        MapSqlParameterSource atual = new MapSqlParameterSource()
                .addValue("hoje", hoje)
                .addValue("moedaId", moedaId);
        DashboardComercialDto.DocumentosVencidos vencidos = jdbc.queryForObject("""
                select count(*) as quantidade, coalesce(sum(p.valor_pendente), 0) as valor
                from pendente p
                join documento_comercial d on d.id = p.id_documento_comercial
                where d.estado = 'EMITIDO' and d.anulado = false
                  and p.valor_pendente > 0 and p.data_vencimento < :hoje
                  and p.id_moeda = :moedaId
                """, atual, (rs, row) -> new DashboardComercialDto.DocumentosVencidos(
                rs.getLong("quantidade"), rs.getBigDecimal("valor")));

        return new DashboardComercialDto(
                new DashboardComercialDto.Periodo(dataInicio, dataFim),
                moedaId, vendas, recebimentos, valorEmAberto, vencidos,
                evolucao(dataInicio, dataFim, periodo), clientesComMaiorSaldo(moedaId)
        );
    }

    private List<DashboardComercialDto.Evolucao> evolucao(
            LocalDate dataInicio, LocalDate dataFim, MapSqlParameterSource params) {
        boolean mensal = ChronoUnit.DAYS.between(dataInicio, dataFim) > 45;
        Map<String, BigDecimal[]> pontos = new LinkedHashMap<>();
        if (mensal) {
            for (YearMonth atual = YearMonth.from(dataInicio), fim = YearMonth.from(dataFim);
                 !atual.isAfter(fim); atual = atual.plusMonths(1)) {
                pontos.put(atual.toString(), new BigDecimal[]{ZERO, ZERO});
            }
        } else {
            for (LocalDate atual = dataInicio; !atual.isAfter(dataFim); atual = atual.plusDays(1)) {
                pontos.put(atual.toString(), new BigDecimal[]{ZERO, ZERO});
            }
        }

        String bucket = mensal ? "to_char(data_emissao, 'YYYY-MM')" : "to_char(data_emissao, 'YYYY-MM-DD')";
        jdbc.query("""
                select %s as periodo,
                       coalesce(sum(case when t.sinal_contabilistico = 2 then -d.valor_total else d.valor_total end), 0) as valor
                from documento_comercial d
                join tipodocumento t on t.id = d.id_tipo_documento
                where d.estado = 'EMITIDO' and d.anulado = false
                  and d.data_emissao between :dataInicio and :dataFim
                  and d.id_moeda = :moedaId
                group by 1 order by 1
                """.formatted(bucket), params,
                (rs, row) -> Map.entry(rs.getString("periodo"), rs.getBigDecimal("valor")))
                .forEach(entry -> pontos.get(entry.getKey())[0] = entry.getValue());
        jdbc.query("""
                select %s as periodo, coalesce(sum(d.valor_pagamento_liquido), 0) as valor
                from documento_financeiro d
                where d.anulado = false and d.data_emissao between :dataInicio and :dataFim
                  and d.id_moeda = :moedaId
                group by 1 order by 1
                """.formatted(bucket), params,
                (rs, row) -> Map.entry(rs.getString("periodo"), rs.getBigDecimal("valor")))
                .forEach(entry -> pontos.get(entry.getKey())[1] = entry.getValue());

        return pontos.entrySet().stream()
                .map(entry -> new DashboardComercialDto.Evolucao(
                        entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .toList();
    }

    private List<DashboardComercialDto.ClienteSaldo> clientesComMaiorSaldo(String moedaId) {
        return jdbc.query("""
                select c.id, c.nome, sum(p.valor_pendente) as saldo,
                       count(*) as documentos, min(p.data_vencimento) as vencimento_mais_antigo
                from pendente p
                join documento_comercial d on d.id = p.id_documento_comercial
                join cliente c on c.id = p.id_cliente
                where d.estado = 'EMITIDO' and d.anulado = false and p.valor_pendente > 0
                  and p.id_moeda = :moedaId
                group by c.id, c.nome
                order by saldo desc, c.id asc
                limit 5
                """, new MapSqlParameterSource("moedaId", moedaId),
                (rs, row) -> new DashboardComercialDto.ClienteSaldo(
                rs.getLong("id"), rs.getString("nome"), rs.getBigDecimal("saldo"),
                rs.getLong("documentos"), rs.getObject("vencimento_mais_antigo", LocalDate.class)));
    }

    private String moedaPrincipal() {
        List<String> moedas = jdbc.query("select id from moeda order by id", (rs, row) -> rs.getString(1));
        return moedas.contains("EUR") ? "EUR" : moedas.stream().findFirst().orElse("EUR");
    }

    private BigDecimal valor(String sql, MapSqlParameterSource params) {
        BigDecimal value = jdbc.queryForObject(sql, params, BigDecimal.class);
        return value == null ? ZERO : value;
    }
}
