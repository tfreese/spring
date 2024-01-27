package de.freese.spring.resilience.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Service
public class FailingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailingService.class);
    @Value("${server.port}")
    private final int port = -1;
    private final Random random = new SecureRandom();

    public Mono<String> greet(final Optional<String> name) {
        final long seconds = random.nextLong(5);

        //@formatter:off
        return name
                .map(s -> {
                    final String msg = "Hello " + s + " ! (in " + seconds + " Seconds) on " + getHost();
                    LOGGER.info(msg);
                    return Mono.just(msg);
                })
                .orElse(Mono.error(new NullPointerException("name")))
                .delayElement(Duration.ofSeconds(seconds)
                );
        //@formatter:on
    }

    private String getHost() {
        try {
            return InetAddress.getLocalHost() + "@" + this.port;
        }
        catch (UnknownHostException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return "???";
    }
}
