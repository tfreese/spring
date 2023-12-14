// Created: 30.01.2020
package de.freese.spring.kryo;

import java.time.LocalDateTime;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
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
class TestClientReflection {
    @LocalServerPort
    private int localServerPort;

    @Test
    void testHttpConnection() {
        final String rootUri = "http://localhost:" + this.localServerPort;
        final ReflectionControllerApi fassade = new ClientReflectionController(KryoApplication.KRYO_POOL, rootUri, ConnectType.HTTP_CONNECTION);

        final LocalDateTime localDateTime = fassade.testKryo();

        TestKryo.validateLocalDateTime(localDateTime);
    }

    @Test
    void testRestTemplate() {
        final String rootUri = "http://localhost:" + this.localServerPort;
        final ReflectionControllerApi fassade = new ClientReflectionController(KryoApplication.KRYO_POOL, rootUri, ConnectType.REST_TEMPLATE);

        final LocalDateTime localDateTime = fassade.testKryo();

        TestKryo.validateLocalDateTime(localDateTime);
    }
}
