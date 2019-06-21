package de.freese.spring.reactive;
/**
 * Created: 21.06.2019
 */

import javax.annotation.Resource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Thomas Freese
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("reactive-jdbc")
public class TestReactiveJdbc implements TestInterface
{
    /**
    *
    *
    */
    @Resource
    private WebTestClient webTestClient = null;

    /**
     * @see de.freese.spring.reactive.TestInterface#getWebClient()
     */
    @Override
    public WebTestClient getWebClient()
    {
        return this.webTestClient;
    }
}
