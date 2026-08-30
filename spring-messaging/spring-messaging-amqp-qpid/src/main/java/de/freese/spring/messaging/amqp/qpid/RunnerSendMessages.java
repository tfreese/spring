package de.freese.spring.messaging.amqp.qpid;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 * @since 31.07.2019
 */
@Component
public class RunnerSendMessages implements CommandLineRunner {
    private final AmqpSender sender;

    @Resource
    private ApplicationContext context;

    public RunnerSendMessages(final AmqpSender sender) {
        super();

        this.sender = Objects.requireNonNull(sender, "sender required");
    }

    @Override
    public void run(final String @NonNull ... args) throws Exception {
        for (int i = 0; i < 5; i++) {
            TimeUnit.MILLISECONDS.sleep(500L);

            this.sender.send(new Email("info@example.com", "Hello-" + (i + 1)));
        }

        TimeUnit.MILLISECONDS.sleep(500L);

        if (context instanceof final ConfigurableApplicationContext cac) {
            cac.close();
        }
        else {
            SpringApplication.exit(context, () -> 1);
        }

        TimeUnit.MILLISECONDS.sleep(1000L);

        System.exit(0);
    }
}
