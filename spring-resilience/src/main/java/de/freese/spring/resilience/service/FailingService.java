package de.freese.spring.resilience.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Service
public class FailingService
{
    /**
     *
     */
    private final Logger logger = LoggerFactory.getLogger(FailingService.class);
    /**
     *
     */
    @Value("${server.port}")
    private int port = -1;

    /**
     * @return String
     */
    private String getHost()
    {
        try
        {
            return InetAddress.getLocalHost() + "@" + this.port;
        }
        catch (UnknownHostException ex)
        {
            this.logger.error(null, ex);
        }

        return "???";
    }

    /**
     * @param name {@link Optional}
     *
     * @return {@link Mono}
     */
    public Mono<String> greet(final Optional<String> name)
    {
        var seconds = (long) (Math.random() * 5);

        //@formatter:off
        return name
                .map(s -> {
                    var msg = "Hello " + s + " ! (in " + seconds + " Seconds) on " + getHost();
                    this.logger.info(msg);
                    return Mono.just(msg);
                })
                .orElse(Mono.error(new NullPointerException("name")))
                .delayElement(Duration.ofSeconds(seconds)
                );
        //@formatter:on
    }
}
