package com.ar2lda.fac.demo;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/** Reject a misdirected demo datasource before Flyway/JPA can write to it. */
@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="fac.demo.signature.enabled",havingValue="true")
public class DemoSignatureEarlyGuard {
    @Bean static BeanPostProcessor demoSignatureDatasourceGuard(Environment environment) {
        return new BeanPostProcessor() {
            @Override public Object postProcessAfterInitialization(Object bean,String name) {
                if(bean instanceof DataSource dataSource) {
                    var jdbc=new JdbcTemplate(dataSource);
                    DemoSignatureSource.validateIdentity(environment,jdbc);
                    if (Boolean.TRUE.equals(jdbc.queryForObject("select to_regclass('public.serie') is not null",Boolean.class)))
                        DemoSignatureSource.validateSeries(jdbc);
                    if (Boolean.TRUE.equals(jdbc.queryForObject("select to_regclass('public.documento_comercial') is not null and to_regclass('public.documento_financeiro') is not null",Boolean.class)))
                        DemoSignatureSource.validateExistingDocuments(jdbc);
                }
                return bean;
            }
        };
    }
}
