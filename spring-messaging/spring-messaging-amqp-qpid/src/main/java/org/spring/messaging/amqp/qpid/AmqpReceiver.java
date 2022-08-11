// Created: 31.01.2019
package org.spring.messaging.amqp.qpid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class AmqpReceiver
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpReceiver.class);

    /**
     * @param email {@link Email}
     * @param queue String
     */
    @RabbitListener(queues = SpringQpidApplication.QUEUE_NAME)
    // @SendTo("returnQueue")
    // public void receiveMessage(final Email email)
    // {
    // System.out.println(Thread.currentThread().getName() + ": Received <" + email + ">");
    // }
    public void receiveMessage(final Email email, @Header(AmqpHeaders.CONSUMER_QUEUE) final String queue)
    {
        LOGGER.info("{}: Received from Queue '{}' <{}>", Thread.currentThread().getName(), queue, email);
    }
}
