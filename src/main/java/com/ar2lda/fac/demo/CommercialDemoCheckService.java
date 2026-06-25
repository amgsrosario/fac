package com.ar2lda.fac.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommercialDemoCheckService {
    private final DataSource dataSource;
    private final Environment environment;
    private final DemoScenarioCheckService demoScenarioCheckService;

    @Transactional(readOnly = true)
    public CommercialDemoReport validate() {
        DemoScenarioCheckService.DemoCheckReport demo = demoScenarioCheckService.validate();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        List<String> problems = new ArrayList<>();
        String database = jdbc.queryForObject("select current_database()", String.class);
        boolean demoProfile = Arrays.asList(environment.getActiveProfiles()).contains("demo");

        expect("fac_demo".equals(database) || "fac_restore_test".equals(database), problems,
                "base comercial deve ser fac_demo ou fac_restore_test");
        expect(!"fac".equalsIgnoreCase(database), problems, "base fac nao pode ser usada na demonstracao");
        expect(demoProfile, problems, "perfil demo deve estar ativo");

        long company = scalar(jdbc, """
                select count(*) from empresa
                where id=1
                  and nome='Alentejo Sabores, Lda.'
                  and nif='599000007'
                  and email is not null
                  and telefone is not null
                  and iban is not null
                  and logo is not null
                """);
        long expectedUsers = scalar(jdbc, """
                select count(*) from utilizador
                where not inativo and (codigo,papel) in (
                  ('admin.demo','ADMINISTRADOR'),
                  ('operador.demo','OPERADOR'),
                  ('consulta.demo','CONSULTA')
                )
                """);
        long importsAvailable = scalar(jdbc, "select count(*) from importacao_dados_mestres");
        long fiscalSnapshots = scalar(jdbc, """
                select count(*) from documento_comercial
                where serie='DEMO26'
                  and fiscal_snapshot_version=2
                  and emitente_nome is not null
                  and emitente_nif is not null
                  and emitente_logo is not null
                  and atcud is not null
                  and qr_payload is not null
                """);
        long auditDenied = scalar(jdbc, """
                select count(*) from auditoria_evento
                where tipo_evento in ('TENTATIVA_ANULACAO_NEGADA','TENTATIVA_ADMINISTRATIVA_NEGADA','TENTATIVA_IMPORTACAO_NEGADA')
                """);
        long auditBusiness = scalar(jdbc, """
                select count(*) from auditoria_evento
                where tipo_evento in ('DOCUMENTO_EMITIDO','DOCUMENTO_ANULADO','RECEBIMENTO_REGISTADO','EXPORTACAO_DADOS_MESTRES')
                """);
        long permissions = scalar(jdbc, """
                select count(*) from utilizador
                where (codigo='admin.demo' and papel='ADMINISTRADOR')
                   or (codigo='operador.demo' and papel='OPERADOR')
                   or (codigo='consulta.demo' and papel='CONSULTA')
                """);
        long noFacReferences = scalar(jdbc, """
                select count(*) from pg_database where datname='fac'
                """);

        expect(company == 1, problems, "empresa demo ou logotipo incompletos");
        expect(expectedUsers == 3 && permissions == 3, problems, "perfis demo incoerentes");
        expect(importsAvailable >= 0, problems, "tabela de importacoes indisponivel");
        expect(fiscalSnapshots == 6, problems, "snapshots fiscais v2, ATCUD, QR ou logo documental incompletos");
        expect(auditDenied >= 1, problems, "auditoria nao demonstra tentativas recusadas");
        expect(auditBusiness >= 6, problems, "auditoria comercial insuficiente");
        expect(noFacReferences == 0, problems, "base fac existe no servidor demo");

        if (!problems.isEmpty()) {
            throw new IllegalStateException("Demo comercial incompleta: " + String.join("; ", problems));
        }
        return new CommercialDemoReport(database, demo.users(), demo.clients(), demo.articles(),
                demo.commercialDocuments(), demo.financialDocuments(), demo.audits(), fiscalSnapshots);
    }

    private long scalar(JdbcTemplate jdbc, String sql) {
        Long value = jdbc.queryForObject(sql, Long.class);
        return value == null ? 0 : value;
    }

    private void expect(boolean condition, List<String> problems, String message) {
        if (!condition) problems.add(message);
    }

    public record CommercialDemoReport(String database, long users, long clients, long articles,
                                       long commercialDocuments, long financialDocuments,
                                       long audits, long fiscalSnapshots) {
    }
}
