package com.ar2lda.fac.fiscal.catalog;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FiscalCatalogInitializationTests {
    record LegacyConsumer(FiscalCatalog catalog) {
        String existingOperation() { return "legacy unchanged"; }
    }

    @ParameterizedTest
    @ValueSource(strings={"MISSING", "", "{broken", "{}", "null"})
    void badInactiveManifestDoesNotPreventLegacyStartupButEveryCatalogEntryPointFailsClosed(String content) {
        JdbcTemplate db=mock(JdbcTemplate.class);
        Resource source=content.equals("MISSING")
                ? new ClassPathResource("fiscal/missing-initialization-fixture.json")
                : new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
        try(var context=new AnnotationConfigApplicationContext()) {
            context.registerBean(FiscalCatalog.class,()->new FiscalCatalog(db,source));
            context.registerBean(LegacyConsumer.class,()->new LegacyConsumer(context.getBean(FiscalCatalog.class)));
            assertThatCode(context::refresh).doesNotThrowAnyException();
            assertThat(context.getBean(LegacyConsumer.class).existingOperation()).isEqualTo("legacy unchanged");
            var catalog=context.getBean(FiscalCatalog.class);
            assertThatThrownBy(catalog::verifyCoherence).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(()->catalog.revision("FT",1)).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(()->catalog.forType("FT")).isInstanceOf(IllegalStateException.class);
            verifyNoInteractions(db);
        }
    }
}
