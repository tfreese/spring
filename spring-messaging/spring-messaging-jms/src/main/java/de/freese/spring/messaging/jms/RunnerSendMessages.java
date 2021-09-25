// Created: 31.07.2019
package de.freese.spring.messaging.jms;

import java.util.Objects;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class RunnerSendMessages implements CommandLineRunner
{
    /**
     *
     */
    private final JmsSender sender;

    /**
     * Erstellt ein neues {@link RunnerSendMessages} Object.
     *
     * @param sender {@link JmsSender}
     */
    public RunnerSendMessages(final JmsSender sender)
    {
        super();

        this.sender = Objects.requireNonNull(sender, "sender required");
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            this.sender.send(new Email("info@example.com", "Hello-" + (i + 1)));
        }

        Thread.sleep(1000);
    }
}
