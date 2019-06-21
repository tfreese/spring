/**
 * Created: 21.06.2019
 */

package de.freese.spring.reactive;

import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Thomas Freese
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("jdbc")
public class TestJdbc implements TestInterface
{
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
     * @see de.freese.spring.reactive.TestInterface#getWebClient()
     */
    @Override
    public WebClient getWebClient()
    {
        return this.webClient;
    }

    /**
     * @see de.freese.spring.reactive.TestInterface#getWebTestClient()
     */
    @Override
    public WebTestClient getWebTestClient()
    {
        return this.webTestClient;
    }
}
