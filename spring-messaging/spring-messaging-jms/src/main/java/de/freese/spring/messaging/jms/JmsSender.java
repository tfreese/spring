// Created: 01.02.2019
package de.freese.spring.messaging.jms;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author Thomas Freese
 */
public class JmsSender
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsSender.class);

    private final JmsTemplate jmsTemplate;

    public JmsSender(final JmsTemplate jmsTemplate)
    {
        super();

        this.jmsTemplate = Objects.requireNonNull(jmsTemplate, "jmsTemplate required");
    }

    public void send(final Email email)
    {
        LOGGER.info("Sending message: {}", email);

        this.jmsTemplate.convertAndSend("mailbox", email);
    }
}
