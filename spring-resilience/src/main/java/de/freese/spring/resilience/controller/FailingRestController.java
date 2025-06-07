package de.freese.spring.resilience.controller;

import java.util.Objects;

import org.reactivestreams.Publisher;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import de.freese.spring.resilience.service.FailingService;

/**
 * @author Thomas Freese
 */
@RestController
public class FailingRestController {
    private final ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> reactiveCircuitBreakerFactory;
    private final FailingService service;

    FailingRestController(final FailingService service, final ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration,
            Resilience4JConfigBuilder> reactiveCircuitBreakerFactory) {
        super();

        this.reactiveCircuitBreakerFactory = reactiveCircuitBreakerFactory;
        this.service = Objects.requireNonNull(service, "service required");
    }

    /**
     * <a href="http://localhost:8080/greet?name=tommy">localhost</a>
     */
    @GetMapping("greet")
    Publisher<String> greet(@RequestParam final String name) {
        final Mono<String> results = service.greet(name);

        return getReactiveCircuitBreaker().run(results, throwable -> Mono.just("fallback (no name): hello world !")).map(r -> r + System.lineSeparator());
    }

    /**
     * <a href="http://localhost:8080/greetFail">localhost</a>
     */
    @GetMapping("greetFail")
    Publisher<String> greetFail() {
        return greet(null);
    }

    private ReactiveCircuitBreaker getReactiveCircuitBreaker() {
        return reactiveCircuitBreakerFactory.create("greet");
    }
}
