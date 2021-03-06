/**
 * Created: 31.01.2019
 */
package org.spring.messaging.amqp.rabbitmq;

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
     * @param email {@link Email}
     * @param queue String
     */
    @RabbitListener(queues = SpringRabbitMqApplication.QUEUE_NAME)
    // @SendTo("returnQueue")
    public void receiveMessage(final Email email, @Header(AmqpHeaders.CONSUMER_QUEUE) final String queue)
    {
        System.out.println(Thread.currentThread().getName() + ": Received from Queue '" + queue + "' <" + email + ">");
    }
}
