// Created: 11.03.2020
package de.freese.spring.rsocket.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "spring.rsocket.server.port=0", webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RsocketServerApplicationTest
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
