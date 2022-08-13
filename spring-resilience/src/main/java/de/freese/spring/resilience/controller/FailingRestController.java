package de.freese.spring.resilience.controller;

import java.util.Objects;
import java.util.Optional;

import de.freese.spring.resilience.service.FailingService;
import org.reactivestreams.Publisher;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@RestController
public class FailingRestController
{
    /**
     *
     */
    private final ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> reactiveCircuitBreakerFactory;
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
                          final ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> reactiveCircuitBreakerFactory)
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
        return this.reactiveCircuitBreakerFactory.create("greet");
    }

    /**
     * http://localhost:8080/greet?name=tommy
     *
     * @param name {@link Optional}
     *
     * @return {@link Publisher}
     */
    @GetMapping("greet")
    Publisher<String> greet(@RequestParam final Optional<String> name)
    {
        Mono<String> results = this.service.greet(name);

        return getReactiveCircuitBreaker().run(results, throwable -> Mono.just("fallback (no name): hello world !")).map(r -> r + "\n");
    }
}
