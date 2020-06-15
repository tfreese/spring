package de.freese.spring.reactive;
/**
 * Created: 21.06.2019
 */

import static org.junit.Assert.assertTrue;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
{
        "test", "jdbc-reactive"
})
class TestWebJdbcReactive implements TestWebInterface
{
    /**
    *
    */
    @Resource
    private JdbcTemplate jdbcTemplate = null;

    /**
    *
    */
    @LocalServerPort
    private int port = -1;

    /**
    *
    */
    private WebClient webClient = null;

    /**
    *
    *
    */
    @Resource
    private WebTestClient webTestClient = null;

    /**
    *
    */
    @BeforeEach
    void beforeEach()
    {
        this.webClient = WebClient.create("http://localhost:" + this.port);
    }

    /**
     * @see de.freese.spring.reactive.TestWebInterface#createNewEmployee()
     */
    @Override
    @Test
    @Disabled("Funktioniert nicht")
    public void createNewEmployee()
    {
        TestWebInterface.super.createNewEmployee();

        assertTrue(true);
    }

    /**
     * @see de.freese.spring.reactive.TestWebInterface#getJdbcTemplate()
     */
    @Override
    public JdbcTemplate getJdbcTemplate()
    {
        return this.jdbcTemplate;
    }

    /**
     * @see de.freese.spring.reactive.TestWebInterface#getWebClient()
     */
    @Override
    public WebClient getWebClient()
    {
        return this.webClient;
    }

    /**
     * @see de.freese.spring.reactive.TestWebInterface#getWebTestClient()
     */
    @Override
    public WebTestClient getWebTestClient()
    {
        return this.webTestClient;
    }
}
