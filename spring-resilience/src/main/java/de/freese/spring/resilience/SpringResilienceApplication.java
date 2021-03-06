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
 * for i in {1..10}; do curl localhost:8080/greet?name=$i; echo ""; done;
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
        /**
         *
         */
        private final ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> reactiveCircuitBreakerFactory;

        /**
         *
         */
        private final FailingService service;

        /**
         * Erstellt ein neues {@link FailingRestController} Object.
         *
         * @param service {@link FailingService}
         * @param reactiveCircuitBreakerFactory {@link ReactiveCircuitBreakerFactory}
         */
        FailingRestController(final FailingService service,
                final ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> reactiveCircuitBreakerFactory)
        {
            super();

            this.reactiveCircuitBreakerFactory = reactiveCircuitBreakerFactory;
            this.service = Objects.requireNonNull(service, "service required");
        }

        /**
         * @return {@link ReactiveCircuitBreaker}
         */
        private ReactiveCircuitBreaker getReactiveCircuitBreaker()
        {
            ReactiveCircuitBreaker circuitBreaker = this.reactiveCircuitBreakerFactory.create("greet");

            return circuitBreaker;
        }

        /**
         * @param name {@link Optional}
         * @return {@link Publisher}
         */
        @GetMapping("greet")
        Publisher<String> greet(@RequestParam final Optional<String> name)
        {
            Mono<String> results = this.service.greet(name);

            return getReactiveCircuitBreaker().run(results, throwable -> Mono.just("fallback: hello world !")).map(r -> r + "\n");
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
        private final Logger logger = LoggerFactory.getLogger(FailingService.class);

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
                this.logger.error(null, ex);
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
                        this.logger.info(msg);
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
    @SuppressWarnings("resource")
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
    Customizer<ReactiveResilience4JCircuitBreakerFactory> customizerDefault()
    {
        //@formatter:off
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(10)).build())
                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                    .build());
        //@formatter:on
    }

    /**
     * @return {@link Customizer}
     */
    @Bean
    Customizer<ReactiveResilience4JCircuitBreakerFactory> customizerSlowGreet()
    {
        //@formatter:off
        return factory -> factory.configure(builder ->
                    builder
                        .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(5)).build())
                        .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
        , "greet");
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
        final long divider = 1024L * 1024L;
        final String unit = "MB";

        LOGGER.info("========================== System Info ==========================");
        LOGGER.info("System: {}/{} {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
        LOGGER.info("User-Dir: {}", System.getProperty("user.dir"));
        LOGGER.info("Programm-Args: {}", Arrays.toString(args));
        LOGGER.info("CPU Cores: {}", runtime.availableProcessors());
        LOGGER.info("Free memory: {}", format.format(freeMemory / divider) + unit);
        LOGGER.info("Allocated memory: {}", format.format(allocatedMemory / divider) + unit);
        LOGGER.info("Max memory: {}", format.format(maxMemory / divider) + unit);
        LOGGER.info("Total free memory: {}", format.format((freeMemory + (maxMemory - allocatedMemory)) / divider) + unit);
        LOGGER.info("=================================================================\n");
    }
}
