// Created: 22.10.22
package de.freese.spring.messaging.jms;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
//@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class SpringJmsApplicationTest {
    //    private static EmbeddedActiveMQ embeddedActiveMQ;

    //    static {
    //        -Djava.util.logging.manager=org.jboss.logmanager.LogManager
    //        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    //    }

    //    @AfterAll
    //    static void afterAll() throws Exception {
    //        embeddedActiveMQ.stop();
    //    }

    //    @BeforeAll
    //    static void beforeAll() throws Exception {
    //        embeddedActiveMQ = new EmbeddedActiveMQ();
    //        embeddedActiveMQ.start();
    //    }

    // @Resource
    // private JmsReceiver receiver;

    @Resource
    private JmsSender sender;

    @Test
    void testReceive() {
        for (int i = 0; i < 5; i++) {
            await().pollDelay(Duration.ofMillis(500L)).until(() -> true);

            sender.send(new Email("info@example.com", "Hello-" + (i + 1)));
        }

        await().pollDelay(Duration.ofMillis(500L)).until(() -> true);

        assertTrue(true);
    }
}
