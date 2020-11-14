/**
 * Created: 30.01.2020
 */

package de.freese.spring.kryo;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import de.freese.spring.kryo.reflection.ReflectionControllerApi;
import de.freese.spring.kryo.reflection.client.AbstractClientReflectionController.ConnectType;
import de.freese.spring.kryo.reflection.client.ClientReflectionController;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = KryoApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
@Disabled
class TestClientReflection
{
    /**
    *
    */
    @LocalServerPort
    private int localServerPort = 0;

    /**
     *
     */
    @Test
    void testHttpConnection()
    {
        ReflectionControllerApi fassade = new ClientReflectionController("http://localhost:" + this.localServerPort, ConnectType.HTTP_CONNECTION);

        LocalDateTime localDateTime = fassade.testKryo();

        TestKryo.validateLocalDateTime(localDateTime);
    }

    /**
    *
    */
    @Test
    void testRestTemplate()
    {
        ReflectionControllerApi fassade = new ClientReflectionController("http://localhost:" + this.localServerPort, ConnectType.REST_TEMPLATE);

        LocalDateTime localDateTime = fassade.testKryo();

        TestKryo.validateLocalDateTime(localDateTime);
    }
}
