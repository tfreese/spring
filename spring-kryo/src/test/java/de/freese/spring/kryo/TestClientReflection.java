// Created: 30.01.2020
package de.freese.spring.kryo;

import java.time.LocalDateTime;

import de.freese.spring.kryo.reflection.ReflectionControllerApi;
import de.freese.spring.kryo.reflection.client.AbstractClientReflectionController.ConnectType;
import de.freese.spring.kryo.reflection.client.ClientReflectionController;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = KryoApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
class TestClientReflection
{
    @LocalServerPort
    private final int localServerPort = 0;

    @Test
    void testHttpConnection()
    {
        ReflectionControllerApi fassade =
                new ClientReflectionController(KryoApplication.KRYO_POOL, "http://localhost:" + this.localServerPort, ConnectType.HTTP_CONNECTION);

        LocalDateTime localDateTime = fassade.testKryo();

        TestKryo.validateLocalDateTime(localDateTime);
    }

    @Test
    void testRestTemplate()
    {
        ReflectionControllerApi fassade =
                new ClientReflectionController(KryoApplication.KRYO_POOL, "http://localhost:" + this.localServerPort, ConnectType.REST_TEMPLATE);

        LocalDateTime localDateTime = fassade.testKryo();

        TestKryo.validateLocalDateTime(localDateTime);
    }
}
