/**
 * Created: 01.02.2019
 */

package de.freese.spring.messaging.jms;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class JmsSender
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsSender.class);

    /**
     *
     */
    private final JmsTemplate jmsTemplate;

    /**
     * Erstellt ein neues {@link JmsSender} Object.
     *
     * @param jmsTemplate {@link JmsTemplate}
     */
    public JmsSender(final JmsTemplate jmsTemplate)
    {
        super();

        this.jmsTemplate = Objects.requireNonNull(jmsTemplate, "jmsTemplate required");
    }

    /**
     * @param email {@link Email}
     */
    public void send(final Email email)
    {
        LOGGER.info("Sending message: {}", email);

        this.jmsTemplate.convertAndSend("mailbox", email);
    }
}
