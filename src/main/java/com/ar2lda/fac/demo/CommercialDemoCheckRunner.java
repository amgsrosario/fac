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
@ConditionalOnProperty(name = "fac.demo.commercial-check-on-startup", havingValue = "true")
@RequiredArgsConstructor
public class CommercialDemoCheckRunner implements ApplicationRunner {
    private final CommercialDemoCheckService service;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        var report = service.validate();
        System.out.printf("FAC_COMMERCIAL_DEMO_CHECK OK | base=%s | perfil=demo%n", report.database());
        System.out.printf("  narrativa: utilizadores=%d clientes=%d artigos=%d%n",
                report.users(), report.clients(), report.articles());
        System.out.printf("  evidencias: comerciais=%d recebimentos=%d snapshots_v2=%d auditoria=%d%n",
                report.commercialDocuments(), report.financialDocuments(), report.fiscalSnapshots(), report.audits());
        if (context.getEnvironment().getProperty("fac.demo.exit-after-commercial-check", Boolean.class, false)) {
            context.close();
        }
    }
}
