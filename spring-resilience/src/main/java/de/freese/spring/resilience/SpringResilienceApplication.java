package de.freese.spring.resilience;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class SpringResilienceApplication
{
    /**
     * @author Thomas Freese
     */
    @RestController
    class FailingRestController
    {
        /**
        *
        */
        private final ReactiveCircuitBreaker circuitBreaker;

        /**
         *
         */
        private final FailingService service;

        /**
         * Erstellt ein neues {@link FailingRestController} Object.
         *
         * @param service {@link FailingService}
         * @param cbf {@link ReactiveCircuitBreakerFactory}
         */
        FailingRestController(final FailingService service, final ReactiveCircuitBreakerFactory cbf)
        {
            super();

            this.service = Objects.requireNonNull(service, "service required");
            this.circuitBreaker = cbf.create("greet");
        }

        /**
         * @param name {@link Optional}
         * @return {@link Publisher}
         */
        @GetMapping("greet")
        Publisher<String> greet(@RequestParam final Optional<String> name)
        {
            Mono<String> results = this.service.greet(name);

            return this.circuitBreaker.run(results, throwable -> Mono.just("fallback: hello world !")).map(r -> r + "\n");
        }
    }

    /**
     * @author Thomas Freese
     */
    @Service
    class FailingService
    {
        /**
         *
         */
        private final Logger LOGGER = LoggerFactory.getLogger(FailingService.class);

        /**
        *
        */
        // @LocalServerPort
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
                this.LOGGER.error(null, ex);
            }

            return "???";
        }

        /**
         * @param name {@link Optional}
         * @return {@link Mono}
         */
        Mono<String> greet(final Optional<String> name)
        {
            var seconds = (long) (Math.random() * 10);

            //@formatter:off
            return name
                    .map(s -> {
                        var msg = "Hello " + s + " ! (in " + seconds + " Seconds) on " + getHost();
                        this.LOGGER.info(msg);
                        return Mono.just(msg);
                        })
                    .orElse(Mono.error(new NullPointerException("name")))
                    .delayElement(Duration.ofSeconds(seconds));
            //@formatter:on
        }
    }

    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(SpringResilienceApplication.class, args);
    }

    /**
     * @return {@link ReactiveCircuitBreakerFactory}
     */
    @Bean
    ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> circuitBreakerFactory()
    {
        var factory = new ReactiveResilience4JCircuitBreakerFactory();

        //@formatter:off
        factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(5)).build())
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .build()
                );
        //@formatter:on

        return factory;
    }
}