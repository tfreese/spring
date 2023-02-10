// Created: 19.12.22
package de.freese.spring.messaging.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class KafkaSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSender.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaSender(final KafkaTemplate<String, String> kafkaTemplate) {
        super();

        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String payload) {
        LOGGER.info("sending payload='{}' to topic='{}'", payload, topic);

        kafkaTemplate.send(topic, payload);
    }
}
