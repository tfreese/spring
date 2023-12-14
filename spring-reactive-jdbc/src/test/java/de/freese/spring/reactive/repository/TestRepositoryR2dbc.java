// Created: 23.06.2019
package de.freese.spring.reactive.repository;

import jakarta.annotation.Resource;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@ActiveProfiles({"test", "r2dbc"})
class TestRepositoryR2dbc implements TestRepository {
    @Resource
    private ConnectionFactory connectionFactory;

    @Resource
    private DatabaseClient databaseClient;

    @Resource
    private EmployeeRepository repository;

    @Override
    public void doAfterEach() {
        this.databaseClient.sql("DROP TABLE IF EXISTS employee").fetch().rowsUpdated().block();
        this.databaseClient.sql("DROP TABLE IF EXISTS department").fetch().rowsUpdated().block();
    }

    @Override
    public void doBeforeEach() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.populate(this.connectionFactory).block();

        // ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        // initializer.setConnectionFactory(this.connectionFactory);
        // initializer.setDatabasePopulator(populator);
        // initializer.afterPropertiesSet();
    }

    @Override
    public EmployeeRepository getRepository() {
        return this.repository;
    }

    @Override
    @Test
    public void testCreateNewEmployee() {
        // nur zum Debuggen
        TestRepository.super.testCreateNewEmployee();
    }

    @Override
    @Test
    public void testGetEmployee() {
        // nur zum Debuggen
        TestRepository.super.testGetEmployee();
    }
}
