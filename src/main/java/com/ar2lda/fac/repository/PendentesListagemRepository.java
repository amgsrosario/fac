package com.ar2lda.fac.repository;

import com.ar2lda.fac.controller.dto.PendenteListagemDto;
import com.ar2lda.fac.controller.dto.PendenteListagemTotaisDto;
import com.ar2lda.fac.controller.dto.PendentesPaginaDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
@RequiredArgsConstructor
public class PendentesListagemRepository {
    private final EntityManager entityManager;

    public PendentesPaginaDto consultar(LocalDate data, OffsetDateTime fimData, List<Long> clienteIds,
                                         boolean apenasVencidos, String pesquisa, int page, int size) {
        boolean historico = fimData != null;
        // Preserve the historical receipt rule: current non-cancelled receipts, issued through the reference date.
        String recebidos = historico ? """
                recebidos as (
                    select l.id_pendente, sum(l.valor_pagamento_bruto) as valor
                    from linha_documento_financeiro l
                    join documento_financeiro f on f.id = l.id_documento_financeiro
                    where f.anulado = false and f.data_emissao <= :data
                    group by l.id_pendente
                ),
                """ : "";
        String valorRecebido = historico ? "coalesce(r.valor, 0)" : "coalesce(p.valor_documento, 0) - coalesce(p.valor_pendente, 0)";
        String valorPendente = historico ? "coalesce(p.valor_documento, 0) - coalesce(r.valor, 0)" : "p.valor_pendente";
        String estado = historico ? """
                d.estado in ('EMITIDO', 'ANULADO')
                and (d.anulado = false or d.data_hora_anulacao is null or d.data_hora_anulacao > :fimData)
                """ : "d.estado = 'EMITIDO' and d.anulado = false";
        String ordem = (historico ? "vencimento, " : "") + "data, tipo, serie, numero, id";
        String sql = "with " + recebidos + """
                filtrados as (
                    select p.id, d.id as documento_id,
                           concat(p.id_tipo_documento, ' ', p.serie_documento, '/', p.numero_documento) as documento,
                           p.data_documento as data, p.data_vencimento as vencimento,
                           c.id as cliente_id, c.nif as cliente_codigo, c.nome as cliente_nome,
                           p.id_moeda as moeda_id, coalesce(p.valor_documento, 0) as total,
                           %s as recebido, %s as pendente,
                           p.id_tipo_documento as tipo, p.serie_documento as serie, p.numero_documento as numero
                    from pendente p
                    join documento_comercial d on d.id = p.id_documento_comercial
                    join cliente c on c.id = p.id_cliente
                    %s
                    where %s and d.numero_documento is not null
                      and p.data_documento <= :data
                      and %s > 0
                      %s
                      %s
                      %s
                ), resumo as (
                    select count(*) as elementos, coalesce(sum(total), 0) as total_global,
                           coalesce(sum(recebido), 0) as recebido_global,
                           coalesce(sum(pendente), 0) as pendente_global
                    from filtrados
                ), pagina as (
                    select * from filtrados order by %s limit :size offset :offset
                )
                select pagina.documento_id, pagina.documento, pagina.data, pagina.vencimento,
                       pagina.cliente_id, pagina.cliente_codigo, pagina.cliente_nome, pagina.moeda_id,
                       pagina.total, pagina.recebido, pagina.pendente,
                       resumo.elementos, resumo.total_global, resumo.recebido_global, resumo.pendente_global
                from resumo left join pagina on true
                order by %s
                """.formatted(valorRecebido, valorPendente,
                historico ? "left join recebidos r on r.id_pendente = p.id" : "", estado, valorPendente,
                apenasVencidos ? "and p.data_vencimento <= :data" : "",
                clienteIds.isEmpty() ? "" : "and c.id in (:clienteIds)",
                pesquisa == null || pesquisa.isBlank() ? "" : """
                    and position(:pesquisa in lower(concat(c.nome, ' ', c.nif, ' ',
                        p.id_tipo_documento, ' ', p.serie_documento, '/', p.numero_documento))) > 0
                    """, ordem, ordem);
        var query = entityManager.createNativeQuery(sql)
                .setParameter("data", data)
                .setParameter("size", size)
                .setParameter("offset", (long) page * size);
        if (historico) query.setParameter("fimData", fimData);
        if (!clienteIds.isEmpty()) query.setParameter("clienteIds", clienteIds);
        if (pesquisa != null && !pesquisa.isBlank()) query.setParameter("pesquisa", pesquisa.trim().toLowerCase(Locale.ROOT));
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        Object[] resumo = rows.getFirst();
        long totalElements = ((Number) resumo[11]).longValue();
        var totais = new PendenteListagemTotaisDto((BigDecimal) resumo[12], (BigDecimal) resumo[13], (BigDecimal) resumo[14]);
        var linhas = new ArrayList<PendenteListagemDto>();
        for (Object[] row : rows) {
            // The LEFT JOIN retains the global summary even for an empty or out-of-range page.
            if (row[0] == null) continue;
            linhas.add(new PendenteListagemDto(((Number) row[0]).longValue(), (String) row[1],
                    date(row[2]), date(row[3]), ((Number) row[4]).longValue(), (String) row[5],
                    (String) row[6], (String) row[7], (BigDecimal) row[8], (BigDecimal) row[9], (BigDecimal) row[10]));
        }
        return new PendentesPaginaDto(linhas, totais, totalElements, (totalElements + size - 1) / size);
    }

    private LocalDate date(Object value) {
        return value instanceof LocalDate date ? date : ((java.sql.Date) value).toLocalDate();
    }
}
