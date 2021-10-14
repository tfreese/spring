// Created: 23.06.2019
package de.freese.spring.reactive.repository;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@ActiveProfiles(
{
        "test", "jdbc"
})
class TestRepositoryJdbc implements TestRepository
{
    /**
    *
    */
    @Resource
    private JdbcTemplate jdbcTemplate;
    /**
    *
    */
    @Resource
    private EmployeeRepository repository;

    /**
     * @see de.freese.spring.reactive.repository.TestRepository#doAfterEach()
     */
    @Override
    public void doAfterEach()
    {
        this.jdbcTemplate.execute("DROP TABLE IF EXISTS employee");
        this.jdbcTemplate.execute("DROP TABLE IF EXISTS department");
    }

    /**
     * @see de.freese.spring.reactive.repository.TestRepository#doBeforeEach()
     */
    @Override
    public void doBeforeEach()
    {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.execute(this.jdbcTemplate.getDataSource());
    }

    // /**
    // * @see de.freese.spring.reactive.repository.TestRepository#getJdbcTemplate()
    // */
    // @Override
    // public JdbcTemplate getJdbcTemplate()
    // {
    // return this.jdbcTemplate;
    // }

    /**
     * @see de.freese.spring.reactive.repository.TestRepository#getRepository()
     */
    @Override
    public EmployeeRepository getRepository()
    {
        return this.repository;
    }

    /**
     * @see de.freese.spring.reactive.repository.TestRepository#testGetEmployee()
     */
    @Override
    @Test
    public void testGetEmployee()
    {
        // nur zum Debuggen
        TestRepository.super.testGetEmployee();
    }
}
