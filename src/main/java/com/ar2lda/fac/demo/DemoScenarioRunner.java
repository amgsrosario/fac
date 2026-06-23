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
@ConditionalOnProperty(name="fac.demo.seed-on-startup", havingValue="true")
@RequiredArgsConstructor
public class DemoScenarioRunner implements ApplicationRunner {
    private final DemoScenarioService service;
    private final ConfigurableApplicationContext context;

    @Override public void run(ApplicationArguments args) {
        var result = service.seedAndValidate();
        System.out.println("FAC_DEMO_READY " + result);
        if (context.getEnvironment().getProperty("fac.demo.exit-after-seed", Boolean.class, false)) context.close();
    }
}
