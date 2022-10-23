// Created: 01.02.2019
package org.spring.messaging.amqp.qpid;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpSender.class);

    private final AmqpTemplate amqpTemplate;

    public AmqpSender(final AmqpTemplate amqpTemplate)
    {
        super();

        this.amqpTemplate = Objects.requireNonNull(amqpTemplate, "amqpTemplate required");
    }

    public void send(final Email email)
    {
        LOGGER.info("Sending message: {}", email);

        this.amqpTemplate.convertAndSend(SpringQpidApplication.TOPIC_EXCHANGE_NAME, "foo.bar.baz", email);
    }
}
