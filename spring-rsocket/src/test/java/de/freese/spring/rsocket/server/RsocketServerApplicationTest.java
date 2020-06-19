/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.server;

import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(classes =
{
        RsocketServerApplication.class
}, properties =
{
        "spring.shell.interactive.enabled=false"
})
@ActiveProfiles(
{
        "test", "server"
})
class RsocketServerApplicationTest
{
    /**
     *
     */
    @Test
    void contextLoads()
    {
        assertTrue(true);
    }
}
