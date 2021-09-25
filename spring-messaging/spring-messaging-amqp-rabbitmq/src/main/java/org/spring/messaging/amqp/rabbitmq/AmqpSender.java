// Created: 01.02.2019
package org.spring.messaging.amqp.rabbitmq;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class AmqpSender
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpSender.class);
    /**
     *
     */
    private final AmqpTemplate amqpTemplate;

    /**
     * Erstellt ein neues {@link AmqpSender} Object.
     *
     * @param amqpTemplate {@link AmqpTemplate}
     */
    public AmqpSender(final AmqpTemplate amqpTemplate)
    {
        super();

        this.amqpTemplate = Objects.requireNonNull(amqpTemplate, "amqpTemplate required");
    }

    /**
     * @param email {@link Email}
     */
    public void send(final Email email)
    {
        LOGGER.info("Sending message: {}", email);

        this.amqpTemplate.convertAndSend(SpringRabbitMqApplication.TOPIC_EXCHANGE_NAME, "foo.bar.baz", email);
    }
}
