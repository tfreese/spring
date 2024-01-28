package de.freese.spring.resilience.config;

import java.time.Duration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
public class ResilienceConfig {
    @Bean
    Customizer<ReactiveResilience4JCircuitBreakerFactory> customizerDefault() {
        //@formatter:off
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(10)).build())
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .build())
                ;
        //@formatter:on
    }

    @Bean
    Customizer<ReactiveResilience4JCircuitBreakerFactory> customizerSlowGreet() {
        //@formatter:off
        return factory -> factory.configure(builder ->
                        builder
                                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(2)).build())
                                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                , "greet")
                ;
        //@formatter:on
    }

    // @Bean
    // ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> circuitBreakerFactory()
    // {
    // var factory = new ReactiveResilience4JCircuitBreakerFactory();
    //
    // return factory;
    // }
}
