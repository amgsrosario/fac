package com.ar2lda.fac.demo;

import com.ar2lda.fac.model.EstadoDocumentoComercial;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.service.DocumentoComercialPdfService;
import com.ar2lda.fac.service.ExtratoClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoScenarioCheckService {
    private final DataSource dataSource;
    private final Environment environment;
    private final DocumentoComercialRepository documentoRepository;
    private final DocumentoComercialPdfService pdfService;
    private final ExtratoClienteService extratoService;

    @Value("${fac.demo.expected-database:fac_demo}")
    private String expectedDatabase;

    @Transactional(readOnly = true)
    public DemoCheckReport inspect() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        List<String> problems = new ArrayList<>();
        String database = jdbc.queryForObject("select current_database()", String.class);
        boolean demoProfile = Arrays.asList(environment.getActiveProfiles()).contains("demo");
        expect(expectedDatabase.equals(database), problems, "base ativa deve ser exatamente fac_demo");
        expect(demoProfile, problems, "perfil Spring demo nao esta ativo");
        if (!expectedDatabase.equals(database) || !demoProfile) {
            return DemoCheckReport.empty(database, demoProfile, problems);
        }

        long company = scalar(jdbc, "select count(*) from empresa where id=1 and nome='Alentejo Sabores, Lda.'");
        long users = scalar(jdbc, "select count(*) from utilizador where (codigo,papel) in (('admin.demo','ADMINISTRADOR'),('operador.demo','OPERADOR'),('consulta.demo','CONSULTA'))");
        long clients = scalar(jdbc, "select count(*) from cliente");
        long articles = scalar(jdbc, "select count(*) from artigo");
        long series = scalar(jdbc, "select count(*) from serie where serie='DEMO26' and ((id_tipo_documento='FT' and numerador=6) or (id_tipo_documento='RC' and numerador=2))");
        long commercial = scalar(jdbc, "select count(*) from documento_comercial where serie='DEMO26'");
        long fiscal = scalar(jdbc, "select count(*) from documento_comercial where serie='DEMO26' and estado in ('EMITIDO','ANULADO') and numero_documento between 1 and 6 and numero_documento_completo is not null and atcud is not null and qr_payload is not null and fiscal_snapshot_version=1");
        long financial = scalar(jdbc, "select count(*) from documento_financeiro where serie='DEMO26' and numero_documento between 1 and 2 and atcud is not null and qr_payload is not null");
        long annulled = scalar(jdbc, "select count(*) from documento_comercial where serie='DEMO26' and estado='ANULADO' and motivo_anulacao is not null and data_hora_anulacao is not null");
        long partial = scalar(jdbc, "select count(*) from pendente p join documento_comercial d on d.id=p.id_documento_comercial where d.serie='DEMO26' and d.estado='EMITIDO' and p.valor_pendente>0 and p.valor_pendente<p.valor_documento");
        long paid = scalar(jdbc, "select count(*) from pendente p join documento_comercial d on d.id=p.id_documento_comercial where d.serie='DEMO26' and d.estado='EMITIDO' and p.valor_pendente=0");
        long multipage = scalar(jdbc, "select count(*) from (select d.id from documento_comercial d join linha_documento_comercial l on l.id_documento_comercial=d.id where d.serie='DEMO26' group by d.id having count(l.id)=35) x");
        long audit = scalar(jdbc, "select count(*) from auditoria_evento where tipo_evento in ('DOCUMENTO_EMITIDO','DOCUMENTO_ANULADO','RECEBIMENTO_REGISTADO','LOGIN_SUCESSO','TENTATIVA_ANULACAO_NEGADA')");
        long auditKinds = scalar(jdbc, "select count(distinct tipo_evento) from auditoria_evento where tipo_evento in ('DOCUMENTO_EMITIDO','DOCUMENTO_ANULADO','RECEBIMENTO_REGISTADO','LOGIN_SUCESSO','TENTATIVA_ANULACAO_NEGADA')");
        long brokenAuditReferences = scalar(jdbc, """
                select count(*) from auditoria_evento a
                where (a.entidade_tipo='DOCUMENTO_COMERCIAL' and not exists (select 1 from documento_comercial d where d.id::text=a.entidade_id))
                   or (a.entidade_tipo='DOCUMENTO_FINANCEIRO' and not exists (select 1 from documento_financeiro d where d.id::text=a.entidade_id))
                   or (a.entidade_tipo='UTILIZADOR' and not exists (select 1 from utilizador u where u.codigo=a.entidade_id))
                """);

        long pdfs = documentoRepository.findAll().stream()
                .filter(d -> d.getEstado() != EstadoDocumentoComercial.RASCUNHO)
                .map(d -> pdfService.gerarParaValidacao(d.getId()))
                .filter(pdf -> pdf.content().length > 1000)
                .count();
        long extractMovements = extratoService.getExtrato(1104L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
                .moedas().stream().mapToLong(m -> m.movimentos().size()).sum();

        expect(company == 1, problems, "empresa demonstrativa em falta");
        expect(users == 3 && scalar(jdbc, "select count(*) from utilizador") == 3, problems, "utilizadores/perfis demo devem ser exatamente 3");
        expect(clients == 5, problems, "clientes demo devem ser exatamente 5");
        expect(articles == 8, problems, "artigos/servicos demo devem ser exatamente 8");
        expect(series == 2, problems, "series DEMO26 ou numeradores incoerentes");
        expect(commercial == 6 && fiscal == 6, problems, "documentos comerciais devem ser 6, numerados e fiscalmente consolidados");
        expect(financial == 2, problems, "recebimentos devem ser 2, numerados e fiscalmente consolidados");
        expect(annulled == 1, problems, "deve existir exatamente um documento anulado");
        expect(partial == 1, problems, "deve existir exatamente um documento parcialmente pago");
        expect(paid == 1, problems, "deve existir exatamente um documento totalmente pago ativo");
        expect(multipage == 1, problems, "deve existir exatamente um documento com 35 linhas");
        expect(pdfs == 6, problems, "os seis PDFs devem ser gerados sem erro");
        expect(extractMovements == 3, problems, "o extrato demonstrativo deve ter exatamente 3 movimentos");
        expect(auditKinds == 5 && brokenAuditReferences == 0, problems, "auditoria incompleta ou com referencias quebradas");

        return new DemoCheckReport(database, demoProfile, users, clients, articles, series, commercial,
                financial, annulled, partial, paid, multipage, pdfs, extractMovements, audit,
                brokenAuditReferences, List.copyOf(problems));
    }

    public DemoCheckReport validate() {
        DemoCheckReport report = inspect();
        if (!report.complete()) throw new IllegalStateException("Cenario demo incompleto: " + String.join("; ", report.problems()));
        return report;
    }

    private long scalar(JdbcTemplate jdbc, String sql) {
        Long value = jdbc.queryForObject(sql, Long.class);
        return value == null ? 0 : value;
    }

    private void expect(boolean condition, List<String> problems, String message) {
        if (!condition) problems.add(message);
    }

    public record DemoCheckReport(String database, boolean demoProfile, long users, long clients, long articles,
            long series, long commercialDocuments, long financialDocuments, long annulled, long partial,
            long fullyPaid, long multipage, long pdfs, long extractMovements, long audits,
            long brokenAuditReferences, List<String> problems) {
        static DemoCheckReport empty(String database, boolean demoProfile, List<String> problems) {
            return new DemoCheckReport(database, demoProfile, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, List.copyOf(problems));
        }
        public boolean complete() { return problems.isEmpty(); }
    }
}
