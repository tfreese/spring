// Created: 22.10.22
package de.freese.spring.messaging.jms;

import java.util.concurrent.TimeUnit;

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
class SpringJmsApplicationTest
{
    //    private static EmbeddedActiveMQ embeddedActiveMQ;

    static
    {
        //        -Djava.util.logging.manager=org.jboss.logmanager.LogManager
        //        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }

    //    @AfterAll
    //    static void afterAll() throws Exception
    //    {
    //        embeddedActiveMQ.stop();
    //    }

    //    @BeforeAll
    //    static void beforeAll() throws Exception
    //    {
    //        embeddedActiveMQ = new EmbeddedActiveMQ();
    //        embeddedActiveMQ.start();
    //    }

    @Resource
    private JmsReceiver receiver;

    @Resource
    private JmsSender sender;

    @Test
    void testReceive() throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            TimeUnit.MILLISECONDS.sleep(500);

            sender.send(new Email("info@example.com", "Hello-" + (i + 1)));
        }

        TimeUnit.MILLISECONDS.sleep(500);
    }
}
