// Created: 31.01.2019
package de.freese.spring.messaging.jms;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class JmsReceiver
{
    /**
     * @param email {@link Email}
     * @param queue String
     */
    @JmsListener(destination = "mailbox", containerFactory = "myFactory")
    public void receiveMessage(final Email email, @Header(JmsHeaders.DESTINATION) final String queue)
    {
        System.out.println(Thread.currentThread().getName() + ": Received from Queue '" + queue + "' <" + email + ">");
    }
}
