package com.ar2lda.fac.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/** Isolated synthetic XSD fixture. It is not a fiscal signing provider or certificate. */
@Component
@ConditionalOnProperty(name="fac.demo.saft.short-signature.enabled",havingValue="true")
@ConditionalOnExpression("'${fac.demo.signature.enabled:false}' == 'true'")
public final class DemoSaftSignatureSource extends DemoSignatureSource {
    public DemoSaftSignatureSource(Environment environment, DataSource dataSource, ObjectMapper mapper) {
        super(environment,dataSource,mapper);
    }
    @Override protected boolean acceptsKeyBits(int bits) { return bits==1024; }
}
