/**
 * Created: 31.01.2019
 */

package de.freese.spring.messaging.jms;

import org.springframework.jms.annotation.JmsListener;
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
    @JmsListener(destination = "mailbox", containerFactory = "myFactory")
    public void receiveMessage(final Email email)
    {
        System.out.println(Thread.currentThread().getName() + ": Received <" + email + ">");
    }
}
