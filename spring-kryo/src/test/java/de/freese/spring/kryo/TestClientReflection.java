/**
 * Created: 30.01.2020
 */

package de.freese.spring.kryo;

import java.time.LocalDateTime;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import de.freese.spring.kryo.reflection.ReflectionControllerApi;
import de.freese.spring.kryo.reflection.client.ClientReflectionController;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = KryoApplication.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ActiveProfiles("test")
public class TestClientReflection
{
    /**
    *
    */
    @LocalServerPort
    private int localServerPort = 0;

    /**
     * Erstellt ein neues {@link TestClientReflection} Object.
     */
    public TestClientReflection()
    {
        super();
    }

    /**
     *
     */
    @Test
    public void testHttpConnection()
    {
        ReflectionControllerApi fassade = new ClientReflectionController("http://localhost:" + this.localServerPort);

        LocalDateTime localDateTime = fassade.testKryo();

        TestKryo.validateLocalDateTime(localDateTime);
    }
}
