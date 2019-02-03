/**
 * Created: 31.01.2019
 */

package org.spring.messaging.amqp.qpid;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class Receiver
{
    /**
     * Erstellt ein neues {@link Receiver} Object.
     */
    public Receiver()
    {
        super();
    }

    /**
     * @param email {@link Email}
     */
    @RabbitListener(queues = SpringQpidApplication.queueName)
    // @SendTo("returnQueue")
    public void receiveMessage(final Email email)
    {
        System.out.println(Thread.currentThread().getName() + ": Received <" + email + ">");
    }
}
