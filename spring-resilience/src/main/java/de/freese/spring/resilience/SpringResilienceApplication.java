package de.freese.spring.resilience;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import reactor.core.publisher.Mono;

/**
 * https://github.com/spring-cloud/spring-cloud-circuitbreaker<br>
 * curl http://localhost:8080/greet?name=tommy
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public class SpringResilienceApplication implements CommandLineRunner
{
    /**
     * @author Thomas Freese
     */
    @RestController
    class FailingRestController
    {
        // /**
        // *
        // */
        // private final ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> cbf;

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
        FailingRestController(final FailingService service,
                final ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> cbf)
        {
            super();

            // this.cbf = cbf;
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
            // return this.cbf.create("greet").run(results, throwable -> Mono.just("fallback: hello world !")).map(r -> r + "\n");
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
                    .delayElement(Duration.ofSeconds(seconds)
                    );
            //@formatter:on
        }
    }

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringResilienceApplication.class);

    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(SpringResilienceApplication.class, args);
    }

    // /**
    // * @return {@link ReactiveCircuitBreakerFactory}
    // */
    // @Bean
    // ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> circuitBreakerFactory()
    // {
    // var factory = new ReactiveResilience4JCircuitBreakerFactory();
    //
    // return factory;
    // }

    /**
     * @return {@link Customizer}
     */
    @Bean
    Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer()
    {
        //@formatter:off
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                    .timeLimiterConfig(
                            TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(4)
                            )
                    .build()
                    )
                    .build());
        //@formatter:on
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        final Runtime runtime = Runtime.getRuntime();

        final NumberFormat format = NumberFormat.getInstance();

        final long maxMemory = runtime.maxMemory();
        final long allocatedMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        final long mb = 1024 * 1024;
        final String mega = "MB";

        LOGGER.info("========================== System Info ==========================");
        LOGGER.info("User-Dir: " + System.getProperty("user.dir"));
        LOGGER.info("Programm-Args: " + Arrays.toString(args));
        LOGGER.info("CPU Cores: " + runtime.availableProcessors());
        LOGGER.info("Free memory: " + format.format(freeMemory / mb) + mega);
        LOGGER.info("Allocated memory: " + format.format(allocatedMemory / mb) + mega);
        LOGGER.info("Max memory: " + format.format(maxMemory / mb) + mega);
        LOGGER.info("Total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / mb) + mega);
        LOGGER.info("=================================================================\n");
    }
}
