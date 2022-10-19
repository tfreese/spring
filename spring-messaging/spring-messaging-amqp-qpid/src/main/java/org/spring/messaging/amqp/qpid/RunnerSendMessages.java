// Created: 31.07.2019
package org.spring.messaging.amqp.qpid;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class RunnerSendMessages implements CommandLineRunner
{
    private final AmqpSender sender;

    @Resource
    private ApplicationContext context;

    public RunnerSendMessages(final AmqpSender sender)
    {
        super();

        this.sender = Objects.requireNonNull(sender, "sender required");
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String... args) throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            TimeUnit.MILLISECONDS.sleep(500);

            this.sender.send(new Email("info@example.com", "Hello-" + (i + 1)));
        }

        TimeUnit.MILLISECONDS.sleep(500);

        //        System.exit(0);
        SpringApplication.exit(context, () -> 0);
    }
}
