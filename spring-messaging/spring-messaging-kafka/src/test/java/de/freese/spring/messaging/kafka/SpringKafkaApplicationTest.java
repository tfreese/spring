// Created: 22.10.22
package de.freese.spring.messaging.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class SpringKafkaApplicationTest {
    //    @ClassRule
    //    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, TOPIC_NAME);

    @Resource
    private KafkaReceiver receiver;

    @Resource
    private KafkaSender sender;

    @Value("${test.topic}")
    private String topic;

    @Test
    void testSend() throws Exception {
        for (int i = 0; i < 5; i++) {
            TimeUnit.MILLISECONDS.sleep(500);

            sender.send(topic, "Hello-" + (i + 1));
        }

        assertTrue(true);
    }
}
