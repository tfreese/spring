/**
 * Created: 01.02.2019
 */

package org.spring.messaging.amqp.rabbitmq;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.CommandLineRunner;
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
    private final AmqpTemplate amqpTemplate;

    /**
     * Erstellt ein neues {@link Sender} Object.
     *
     * @param amqpTemplate {@link AmqpTemplate}
     */
    public Sender(final AmqpTemplate amqpTemplate)
    {
        super();

        this.amqpTemplate = Objects.requireNonNull(amqpTemplate, "amqpTemplate required");
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
            // this.amqpTemplate.convertAndSend(SpringRabbitMqApplication.topicExchangeName, "foo.bar.baz", "Hello from RabbitMQ - " + (i + 1));
            this.amqpTemplate.convertAndSend(SpringRabbitMqApplication.topicExchangeName, "foo.bar.baz", new Email("info@example.com", "Hello-" + (i + 1)));

        }

        Thread.sleep(1000);
    }
}
