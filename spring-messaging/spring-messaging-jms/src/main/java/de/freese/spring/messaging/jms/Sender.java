/**
 * Created: 01.02.2019
 */

package de.freese.spring.messaging.jms;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class Sender implements CommandLineRunner
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    /**
     *
     */
    private final JmsTemplate jmsTemplate;

    /**
     * Erstellt ein neues {@link Sender} Object.
     *
     * @param jmsTemplate {@link JmsTemplate}
     */
    public Sender(final JmsTemplate jmsTemplate)
    {
        super();

        this.jmsTemplate = Objects.requireNonNull(jmsTemplate, "jmsTemplate required");
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            LOGGER.info("Sending an email message: {}", (i + 1));
            this.jmsTemplate.convertAndSend("mailbox", new Email("info@example.com", "Hello-" + (i + 1)));
        }

        Thread.sleep(1000);
    }
}
