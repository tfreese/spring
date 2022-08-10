// Created: 31.01.2019
package de.freese.spring.messaging.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsReceiver.class);

    /**
     * @param email {@link Email}
     * @param queue String
     */
    @JmsListener(destination = "mailbox", containerFactory = "myFactory")
    public void receiveMessage(final Email email, @Header(JmsHeaders.DESTINATION) final String queue)
    {
        LOGGER.info("{}: Received from Queue '{}' <{}>", Thread.currentThread().getName(), queue, email);
    }
}
