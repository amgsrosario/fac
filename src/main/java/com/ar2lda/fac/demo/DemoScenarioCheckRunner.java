package com.ar2lda.fac.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@ConditionalOnProperty(name = "fac.demo.check-on-startup", havingValue = "true")
@RequiredArgsConstructor
public class DemoScenarioCheckRunner implements ApplicationRunner {
    private final DemoScenarioCheckService service;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        var r = service.validate();
        System.out.printf("FAC_DEMO_CHECK OK | base=%s | perfil=demo%n", r.database());
        System.out.printf("  dados: utilizadores=%d clientes=%d artigos=%d series=%d%n", r.users(), r.clients(), r.articles(), r.series());
        System.out.printf("  circuito: comerciais=%d recebimentos=%d anulado=%d parcial=%d liquidado=%d%n",
                r.commercialDocuments(), r.financialDocuments(), r.annulled(), r.partial(), r.fullyPaid());
        System.out.printf("  evidencias: multipagina=%d pdfs=%d extrato=%d auditoria=%d referencias_quebradas=%d%n",
                r.multipage(), r.pdfs(), r.extractMovements(), r.audits(), r.brokenAuditReferences());
        if (context.getEnvironment().getProperty("fac.demo.exit-after-check", Boolean.class, false)) context.close();
    }
}
