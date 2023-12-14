// Created: 23.06.2019
package de.freese.spring.reactive.repository;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@ActiveProfiles({"test", "jdbc"})
class TestRepositoryJdbc implements TestRepository {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private EmployeeRepository repository;

    @Override
    public void doAfterEach() {
        this.jdbcTemplate.execute("DROP TABLE IF EXISTS employee");
        this.jdbcTemplate.execute("DROP TABLE IF EXISTS department");
    }

    @Override
    public void doBeforeEach() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.execute(this.jdbcTemplate.getDataSource());
    }

    // @Override
    // public JdbcTemplate getJdbcTemplate()
    // {
    // return this.jdbcTemplate;
    // }

    @Override
    public EmployeeRepository getRepository() {
        return this.repository;
    }

    @Override
    @Test
    public void testGetEmployee() {
        // nur zum Debuggen
        TestRepository.super.testGetEmployee();
    }
}
