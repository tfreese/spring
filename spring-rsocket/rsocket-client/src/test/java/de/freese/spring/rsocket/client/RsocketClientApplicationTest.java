/**
 * Created: 11.03.2020
 */
package de.freese.spring.rsocket.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties =
{
        "spring.shell.interactive.enabled=false"
})
@ActiveProfiles("test")
class RsocketClientApplicationTest
{
    /**
     *
     */
    @Test
    void testContextLoads()
    {
        assertTrue(true);
    }
}
