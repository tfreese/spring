// Created: 31.07.2019
package de.freese.spring.messaging.jms;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Profile("!test")
public class RunnerSendMessages implements CommandLineRunner {
    private final JmsSender sender;

    @Resource
    private ApplicationContext context;

    public RunnerSendMessages(final JmsSender sender) {
        super();

        this.sender = Objects.requireNonNull(sender, "sender required");
    }

    @Override
    public void run(final String... args) throws Exception {
        for (int i = 0; i < 5; i++) {
            TimeUnit.MILLISECONDS.sleep(500);

            sender.send(new Email("info@example.com", "Hello-" + (i + 1)));
        }

        TimeUnit.MILLISECONDS.sleep(500);

        if (context instanceof ConfigurableApplicationContext cac) {
            cac.close();
        }
        else {
            SpringApplication.exit(context, () -> 1);
        }

        TimeUnit.MILLISECONDS.sleep(1000);

        System.exit(0);
    }
}
