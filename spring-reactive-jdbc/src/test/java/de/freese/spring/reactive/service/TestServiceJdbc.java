/**
 * Created: 23.06.2019
 */

package de.freese.spring.reactive.service;

import javax.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import de.freese.spring.reactive.EmployeeService;

/**
 * @author Thomas Freese
 */
@SpringBootTest
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
    private JdbcTemplate jdbcTemplate = null;

    /**
    *
    */
    @Resource
    private EmployeeService service = null;

    /**
     * Erstellt ein neues {@link TestServiceJdbc} Object.
     */
    public TestServiceJdbc()
    {
        super();
    }

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
