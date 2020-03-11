/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import de.freese.spring.rsocket.server.RsocketServerApplication;

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
        "test", "client"
})
public class RsocketClientApplicationTests
{
    /**
     * Erstellt ein neues {@link RsocketClientApplicationTests} Object.
     */
    public RsocketClientApplicationTests()
    {
        super();
    }

    /**
     *
     */
    @Test
    void contextLoads()
    {
    }
}
