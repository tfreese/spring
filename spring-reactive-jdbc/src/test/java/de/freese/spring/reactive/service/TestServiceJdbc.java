// Created: 23.06.2019
package de.freese.spring.reactive.service;

import javax.annotation.Resource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import de.freese.spring.reactive.EmployeeService;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
{
        "test", "jdbc"
})
class TestServiceJdbc implements TestServiceInterface
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
    private EmployeeService service;

    /**
     * @see de.freese.spring.reactive.service.TestServiceInterface#getJdbcTemplate()
     */
    @Override
    public JdbcTemplate getJdbcTemplate()
    {
        return this.jdbcTemplate;
    }

    /**
     * @see de.freese.spring.reactive.service.TestServiceInterface#getService()
     */
    @Override
    public EmployeeService getService()
    {
        return this.service;
    }
}
