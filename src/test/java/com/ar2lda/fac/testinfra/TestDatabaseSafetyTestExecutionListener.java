package com.ar2lda.fac.testinfra;

import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseSafetyTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        ApplicationContext context = testContext.getApplicationContext();
        String[] dataSourceNames = context.getBeanNamesForType(DataSource.class);
        if (dataSourceNames.length == 0) {
            return;
        }

        Environment environment = context.getEnvironment();
        boolean destructiveOperationsEnabled = environment.getProperty(
                "fac.test-database.destructive-operations-enabled",
                Boolean.class,
                false
        );
        DataSource dataSource = context.getBean(dataSourceNames[0], DataSource.class);
        String databaseName = currentDatabaseName(dataSource);

        TestDatabaseSafetyValidator.validate(
                environment.getActiveProfiles(),
                databaseName,
                destructiveOperationsEnabled
        );
    }

    private String currentDatabaseName(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getCatalog();
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível confirmar a base PostgreSQL usada pelos testes", exception);
        }
    }
}

