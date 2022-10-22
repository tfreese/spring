// Created: 31.01.2019
package de.freese.spring.messaging.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * @author Thomas Freese
 */
public class JmsReceiver
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsReceiver.class);

    @JmsListener(destination = "mailbox", containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(final Email email, @Header(JmsHeaders.DESTINATION) final String queue)
    {
        LOGGER.info("{}: Received from Queue '{}' <{}>", Thread.currentThread().getName(), queue, email);
    }
}
