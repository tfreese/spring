// Created: 22.10.22
package de.freese.spring.messaging.kafka;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Disabled;
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
@Disabled("Failed to create embedded cluster")
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
    void testSend() {
        for (int i = 0; i < 5; i++) {
            await().pollDelay(Duration.ofMillis(500L)).until(() -> true);

            sender.send(topic, "Hello-" + (i + 1));
        }

        assertTrue(true);
    }
}
